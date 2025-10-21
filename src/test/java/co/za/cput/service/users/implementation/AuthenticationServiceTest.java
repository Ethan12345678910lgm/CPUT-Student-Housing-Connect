package co.za.cput.service.users.implementation;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.domain.users.Student;
import co.za.cput.dto.LoginResponse;
import co.za.cput.repository.users.AdministratorRepository;
import co.za.cput.repository.users.LandLordRepository;
import co.za.cput.service.users.implementation.AuthenticationService;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private StudentServiceImpl studentService;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private LandLordRepository landLordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void loginWithRegisteredStudentSucceeds() {
        Contact contact = new Contact.Builder()
                .setEmail("integration@student.test")
                .setPhoneNumber("0712345678")
                .setAlternatePhoneNumber("0723456789")
                .setIsEmailVerified(false)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Student student = new Student.Builder()
                .setStudentName("Integration")
                .setStudentSurname("Test")
                .setDateOfBirth(LocalDate.now().minusYears(20))
                .setGender("Female")
                .setPassword("Password1234")
                .setRegistrationDate(LocalDateTime.now())
                .setIsStudentVerified(false)
                .setFundingStatus(Student.FundingStatus.SELF_FUNDED)
                .setContact(contact)
                .build();

        Student created = studentService.create(student);
        assertNotNull(created.getStudentID());

        LoginResponse response = authenticationService.login("integration@student.test", "Password1234");
        assertTrue(response.isAuthenticated(), "Expected login to succeed");
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void landlordLoginSucceedsWhenAdminAccountWithSameEmailPending() {
        String sharedEmail = "overlap@test-suite.local";
        String sharedPassword = "SharedPassword123!";

        Contact adminContact = new Contact.Builder()
                .setEmail(sharedEmail)
                .setPhoneNumber("0600000000")
                .setAlternatePhoneNumber("0611111111")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Administrator pendingAdmin = new Administrator.Builder()
                .setAdminName("Pending")
                .setAdminSurname("Approver")
                .setAdminPassword(passwordEncoder.encode(sharedPassword))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.INACTIVE)
                .setSuperAdmin(false)
                .setContact(adminContact)
                .build();

        administratorRepository.save(pendingAdmin);

        Contact landlordContact = new Contact.Builder()
                .setEmail(sharedEmail)
                .setPhoneNumber("0622222222")
                .setAlternatePhoneNumber("0633333333")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Landlord landlord = new Landlord.Builder()
                .setLandlordFirstName("Lwazi")
                .setLandlordLastName("Ngcobo")
                .setVerified(true)
                .setDateRegistered(LocalDate.now())
                .setPassword(passwordEncoder.encode(sharedPassword))
                .setContact(landlordContact)
                .build();

        landLordRepository.save(landlord);

        LoginResponse response = authenticationService.login(sharedEmail, sharedPassword);

        assertTrue(response.isAuthenticated(), "Expected landlord login to succeed despite pending admin role");
        assertEquals("LANDLORD", response.getRole());
    }
    @Test
    void studentLoginWithExplicitRoleIgnoresPendingAdministratorStatus() {
        String sharedEmail = "student-role@test-suite.local";
        String sharedPassword = "RoleAwarePass123";

        Contact adminContact = new Contact.Builder()
                .setEmail(sharedEmail)
                .setPhoneNumber("0601111111")
                .setAlternatePhoneNumber("0612222222")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Administrator pendingAdmin = new Administrator.Builder()
                .setAdminName("Role")
                .setAdminSurname("Pending")
                .setAdminPassword(passwordEncoder.encode(sharedPassword))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.INACTIVE)
                .setSuperAdmin(false)
                .setContact(adminContact)
                .build();

        administratorRepository.save(pendingAdmin);

        Contact studentContact = new Contact.Builder()
                .setEmail(sharedEmail)
                .setPhoneNumber("0721111111")
                .setAlternatePhoneNumber("0732222222")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Student student = new Student.Builder()
                .setStudentName("Role")
                .setStudentSurname("Aware")
                .setDateOfBirth(LocalDate.now().minusYears(21))
                .setGender("Female")
                .setPassword(sharedPassword)
                .setRegistrationDate(LocalDateTime.now())
                .setIsStudentVerified(true)
                .setFundingStatus(Student.FundingStatus.FUNDED)
                .setContact(studentContact)
                .build();

        studentService.create(student);

        LoginResponse response = authenticationService.login(sharedEmail, sharedPassword, "student");

        assertTrue(response.isAuthenticated(), "Expected student login to succeed when role is specified");
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void studentRoleSelectionDoesNotSurfaceAdministratorPendingMessage() {
        String email = "pending-only@test-suite.local";
        String password = "AdminPendingOnly123";

        Contact adminContact = new Contact.Builder()
                .setEmail(email)
                .setPhoneNumber("0603333333")
                .setAlternatePhoneNumber("0614444444")
                .setIsEmailVerified(true)
                .setIsPhoneVerified(false)
                .setPreferredContactMethod(Contact.PreferredContactMethod.EMAIL)
                .build();

        Administrator pendingAdmin = new Administrator.Builder()
                .setAdminName("Pending")
                .setAdminSurname("Only")
                .setAdminPassword(passwordEncoder.encode(password))
                .setAdminRoleStatus(Administrator.AdminRoleStatus.INACTIVE)
                .setSuperAdmin(false)
                .setContact(adminContact)
                .build();

        administratorRepository.save(pendingAdmin);

        LoginResponse response = authenticationService.login(email, password, "student");

        assertFalse(response.isAuthenticated(), "Login should fail because no student account exists");
        assertEquals(
                "Invalid email or password for the selected account type.",
                response.getMessage(),
                "Expected a generic failure message for the chosen role"
        );
    }
}