package co.za.cput.dto;

public class AdminApprovalRequest {
    private String superAdminEmail;
    private String superAdminPassword;

    public AdminApprovalRequest() {
    }

    public String getSuperAdminEmail() {
        return superAdminEmail;
    }

    public void setSuperAdminEmail(String superAdminEmail) {
        this.superAdminEmail = superAdminEmail;
    }

    public String getSuperAdminPassword() {
        return superAdminPassword;
    }

    public void setSuperAdminPassword(String superAdminPassword) {
        this.superAdminPassword = superAdminPassword;
    }
}