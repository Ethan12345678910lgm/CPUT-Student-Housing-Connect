package co.za.cput.service.users.implementation;

import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.domain.users.Student;
import co.za.cput.dto.LoginResponse;
import co.za.cput.repository.users.AdministratorRepository;
import co.za.cput.repository.users.LandLordRepository;
import co.za.cput.repository.users.StudentRepository;
import co.za.cput.service.users.LoginRateLimiter;
import co.za.cput.service.users.TooManyLoginAttemptsException;
import co.za.cput.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class AuthenticationService {

    private final AdministratorRepository administratorRepository;
    private final LandLordRepository landLordRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;

    @Autowired
    public AuthenticationService(AdministratorRepository administratorRepository,
                                 LandLordRepository landLordRepository,
                                 StudentRepository studentRepository,
                                 PasswordEncoder passwordEncoder,
                                 LoginRateLimiter loginRateLimiter) {
        this.administratorRepository = administratorRepository;
        this.landLordRepository = landLordRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
    }

    public boolean emailExists(String email) {
        if (Helper.isNullorEmpty(email)) {
            return false;
        }

        String normalisedEmail = email.trim().toLowerCase(Locale.ROOT);

        return administratorRepository.existsByContact_EmailIgnoreCase(normalisedEmail)
                || landLordRepository.existsByContact_EmailIgnoreCase(normalisedEmail)
                || studentRepository.existsByContact_EmailIgnoreCase(normalisedEmail);
    }

    public LoginResponse login(String email, String password) {
        return login(email, password, null);
    }

    public LoginResponse login(String email, String password, String role) {

        if (Helper.isNullorEmpty(email) || Helper.isNullorEmpty(password)) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String normalisedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalisedRole = normaliseRole(role);

        if (role != null && !role.trim().isEmpty() && normalisedRole == null) {
            throw new IllegalArgumentException("Unknown account type selected.");
        }

        if (loginRateLimiter.isBlocked(normalisedEmail)) {
            Duration remaining = loginRateLimiter.timeUntilUnlock(normalisedEmail);
            long minutes = Math.max(1, remaining.toMinutes());
            throw new TooManyLoginAttemptsException(
                    String.format("Too many failed attempts. Please try again in %d minute%s.", minutes, minutes == 1 ? "" : "s")
            );
        }

        if ("STUDENT".equals(normalisedRole)) {
            return authenticateStudent(normalisedEmail, password, true);
        }

        if ("LANDLORD".equals(normalisedRole)) {
            return authenticateLandlord(normalisedEmail, password, true);
        }

        if ("ADMIN".equals(normalisedRole)) {
            return authenticateAdministrator(normalisedEmail, password, true);
        }

        return authenticateWithoutExplicitRole(normalisedEmail, password);

    }

    private String normaliseRole(String role) {
        if (Helper.isNullorEmpty(role)) {
            return null;
        }

        String trimmedRole = role.trim().toUpperCase(Locale.ROOT);
        return switch (trimmedRole) {
            case "ADMIN", "ADMINISTRATOR" -> "ADMIN";
            case "LANDLORD" -> "LANDLORD";
            case "STUDENT" -> "STUDENT";
            default -> null;
        };
    }

    private boolean passwordMatches(String rawPassword, String encodedPassword) {
        if (Helper.isNullorEmpty(encodedPassword)) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        return encodedPassword.equals(rawPassword);
    }
    private LoginResponse authenticateStudent(String email, String password, boolean strictRole) {
        Student student = studentRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);

        if (student != null && passwordMatches(password, student.getPassword())) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.successForStudent(student);
        }

        loginRateLimiter.recordFailedAttempt(email);
        return LoginResponse.failure(strictRole
                ? "Invalid email or password for the selected account type."
                : "Invalid email or password.");
    }

    private LoginResponse authenticateLandlord(String email, String password, boolean strictRole) {
        Landlord landlord = landLordRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);

        if (landlord != null && passwordMatches(password, landlord.getPassword())) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.successForLandlord(landlord);
        }

        loginRateLimiter.recordFailedAttempt(email);
        return LoginResponse.failure(strictRole
                ? "Invalid email or password for the selected account type."
                : "Invalid email or password.");
    }

    private LoginResponse authenticateAdministrator(String email, String password, boolean strictRole) {
        Administrator administrator = administratorRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);

        if (administrator != null && passwordMatches(password, administrator.getAdminPassword())) {
            Administrator.AdminRoleStatus roleStatus = administrator.getAdminRoleStatus();
            if (roleStatus == Administrator.AdminRoleStatus.ACTIVE) {
                loginRateLimiter.resetAttempts(email);
                return LoginResponse.successForAdministrator(administrator);
            }

            loginRateLimiter.resetAttempts(email);
            String message = roleStatus == Administrator.AdminRoleStatus.SUSPENDED
                    ? "Your administrator account has been suspended."
                    : "Your administrator account is awaiting approval.";
            return LoginResponse.failure(message);
        }

        loginRateLimiter.recordFailedAttempt(email);
        return LoginResponse.failure(strictRole
                ? "Invalid email or password for the selected account type."
                : "Invalid email or password.");
    }

    private LoginResponse authenticateWithoutExplicitRole(String email, String password) {
        String pendingAdministratorMessage = null;
        boolean administratorCredentialsMatched = false;

        Administrator administrator = administratorRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);

        if (administrator != null && passwordMatches(password, administrator.getAdminPassword())) {
            administratorCredentialsMatched = true;
            Administrator.AdminRoleStatus roleStatus = administrator.getAdminRoleStatus();
            if (roleStatus == Administrator.AdminRoleStatus.ACTIVE) {
                loginRateLimiter.resetAttempts(email);
                return LoginResponse.successForAdministrator(administrator);
            }

            pendingAdministratorMessage = roleStatus == Administrator.AdminRoleStatus.SUSPENDED
                    ? "Your administrator account has been suspended."
                    : "Your administrator account is awaiting approval.";
        }

        Landlord landlord = landLordRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);
        if (landlord != null && passwordMatches(password, landlord.getPassword())) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.successForLandlord(landlord);
        }

        Student student = studentRepository
                .findFirstByContact_EmailIgnoreCase(email)
                .orElse(null);
        if (student != null && passwordMatches(password, student.getPassword())) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.successForStudent(student);
        }

        if (pendingAdministratorMessage != null) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.failure(pendingAdministratorMessage);
        }

        if (administratorCredentialsMatched) {
            loginRateLimiter.resetAttempts(email);
            return LoginResponse.failure("Invalid email or password.");
        }

        loginRateLimiter.recordFailedAttempt(email);
        return LoginResponse.failure("Invalid email or password.");
    }
}
