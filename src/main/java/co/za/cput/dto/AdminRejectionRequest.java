package co.za.cput.dto;

public class AdminRejectionRequest {
    private Long superAdminId;
    private String reason;

    public AdminRejectionRequest() {
    }

    public Long getSuperAdminId() {
        return superAdminId;
    }

    public void setSuperAdminId(Long superAdminId) {
        this.superAdminId = superAdminId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}