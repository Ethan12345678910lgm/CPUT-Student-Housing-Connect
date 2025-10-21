package co.za.cput.service.users.implementation;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Administrator;
import co.za.cput.dto.LoginResponse;
import co.za.cput.repository.users.AdministratorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SuperAdminLoginTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SUPER_ADMIN_EMAIL = "admin@cput-housing.co.za";

    @BeforeEach
    void setUp() {
        administratorRepository.deleteAll();

        Contact superAdminContact = new Contact.Builder()
                .setEmail(SUPER_ADMIN_EMAIL)
                .setPhoneNumber("0601234567")
                .setAlternatePhoneNumber("0789876543")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(true)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Administrator superAdmin = new Administrator.Builder()
                .setAdminName("Super")
                .setAdminSurname("Admin")
                .setAdminPassword(passwordEncoder.encode("Admin1234"))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.ACTIVE)
                .setSuperAdmin(true)
                .setContact(superAdminContact)
                .build();

        administratorRepository.save(superAdmin);

        Contact applicantContact = new Contact.Builder()
                .setEmail("new.admin@cput-housing.co.za")
                .setPhoneNumber("0612345678")
                .setAlternatePhoneNumber("0798765432")
                .setIsEmailVerified(false)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Administrator applicant = new Administrator.Builder()
                .setAdminName("New")
                .setAdminSurname("Applicant")
                .setAdminPassword(passwordEncoder.encode("Applicant123"))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.INACTIVE)
                .setSuperAdmin(false)
                .setContact(applicantContact)
                .build();

        administratorRepository.save(applicant);
    }

    @Test
    void superAdminCanLoginWithPendingApplicationsPresent() {
        LoginResponse response = authenticationService.login(SUPER_ADMIN_EMAIL, "Admin1234");
        assertNotNull(response);
        assertTrue(response.isAuthenticated());
        assertEquals("ADMIN", response.getRole());
        assertTrue(Boolean.TRUE.equals(response.getSuperAdmin()));
    }
}