package co.za.cput.dto;

import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.domain.users.Student;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private boolean authenticated;
    private String role;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean verified;
    private String adminRoleStatus;
    private Boolean superAdmin;
    private String message;

    public LoginResponse() {
    }

    private LoginResponse(Builder builder) {
        this.authenticated = builder.authenticated;
        this.role = builder.role;
        this.userId = builder.userId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.verified = builder.verified;
        this.adminRoleStatus = builder.adminRoleStatus;
        this.superAdmin = builder.superAdmin;
        this.message = builder.message;
    }

    public static LoginResponse successForAdministrator(Administrator administrator) {
        if (administrator == null) {
            throw new IllegalArgumentException("Administrator must not be null");
        }

        return new Builder()
                .setAuthenticated(true)
                .setRole("ADMIN")
                .setUserId(administrator.getAdminID())
                .setFirstName(administrator.getAdminName())
                .setLastName(administrator.getAdminSurname())
                .setEmail(administrator.getContact() != null ? administrator.getContact().getEmail() : null)
                .setVerified(administrator.getAdminRoleStatus() == Administrator.AdminRoleStatus.ACTIVE)
                .setAdminRoleStatus(administrator.getAdminRoleStatus() != null
                        ? administrator.getAdminRoleStatus().name()
                        : null)
                .setSuperAdmin(administrator.isSuperAdmin())
                .setMessage("Login successful.")
                .build();
    }

    public static LoginResponse successForLandlord(Landlord landlord) {
        if (landlord == null) {
            throw new IllegalArgumentException("Landlord must not be null");
        }

        return new Builder()
                .setAuthenticated(true)
                .setRole("LANDLORD")
                .setUserId(landlord.getLandlordID())
                .setFirstName(landlord.getLandlordFirstName())
                .setLastName(landlord.getLandlordLastName())
                .setEmail(landlord.getContact() != null ? landlord.getContact().getEmail() : null)
                .setVerified(landlord.isVerified())
                .setMessage("Login successful.")
                .build();
    }

    public static LoginResponse successForStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student must not be null");
        }

        return new Builder()
                .setAuthenticated(true)
                .setRole("STUDENT")
                .setUserId(student.getStudentID())
                .setFirstName(student.getStudentName())
                .setLastName(student.getStudentSurname())
                .setEmail(student.getContact() != null ? student.getContact().getEmail() : null)
                .setVerified(student.getIsStudentVerified())
                .setMessage("Login successful.")
                .build();
    }

    public static LoginResponse failure(String message) {
        return new Builder()
                .setAuthenticated(false)
                .setMessage(message)
                .build();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getVerified() {
        return verified;
    }

    public String getAdminRoleStatus() {
        return adminRoleStatus;
    }

    public Boolean getSuperAdmin() {
        return superAdmin;
    }

    public String getMessage() {
        return message;
    }

    public static class Builder {
        private boolean authenticated;
        private String role;
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;
        private Boolean verified;
        private String adminRoleStatus;
        private Boolean superAdmin;
        private String message;

        public Builder setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setVerified(Boolean verified) {
            this.verified = verified;
            return this;
        }

        public Builder setAdminRoleStatus(String adminRoleStatus) {
            this.adminRoleStatus = adminRoleStatus;
            return this;
        }

        public Builder setSuperAdmin(Boolean superAdmin) {
            this.superAdmin = superAdmin;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(this);
        }
    }
}
