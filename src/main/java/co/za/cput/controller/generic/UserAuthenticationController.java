package co.za.cput.controller.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.domain.users.Landlord;
import co.za.cput.service.generic.implementation.ContactServiceImpl;
import co.za.cput.service.generic.implementation.UserAuthenticationServiceImpl;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import co.za.cput.service.users.implementation.LandLordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/UserAuthentication", "/HouseConnect/UserAuthentication"})
@CrossOrigin(origins = "http://localhost:3000")
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
        }        return ResponseEntity.ok(userAuth);
    }

    @PutMapping("/update")
    public ResponseEntity<UserAuthentication> update(@RequestBody UserAuthentication userAuthentication) {
        if (userAuthentication.getAuthenticationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        UserAuthentication updated = userAuthenticationService.update(userAuthentication);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllUserAuthentications")
    public ResponseEntity<List<UserAuthentication>> getAllUserAuthentications() {
        List<UserAuthentication> userAuthList = userAuthenticationService.getAllUserAuthentications();
        if (userAuthList == null || userAuthList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }        return ResponseEntity.ok(userAuthList);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userAuthenticationService.delete(id);
    }

    @PostMapping({"/signup/student", "/api/auth/signup/student"})
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegistrationRequest request) {
        try {
            Contact contact = buildContact(request.getContact());
            Contact savedContact = contactService.create(contact);

            Student student = new Student.Builder()
                    .setStudentName(request.getStudentName())
                    .setStudentSurname(request.getStudentSurname())
                    .setDateOfBirth(request.getDateOfBirth())
                    .setGender(request.getGender())
                    .setRegistrationDate(LocalDateTime.now())
                    .setIsStudentVerified(request.isStudentVerified())
                    .setFundingStatus(Student.FundingStatus.valueOf(request.getFundingStatus()))
                    .setContact(savedContact)
                    .build();

            Student savedStudent = studentService.create(student);

            String username = savedContact.getEmail();
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            UserAuthentication userAuth = new UserAuthentication.Builder()
                    .setUsername(username)
                    .setPassword(hashedPassword)
                    .setUserRole(UserAuthentication.UserRole.STUDENT)
                    .setContact(savedContact)
                    .setStudent(savedStudent)
                    .build();

            UserAuthentication savedUserAuth = userAuthenticationService.create(userAuth);

            return ResponseEntity.ok(new RegistrationResponse(
                    "Registration successful",
                    savedStudent.getStudentID(),
                    savedUserAuth.getAuthenticationId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid data: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping({"/signup/landlord", "/api/auth/signup/landlord"})
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> registerLandlord(@RequestBody LandlordRegistrationRequest request) {
        try {
            Contact contact = buildContact(request.getContact());


            Contact savedContact = contactService.create(contact);

            Landlord landlord = new Landlord.Builder()
                    .setLandlordFirstName(request.getLandlordFirstName())
                    .setLandlordLastName(request.getLandlordLastName())
                    .setDateRegistered(LocalDateTime.now().toLocalDate())
                    .setVerified(request.isVerified())
                    .setContact(savedContact)
                    .build();

            Landlord savedLandlord = landLordService.create(landlord);

            String username = savedContact.getEmail();
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            UserAuthentication userAuth = new UserAuthentication.Builder()
                    .setUsername(username)
                    .setPassword(hashedPassword)
                    .setUserRole(UserAuthentication.UserRole.LANDLORD)
                    .setContact(savedContact)
                    .setLandlord(savedLandlord)
                    .build();

            UserAuthentication savedUserAuth = userAuthenticationService.create(userAuth);

            return ResponseEntity.ok(new RegistrationResponse(
                    "Registration successful",
                    savedLandlord.getLandlordID(),
                    savedUserAuth.getAuthenticationId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Find user by username/email
            List<UserAuthentication> allUsers = userAuthenticationService.getAllUserAuthentications();
            UserAuthentication user = allUsers.stream()
                    .filter(u -> u.getUsername().equals(request.getUsername()) ||
                            (u.getContact() != null && u.getContact().getEmail().equals(request.getUsername())))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("User not found"));
            }

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid credentials"));
            }

            // Get student ID if user is a student
            Long studentId = null;
            if (user.getUserRole() == UserAuthentication.UserRole.STUDENT && user.getStudent() != null) {
                studentId = user.getStudent().getStudentID();
            }

            return ResponseEntity.ok(new LoginResponse(
                    "Login successful",
                    user.getAuthenticationId(),
                    user.getUserRole().toString(),
                    user.getUsername(),
                    studentId // Add student ID to response
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }


    // DTO Classes
    public static class StudentRegistrationRequest {
        private String studentName;
        private String studentSurname;
        private String dateOfBirth;
        private String gender;
        private String password;
        private boolean isStudentVerified;
        private String fundingStatus;
        private ContactRequest contact;

        // Getters and setters
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getStudentSurname() { return studentSurname; }
        public void setStudentSurname(String studentSurname) { this.studentSurname = studentSurname; }

        public java.time.LocalDate getDateOfBirth() {
            return java.time.LocalDate.parse(dateOfBirth);
        }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isStudentVerified() { return isStudentVerified; }
        public void setStudentVerified(boolean studentVerified) { isStudentVerified = studentVerified; }

        public String getFundingStatus() { return fundingStatus; }
        public void setFundingStatus(String fundingStatus) { this.fundingStatus = fundingStatus; }

        public ContactRequest getContact() { return contact; }
        public void setContact(ContactRequest contact) { this.contact = contact; }
    }

    public static class LandlordRegistrationRequest {
        private String landlordFirstName;
        private String landlordLastName;
        private String password;
        private boolean isVerified;
        private ContactRequest contact;

        // Getters and setters
        public String getLandlordFirstName() { return landlordFirstName; }
        public void setLandlordFirstName(String landlordFirstName) { this.landlordFirstName = landlordFirstName; }

        public String getLandlordLastName() { return landlordLastName; }
        public void setLandlordLastName(String landlordLastName) { this.landlordLastName = landlordLastName; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isVerified() { return isVerified; }
        public void setVerified(boolean verified) { isVerified = verified; }

        public ContactRequest getContact() { return contact; }
        public void setContact(ContactRequest contact) { this.contact = contact; }
    }

    public static class ContactRequest {
        private String email;
        private String phoneNumber;
        private String alternatePhoneNumber;
        private boolean isEmailVerified;
        private boolean isPhoneVerified;
        private String preferredContactMethod;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAlternatePhoneNumber() { return alternatePhoneNumber; }
        public void setAlternatePhoneNumber(String alternatePhoneNumber) { this.alternatePhoneNumber = alternatePhoneNumber; }

        public boolean isEmailVerified() { return isEmailVerified; }
        public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

        public boolean isPhoneVerified() { return isPhoneVerified; }
        public void setPhoneVerified(boolean phoneVerified) { isPhoneVerified = phoneVerified; }

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
        private String message;
        private Long studentId;
        private Long authenticationId;

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
        private String message;
        private Long authenticationId;
        private String userRole;
        private String username;
        private Long studentId;

        public LoginResponse(String message, Long authenticationId, String userRole, String username, Long studentId) {
            this.message = message;
            this.authenticationId = authenticationId;
            this.userRole = userRole;
            this.username = username;
            this.studentId = studentId;
        }

        public Long getStudentId() { return studentId; }
        public String getMessage() { return message; }
        public Long getAuthenticationId() { return authenticationId; }
        public String getUserRole() { return userRole; }
        public String getUsername() { return username; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}





/*package co.za.cput.controller.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.domain.users.Student;
import co.za.cput.service.generic.implementation.ContactServiceImpl;
import co.za.cput.service.generic.implementation.UserAuthenticationServiceImpl;
import co.za.cput.service.users.implementation.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/UserAuthentication")
@CrossOrigin(origins = "*") // Allow frontend access
public class UserAuthenticationController {

    private final UserAuthenticationServiceImpl userAuthenticationService;
    private final StudentServiceImpl studentService;
    private final ContactServiceImpl contactService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAuthenticationController(UserAuthenticationServiceImpl userAuthenticationService,
                                        StudentServiceImpl studentService,
                                        ContactServiceImpl contactService,
                                        PasswordEncoder passwordEncoder) {
        this.userAuthenticationService = userAuthenticationService;
        this.studentService = studentService;
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
        if (userAuth == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userAuth);
    }

    @PutMapping("/update")
    public ResponseEntity<UserAuthentication> update(@RequestBody UserAuthentication userAuthentication) {
        if (userAuthentication.getAuthenticationId() == null) {
            return ResponseEntity.badRequest().build();
        }
        UserAuthentication updated = userAuthenticationService.update(userAuthentication);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllUserAuthentications")
    public ResponseEntity<List<UserAuthentication>> getAllUserAuthentications() {
        List<UserAuthentication> userAuthList = userAuthenticationService.getAllUserAuthentications();
        if (userAuthList == null || userAuthList.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userAuthList);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userAuthenticationService.delete(id);
    }

    // New endpoint for student registration
    @PostMapping("/api/auth/signup/student")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegistrationRequest request) {
        try {
            // 1. Create Contact first
            Contact contact = new Contact.Builder()
                    .setEmail(request.getContact().getEmail())
                    .setPhoneNumber(request.getContact().getPhoneNumber())
                    .setAlternatePhoneNumber(request.getContact().getAlternatePhoneNumber())
                    .setIsEmailVerified(request.getContact().isEmailVerified())
                    .setIsPhoneVerified(request.getContact().isPhoneVerified())
                    .setPreferredContactMethod(Contact.PreferredContactMethod.valueOf(request.getContact().getPreferredContactMethod()))
                    .build();

            Contact savedContact = contactService.create(contact);

            // 2. Create Student (without password - it goes to UserAuthentication)
            Student student = new Student.Builder()
                    .setStudentName(request.getStudentName())
                    .setStudentSurname(request.getStudentSurname())
                    .setDateOfBirth(request.getDateOfBirth())
                    .setGender(request.getGender())
                    .setRegistrationDate(LocalDateTime.now())
                    .setIsStudentVerified(request.isStudentVerified())
                    .setFundingStatus(Student.FundingStatus.valueOf(request.getFundingStatus()))
                    .setContact(savedContact)
                    .build();

            Student savedStudent = studentService.create(student);

            // 3. Create UserAuthentication with hashed password
            String username = savedContact.getEmail(); // Use email as username
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            UserAuthentication userAuth = new UserAuthentication.Builder()
                    .setUsername(username)
                    .setPassword(hashedPassword)
                    .setUserRole(UserAuthentication.UserRole.STUDENT)
                    .setContact(savedContact)
                    .setStudent(savedStudent)
                    .build();

            UserAuthentication savedUserAuth = userAuthenticationService.create(userAuth);

            return ResponseEntity.ok(new RegistrationResponse(
                    "Registration successful",
                    savedStudent.getStudentID(),
                    savedUserAuth.getAuthenticationId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Find user by username/email
            List<UserAuthentication> allUsers = userAuthenticationService.getAllUserAuthentications();
            UserAuthentication user = allUsers.stream()
                    .filter(u -> u.getUsername().equals(request.getUsername()) ||
                            (u.getContact() != null && u.getContact().getEmail().equals(request.getUsername())))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("User not found"));
            }

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid credentials"));
            }

            // Get student ID if user is a student
            Long studentId = null;
            if (user.getUserRole() == UserAuthentication.UserRole.STUDENT && user.getStudent() != null) {
                studentId = user.getStudent().getStudentID();
            }

            return ResponseEntity.ok(new LoginResponse(
                    "Login successful",
                    user.getAuthenticationId(),
                    user.getUserRole().toString(),
                    user.getUsername(),
                    studentId // Add student ID to response
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }


    // DTO Classes
    public static class StudentRegistrationRequest {
        private String studentName;
        private String studentSurname;
        private String dateOfBirth;
        private String gender;
        private String password;
        private boolean isStudentVerified;
        private String fundingStatus;
        private ContactRequest contact;

        // Getters and setters
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public String getStudentSurname() { return studentSurname; }
        public void setStudentSurname(String studentSurname) { this.studentSurname = studentSurname; }

        public java.time.LocalDate getDateOfBirth() {
            return java.time.LocalDate.parse(dateOfBirth);
        }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public boolean isStudentVerified() { return isStudentVerified; }
        public void setStudentVerified(boolean studentVerified) { isStudentVerified = studentVerified; }

        public String getFundingStatus() { return fundingStatus; }
        public void setFundingStatus(String fundingStatus) { this.fundingStatus = fundingStatus; }

        public ContactRequest getContact() { return contact; }
        public void setContact(ContactRequest contact) { this.contact = contact; }
    }

    public static class ContactRequest {
        private String email;
        private String phoneNumber;
        private String alternatePhoneNumber;
        private boolean isEmailVerified;
        private boolean isPhoneVerified;
        private String preferredContactMethod;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getAlternatePhoneNumber() { return alternatePhoneNumber; }
        public void setAlternatePhoneNumber(String alternatePhoneNumber) { this.alternatePhoneNumber = alternatePhoneNumber; }

        public boolean isEmailVerified() { return isEmailVerified; }
        public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

        public boolean isPhoneVerified() { return isPhoneVerified; }
        public void setPhoneVerified(boolean phoneVerified) { isPhoneVerified = phoneVerified; }

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
        private String message;
        private Long studentId;
        private Long authenticationId;

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
        private String message;
        private Long authenticationId;
        private String userRole;
        private String username;
        private Long studentId;

        public LoginResponse(String message, Long authenticationId, String userRole, String username, Long studentId) {
            this.message = message;
            this.authenticationId = authenticationId;
            this.userRole = userRole;
            this.username = username;
            this.studentId = studentId;
        }

        public Long getStudentId() { return studentId; }
        public String getMessage() { return message; }
        public Long getAuthenticationId() { return authenticationId; }
        public String getUserRole() { return userRole; }
        public String getUsername() { return username; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
    }
}*/


/*package co.za.cput.controller.generic;

import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.service.generic.implementation.UserAuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/UserAuthentication")
public class UserAuthenticationController {

    private final UserAuthenticationServiceImpl userAuthenticationService;

    @Autowired
    public UserAuthenticationController(UserAuthenticationServiceImpl userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @PostMapping("/create")
    public ResponseEntity<UserAuthentication> create(@RequestBody UserAuthentication userAuthentication) {
        UserAuthentication created = userAuthenticationService.create(userAuthentication);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<UserAuthentication> read(@PathVariable Long id) {
        UserAuthentication userAuth = userAuthenticationService.read(id);
        if (userAuth == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userAuth);
    }

    @PutMapping("/update")
    public ResponseEntity<UserAuthentication> update(@RequestBody UserAuthentication userAuthentication) {
        if (userAuthentication.getAuthenticationId() == null) { // assuming userId is the primary key
            return ResponseEntity.badRequest().build();
        }
        UserAuthentication updated = userAuthenticationService.update(userAuthentication);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllUserAuthentications")
    public ResponseEntity<List<UserAuthentication>> getAllUserAuthentications() {
        List<UserAuthentication> userAuthList = userAuthenticationService.getAllUserAuthentications();
        if (userAuthList == null || userAuthList.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userAuthList);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        userAuthenticationService.delete(id);
    }
}*/
