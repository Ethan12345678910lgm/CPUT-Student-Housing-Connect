package co.za.cput.controller.users;

import co.za.cput.domain.users.Student;
import co.za.cput.dto.LoginRequest;
import co.za.cput.dto.LoginResponse;
import co.za.cput.service.users.TooManyLoginAttemptsException;
import co.za.cput.service.users.implementation.AuthenticationService;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import co.za.cput.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final StudentServiceImpl studentService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    StudentServiceImpl studentService) {
        this.authenticationService = authenticationService;
        this.studentService = studentService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null) {
            return ResponseEntity.badRequest().body(LoginResponse.failure("Login request is required."));
        }

        try {
            LoginResponse loginResponse = authenticationService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword(),
                    loginRequest.getRole()
            );

            if (loginResponse.isAuthenticated()) {
                return ResponseEntity.ok(loginResponse);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
        } catch (TooManyLoginAttemptsException exception) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(LoginResponse.failure(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(LoginResponse.failure(exception.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Student student) {
        if (student == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student details are required."));
        }

        if (student.getContact() == null || Helper.isNullorEmpty(student.getContact().getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "A valid email address is required."));
        }

        String normalisedEmail = student.getContact().getEmail().trim().toLowerCase(Locale.ROOT);

        if (authenticationService.emailExists(normalisedEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists."));
        }

        student.getContact().setEmail(normalisedEmail);

        try {
            Student createdStudent = studentService.create(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        }
    }
}
