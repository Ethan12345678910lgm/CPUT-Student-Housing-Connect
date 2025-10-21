import apiClient from "./apiClient";

export const fetchDashboardOverview = async () => {
    const response = await apiClient.get("/admin/dashboard/overview");
    if (!response) {
        return null;
    }
    return response;
};

export const applyForAdministrator = async (application) => {
    if (!application) {
        throw new Error("Application details are required.");
    }

    const response = await apiClient.post("/admins/apply", application);
    return response;
};

export const fetchPendingAdminApplications = async (superAdminId) => {
    if (!superAdminId) {
        throw new Error("Super administrator id is required.");
    }

    const response = await apiClient.get(`/admins/applications?superAdminId=${superAdminId}`);
    if (!response) {
        return [];
    }
    return response;
};

export const approveAdminApplication = async (applicantId, superAdminId) => {
    if (!applicantId) {
        throw new Error("Applicant id is required.");
    }
    if (!superAdminId) {
        throw new Error("Super administrator id is required.");
    }

    const response = await apiClient.post(`/admins/${applicantId}/approve`, {
        superAdminId,
    });

    return response;
};

export const rejectAdminApplication = async (applicantId, superAdminId) => {
    if (!applicantId) {
        throw new Error("Applicant id is required.");
    }
    if (!superAdminId) {
        throw new Error("Super administrator id is required.");
    }

    const response = await apiClient.post(`/admins/${applicantId}/reject`, {
        superAdminId,
    });

    return response;
};

const adminService = {
    fetchDashboardOverview,
    applyForAdministrator,
    fetchPendingAdminApplications,
    approveAdminApplication,
    rejectAdminApplication,
};

export default adminService;
