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
        if (Helper.isNullorEmpty(email) || Helper.isNullorEmpty(password)) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String normalisedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (loginRateLimiter.isBlocked(normalisedEmail)) {
            Duration remaining = loginRateLimiter.timeUntilUnlock(normalisedEmail);
            long minutes = Math.max(1, remaining.toMinutes());
            throw new TooManyLoginAttemptsException(
                    String.format("Too many failed attempts. Please try again in %d minute%s.", minutes, minutes == 1 ? "" : "s")
            );
        }

        Administrator administrator = administratorRepository.findFirstByContact_EmailIgnoreCase(normalisedEmail).orElse(null);
        if (administrator != null && passwordMatches(password, administrator.getAdminPassword())) {
            if (administrator.getAdminRoleStatus() != Administrator.AdminRoleStatus.ACTIVE) {
                String message = "Your administrator account is awaiting approval.";
                if (administrator.getAdminRoleStatus() == Administrator.AdminRoleStatus.SUSPENDED) {
                    message = "Your administrator application has been declined.";
                }
                return LoginResponse.failure(message);
            }
            loginRateLimiter.resetAttempts(normalisedEmail);
            return LoginResponse.successForAdministrator(administrator);
        }

        Landlord landlord = landLordRepository.findFirstByContact_EmailIgnoreCase(normalisedEmail).orElse(null);
        if (landlord != null && passwordMatches(password, landlord.getPassword())) {
            loginRateLimiter.resetAttempts(normalisedEmail);
            return LoginResponse.successForLandlord(landlord);
        }

        Student student = studentRepository.findFirstByContact_EmailIgnoreCase(normalisedEmail).orElse(null);
        if (student != null && passwordMatches(password, student.getPassword())) {
            loginRateLimiter.resetAttempts(normalisedEmail);
            return LoginResponse.successForStudent(student);
        }

        loginRateLimiter.recordFailedAttempt(normalisedEmail);
        return LoginResponse.failure("Invalid email or password.");
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
}
