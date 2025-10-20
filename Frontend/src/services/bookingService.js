import apiClient from "./apiClient";

export const applyForListing = async (studentId, accommodationId, options = {}) => {
    if (!studentId || !accommodationId) {
        throw new Error("Student and accommodation identifiers are required to apply.");
    }

    const payload = {
        studentId,
        accommodationId,
        preferredCheckInDate: options.preferredCheckInDate ?? null,
        preferredCheckOutDate: options.preferredCheckOutDate ?? null,
    };

    return apiClient.post("/bookings/apply", payload);

};

export const listApplicationsForLandlord = async (landlordId) => {
    if (!landlordId) {
        throw new Error("Landlord id is required to fetch applications.");

    }

    const response = await apiClient.get(`/bookings/landlord/${landlordId}`);
    return Array.isArray(response) ? response : [];};

export const listApplicationsForStudent = async (studentId) => {

    if (!studentId) {
        throw new Error("Student id is required to fetch applications.");
    }

    const response = await apiClient.get(`/bookings/student/${studentId}`);
    return Array.isArray(response) ? response : [];
};

export const updateApplicationStatus = async (bookingId, nextStatus) => {
    if (!bookingId) {
        throw new Error("Booking id is required to update status.");
    }

    if (!nextStatus) {
        throw new Error("A valid status value is required.");
    }

    return apiClient.patch(`/bookings/applications/${bookingId}/status`, { status: nextStatus });
};

export const listBookings = async (filters = {}) => {
    if (filters.landlordId) {
        return listApplicationsForLandlord(filters.landlordId);
    }

    if (filters.studentId) {
        return listApplicationsForStudent(filters.studentId);
    }

    const response = await apiClient.get("/bookings/getAllBookings");
    return Array.isArray(response) ? response : [];
};

const bookingService = {
    applyForListing,
    listApplicationsForLandlord,
    listApplicationsForStudent,
    updateApplicationStatus,
    listBookings,
};

export default bookingService;
