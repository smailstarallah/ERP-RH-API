package ma.digitalia.gestionutilisateur.repositories;


import ma.digitalia.gestionutilisateur.Enum.UserType;
import ma.digitalia.gestionutilisateur.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    Optional<Users> findByTelephone(String telephone);

    List<Users> findByUserType(UserType userType);
    List<Users> findByActiveTrue();
    List<Users> findByActiveFalse();

    @Query("SELECT u FROM Users u WHERE u.userType = :userType AND u.active = true")
    List<Users> findActiveUsersByType(@Param("userType") UserType userType);

    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}