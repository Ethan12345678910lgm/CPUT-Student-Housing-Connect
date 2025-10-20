package co.za.cput.repository.users;

import co.za.cput.domain.users.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findFirstByContact_EmailIgnoreCase(String email);

    boolean existsByContact_EmailIgnoreCase(String email);

    Optional<Administrator> findFirstBySuperAdminTrue();

    List<Administrator> findByAdminRoleStatus(Administrator.AdminRoleStatus status);
}