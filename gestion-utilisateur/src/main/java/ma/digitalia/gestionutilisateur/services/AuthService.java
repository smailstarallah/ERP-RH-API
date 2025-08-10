package ma.digitalia.gestionutilisateur.services;

import ma.digitalia.gestionutilisateur.Enum.UserType;
import ma.digitalia.gestionutilisateur.dto.*;
import ma.digitalia.gestionutilisateur.entities.*;
import ma.digitalia.gestionutilisateur.repositories.UsersRepository;
import ma.digitalia.gestionutilisateur.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'email: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            log.info("Authentification réussie pour l'email: {}", request.getEmail());
        } catch (BadCredentialsException e) {
            log.error("Échec d'authentification - Mot de passe incorrect pour l'email: {}", request.getEmail());
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (AuthenticationException e) {
            log.error("Échec d'authentification pour l'email: {} - Erreur: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur d'authentification: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'authentification pour l'email: {} - Erreur: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Erreur d'authentification: " + e.getMessage());
        }

        var user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé après authentification réussie pour l'email: {}", request.getEmail());
                    return new RuntimeException("Utilisateur non trouvé");
                });

        log.info("Utilisateur trouvé: {} {} (ID: {})", user.getPreNom(), user.getNom(), user.getId());

        if (!user.isActive()) {
            log.warn("Tentative de connexion d'un utilisateur inactif: {}", request.getEmail());
            throw new RuntimeException("Compte utilisateur désactivé");
        }

        // Mettre à jour la dernière connexion
        user.setDernierConnexion(LocalDateTime.now());
        usersRepository.save(user);
        log.debug("Dernière connexion mise à jour pour l'utilisateur: {}", user.getEmail());

        var userDetails = new CustomUserDetails(user);
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Connexion réussie pour l'utilisateur: {} {}", user.getPreNom(), user.getNom());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L) // 1 heure
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .nom(user.getNom())
                        .preNom(user.getPreNom())
                        .email(user.getEmail())
                        .userType(user.getUserType())
                        .active(user.isActive())
                        .build())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'utilisateur existe déjà
        if (usersRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        if (usersRepository.existsByTelephone(request.getTelephone())) {
            throw new RuntimeException("Un utilisateur avec ce téléphone existe déjà");
        }

        // Créer l'utilisateur selon le type
        Users user = createUserByType(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        user = usersRepository.save(user);

        var userDetails = new CustomUserDetails(user);
        var accessToken = jwtService.generateAccessToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(3600L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .nom(user.getNom())
                        .preNom(user.getPreNom())
                        .email(user.getEmail())
                        .userType(user.getUserType())
                        .active(user.isActive())
                        .build())
                .build();
    }

    private Users createUserByType(RegisterRequest request) {
        return switch (request.getUserType()) {
            case EMPLOYE -> {
                Employe employe = new Employe();
                employe.setUserType(UserType.EMPLOYE);
                setCommonFields(employe, request);
                employe.setNumeroEmploye(request.getNumeroEmploye());
                employe.setPoste(request.getPoste());
                yield employe;
            }
            case MANAGER -> {
                Manager manager = new Manager();
                manager.setUserType(UserType.MANAGER);
                setCommonFields(manager, request);
                manager.setDepartment(request.getDepartement());
                yield manager;
            }
            case RH -> {
                RH rh = new RH();
                rh.setUserType(UserType.RH);
                setCommonFields(rh, request);
                rh.setNiveauResponsabilite(request.getNiveau());
                yield rh;
            }
            default -> throw new RuntimeException("Type d'utilisateur non supporté");
        };
    }

    private void setCommonFields(Users user, RegisterRequest request) {
        user.setNom(request.getNom());
        user.setPreNom(request.getPreNom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setDateNaissance(request.getDateNaissance());
        // Validation que userType est bien défini
        if (user.getUserType() == null) {
            throw new RuntimeException("UserType ne peut pas être null");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = usersRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            var userDetails = new CustomUserDetails(user);

            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                var newAccessToken = jwtService.generateAccessToken(userDetails);

                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(3600L)
                        .user(AuthResponse.UserInfo.builder()
                                .id(user.getId())
                                .nom(user.getNom())
                                .preNom(user.getPreNom())
                                .email(user.getEmail())
                                .userType(user.getUserType())
                                .active(user.isActive())
                                .build())
                        .build();
            }
        }
        throw new RuntimeException("Token de rafraîchissement invalide");
    }
}