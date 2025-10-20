package co.za.cput.repository.generic;

import co.za.cput.domain.generic.UserAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthenticationRepository extends JpaRepository<UserAuthentication, Long> {
    boolean existsByUsernameIgnoreCaseOrContact_EmailIgnoreCase(String username, String email);

    Optional<UserAuthentication> findByUsernameIgnoreCaseOrContact_EmailIgnoreCase(String username, String email);
}