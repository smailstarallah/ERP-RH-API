package ma.digitalia.gestionutilisateur.security;


import lombok.RequiredArgsConstructor;
import ma.digitalia.gestionutilisateur.Enum.UserType;
import ma.digitalia.gestionutilisateur.entities.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Users user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convertir le UserType en rôles Spring Security
        return getRolesByUserType(user.getUserType()).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    private List<String> getRolesByUserType(UserType userType) {
        return switch (userType) {
            case EMPLOYE -> Arrays.asList("USER", "EMPLOYE");
            case MANAGER -> Arrays.asList("USER", "MANAGER");
            case RH -> Arrays.asList("USER", "RH");
        };
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    // Méthode utilitaire pour récupérer l'entité User
    public Users getUser() {
        return user;
    }
}