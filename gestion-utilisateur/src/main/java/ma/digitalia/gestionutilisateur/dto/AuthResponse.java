package ma.digitalia.gestionutilisateur.dto;

import lombok.Builder;
import lombok.Data;
import ma.digitalia.gestionutilisateur.Enum.UserType;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String nom;
        private String preNom;
        private String email;
        private UserType userType;
        private boolean active;
    }
}

