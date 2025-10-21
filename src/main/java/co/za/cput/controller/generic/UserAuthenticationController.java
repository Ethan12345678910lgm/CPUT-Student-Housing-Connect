package co.za.cput.controller.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.domain.users.Landlord;
import co.za.cput.domain.users.Student;
import co.za.cput.service.generic.implementation.ContactServiceImpl;
import co.za.cput.service.generic.implementation.UserAuthenticationServiceImpl;
import co.za.cput.service.users.implementation.LandLordServiceImpl;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping({"/UserAuthentication", "/HouseConnect/UserAuthentication"})
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
public class UserAuthenticationController {

    private final UserAuthenticationServiceImpl userAuthenticationService;
    private final StudentServiceImpl studentService;
    private final LandLordServiceImpl landLordService;
    private final ContactServiceImpl contactService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAuthenticationController(UserAuthenticationServiceImpl userAuthenticationService,
                                        StudentServiceImpl studentService,
                                        LandLordServiceImpl landLordService,
                                        ContactServiceImpl contactService,
                                        PasswordEncoder passwordEncoder) {
        this.userAuthenticationService = userAuthenticationService;
        this.studentService = studentService;
        this.landLordService = landLordService;
        this.contactService = contactService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/create")
    public ResponseEntity<UserAuthentication> create(@RequestBody UserAuthentication userAuthentication) {
        UserAuthentication created = userAuthenticationService.create(userAuthentication);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<UserAuthentication> read(@PathVariable Long id) {
        UserAuthentication userAuth = userAuthenticationService.read(id);
        if (userAuth == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userAuth);
    }

    @PutMapping("/update")
    public ResponseEntity<UserAuthentication> update(@RequestBody UserAuthentication userAuthentication) {
        if (userAuthentication.getAuthenticationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        UserAuthentication updated = userAuthenticationService.update(userAuthentication);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllUserAuthentications")
    public ResponseEntity<List<UserAuthentication>> getAllUserAuthentications() {
        List<UserAuthentication> userAuthList = userAuthenticationService.getAllUserAuthentications();
        if (userAuthList == null || userAuthList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userAuthList);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userAuthenticationService.delete(id);
    }

    @PostMapping({"/signup/student", "/api/auth/signup/student"})
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegistrationRequest request) {
        try {
            validateStudentRequest(request);

            String username = normalizeUsername(request.getContact().getEmail());
            if (userAuthenticationService.existsByUsernameOrEmail(username)) {
                return conflict("An account with the supplied email already exists");
            }

            Contact savedContact = contactService.create(buildContact(request.getContact()));

            Student student = new Student.Builder()
                    .setStudentName(request.getStudentName().trim())
                    .setStudentSurname(request.getStudentSurname().trim())
                    .setDateOfBirth(parseDate(request.getDateOfBirth(), "dateOfBirth"))
                    .setGender(request.getGender().trim())
                    .setRegistrationDate(LocalDateTime.now())
                    .setIsStudentVerified(request.isStudentVerified())
                    .setFundingStatus(parseEnum(Student.FundingStatus.class, request.getFundingStatus(), "fundingStatus"))
                    .setContact(savedContact)
                    .build();

            Student savedStudent = studentService.create(student);

            UserAuthentication savedUserAuth = userAuthenticationService.create(
                    new UserAuthentication.Builder()
                            .setUsername(username)
                            .setPassword(passwordEncoder.encode(request.getPassword()))
                            .setUserRole(UserAuthentication.UserRole.STUDENT)
                            .setContact(savedContact)
                            .setStudent(savedStudent)
                            .build()
            );

            return ResponseEntity.ok(new RegistrationResponse(
                    "Registration successful",
                    savedStudent.getStudentID(),
                    savedUserAuth.getAuthenticationId()
            ));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return internalError("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping({"/signup/landlord", "/api/auth/signup/landlord"})
    public ResponseEntity<?> registerLandlord(@RequestBody LandlordRegistrationRequest request) {
        try {
            validateLandlordRequest(request);

            String username = normalizeUsername(request.getContact().getEmail());
            if (userAuthenticationService.existsByUsernameOrEmail(username)) {
                return conflict("An account with the supplied email already exists");
            }

            Contact savedContact = contactService.create(buildContact(request.getContact()));

            Landlord savedLandlord = landLordService.create(
                    new Landlord.Builder()
                            .setLandlordFirstName(request.getLandlordFirstName().trim())
                            .setLandlordLastName(request.getLandlordLastName().trim())
                            .setDateRegistered(LocalDate.now())
                            .setVerified(request.isVerified())
                            .setContact(savedContact)
                            .build()
            );

            UserAuthentication savedUserAuth = userAuthenticationService.create(
                    new UserAuthentication.Builder()
                            .setUsername(username)
                            .setPassword(passwordEncoder.encode(request.getPassword()))
                            .setUserRole(UserAuthentication.UserRole.LANDLORD)
                            .setContact(savedContact)
                            .setLandlord(savedLandlord)
                            .build()
            );

            return ResponseEntity.ok(new RegistrationResponse(
                    "Registration successful",
                    savedLandlord.getLandlordID(),
                    savedUserAuth.getAuthenticationId()
            ));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            return internalError("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            validateLoginRequest(request);

            UserAuthentication user = userAuthenticationService
                    .findByUsernameOrEmail(normalizeUsername(request.getUsername()))
                    .orElse(null);
            if (user == null) {
                return badRequest("User not found");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return badRequest("Invalid credentials");
            }

            Long studentId = null;
            if (user.getUserRole() == UserAuthentication.UserRole.STUDENT && user.getStudent() != null) {
                studentId = user.getStudent().getStudentID();
            }

            return ResponseEntity.ok(new LoginResponse(
                    "Login successful",
                    user.getAuthenticationId(),
                    user.getUserRole().toString(),
                    user.getUsername(),
                    studentId
            ));
        } catch (Exception e) {
            return internalError("Login failed: " + e.getMessage());
        }
    }

    private Contact buildContact(ContactRequest request) {
        return new Contact.Builder()
                .setEmail(request.getEmail().trim())
                .setPhoneNumber(request.getPhoneNumber().trim())
                .setAlternatePhoneNumber(request.getAlternatePhoneNumber())
                .setIsEmailVerified(request.isEmailVerified())
                .setIsPhoneVerified(request.isPhoneVerified())
                .setPreferredContactMethod(parseEnum(Contact.PreferredContactMethod.class, request.getPreferredContactMethod(), "preferredContactMethod"))
                .build();
    }

    private void validateStudentRequest(StudentRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getStudentName(), "studentName");
        requireText(request.getStudentSurname(), "studentSurname");
        requireText(request.getGender(), "gender");
        requireText(request.getFundingStatus(), "fundingStatus");
        requireText(request.getPassword(), "password");
        requireText(request.getDateOfBirth(), "dateOfBirth");
        validateContactRequest(request.getContact());
    }

    private void validateLandlordRequest(LandlordRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getLandlordFirstName(), "landlordFirstName");
        requireText(request.getLandlordLastName(), "landlordLastName");
        requireText(request.getPassword(), "password");
        validateContactRequest(request.getContact());
    }

    private void validateContactRequest(ContactRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("contact is required");
        }

        requireText(request.getEmail(), "contact.email");
        requireText(request.getPhoneNumber(), "contact.phoneNumber");
        requireText(request.getPreferredContactMethod(), "contact.preferredContactMethod");

        request.setEmail(request.getEmail().trim());
        request.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getAlternatePhoneNumber() != null) {
            request.setAlternatePhoneNumber(request.getAlternatePhoneNumber().trim());
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getUsername(), "username");
        requireText(request.getPassword(), "password");
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        requireText(value, fieldName);
        String sanitized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        try {
            return Enum.valueOf(enumClass, sanitized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported " + fieldName + " value: " + value);
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        requireText(value, fieldName);
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format. Expected ISO-8601 date");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    private ResponseEntity<ErrorResponse> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(message));
    }

    private ResponseEntity<ErrorResponse> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(message));
    }

    public static class StudentRegistrationRequest {
        private String studentName;
        private String studentSurname;
        private String dateOfBirth;
        private String gender;
        private String password;
        private boolean studentVerified;
        private String fundingStatus;
        private ContactRequest contact;

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getStudentSurname() { return studentSurname; }
        public void setStudentSurname(String studentSurname) { this.studentSurname = studentSurname; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isStudentVerified() { return studentVerified; }
        public void setStudentVerified(boolean studentVerified) { this.studentVerified = studentVerified; }

        public String getFundingStatus() { return fundingStatus; }
        public void setFundingStatus(String fundingStatus) { this.fundingStatus = fundingStatus; }

        public ContactRequest getContact() { return contact; }
        public void setContact(ContactRequest contact) { this.contact = contact; }
    }

    public static class LandlordRegistrationRequest {
        private String landlordFirstName;
        private String landlordLastName;
        private boolean verified;
        private String password;
        private ContactRequest contact;

        public String getLandlordFirstName() { return landlordFirstName; }
        public void setLandlordFirstName(String landlordFirstName) { this.landlordFirstName = landlordFirstName; }

        public String getLandlordLastName() { return landlordLastName; }
        public void setLandlordLastName(String landlordLastName) { this.landlordLastName = landlordLastName; }

        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public ContactRequest getContact() { return contact; }
        public void setContact(ContactRequest contact) { this.contact = contact; }
    }

    public static class ContactRequest {
        private String email;
        private String phoneNumber;
        private String alternatePhoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
        private String preferredContactMethod;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAlternatePhoneNumber() { return alternatePhoneNumber; }
        public void setAlternatePhoneNumber(String alternatePhoneNumber) { this.alternatePhoneNumber = alternatePhoneNumber; }

        public boolean isEmailVerified() { return emailVerified; }
        public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

        public boolean isPhoneVerified() { return phoneVerified; }
        public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }

        public String getPreferredContactMethod() { return preferredContactMethod; }
        public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegistrationResponse {
        private final String message;
        private final Long studentId;
        private final Long authenticationId;

        public RegistrationResponse(String message, Long studentId, Long authenticationId) {
            this.message = message;
            this.studentId = studentId;
            this.authenticationId = authenticationId;
        }

        public String getMessage() { return message; }
        public Long getStudentId() { return studentId; }
        public Long getAuthenticationId() { return authenticationId; }
    }

    public static class LoginResponse {
        private final String message;
        private final Long authenticationId;
        private final String userRole;
        private final String username;
        private final Long studentId;

        public LoginResponse(String message, Long authenticationId, String userRole, String username, Long studentId) {
            this.message = message;
            this.authenticationId = authenticationId;
            this.userRole = userRole;
            this.username = username;
            this.studentId = studentId;
        }

        public String getMessage() { return message; }
        public Long getAuthenticationId() { return authenticationId; }
        public String getUserRole() { return userRole; }
        public String getUsername() { return username; }
        public Long getStudentId() { return studentId; }
    }

    public static class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}
