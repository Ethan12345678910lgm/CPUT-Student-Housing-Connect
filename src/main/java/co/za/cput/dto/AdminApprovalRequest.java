package co.za.cput.dto;

public class AdminApprovalRequest {
    private Long superAdminId;


    public AdminApprovalRequest() {
    }

    public Long getSuperAdminId() {
        return superAdminId;
    }

    public void setSuperAdminId(Long superAdminId) {
        this.superAdminId = superAdminId;
    }
}