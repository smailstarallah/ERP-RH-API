package ma.digitalia.gestionutilisateur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.digitalia.gestionutilisateur.dto.ApiResponse;
import ma.digitalia.gestionutilisateur.dto.AuthResponse;
import ma.digitalia.gestionutilisateur.dto.LoginRequest;
import ma.digitalia.gestionutilisateur.dto.RegisterRequest;
import ma.digitalia.gestionutilisateur.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Identifiants invalides", "AUTH_ERROR"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REGISTER_ERROR"));
        }
    }

    @PostMapping("/register-list")
    public ResponseEntity<ApiResponse<List<AuthResponse>>> registerList(@Valid @RequestBody List<RegisterRequest> requestList) {
        try {
            log.info("Registering list of users: {}", requestList);
            if (requestList == null || requestList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La liste ne peut pas être vide", "EMPTY_LIST"));
            }

            List<AuthResponse> responses = new ArrayList<>();
            for (RegisterRequest request : requestList) {
                log.info("Registering user: {}", request);
                AuthResponse response = authService.register(request);
                responses.add(response);
            }

            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "REGISTER_ERROR"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Authorization") String refreshToken) {
        try {
            String token = refreshToken.replace("Bearer ", "");
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token invalide", "TOKEN_ERROR"));
        }
    }
}
