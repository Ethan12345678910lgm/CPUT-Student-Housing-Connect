import apiClient from "./apiClient";

export const addToWatchlist = async (studentId, accommodationId) => {
    if (!studentId || !accommodationId) {
        throw new Error("Student and accommodation identifiers are required.");
    }

    return apiClient.post("/watchlist", { studentId, accommodationId });
};

export const listWatchlistItems = async (studentId) => {
    if (!studentId) {
        throw new Error("Student id is required to fetch the watchlist.");
    }

    const response = await apiClient.get(`/watchlist/student/${studentId}`);
    return Array.isArray(response) ? response : [];
};

export const removeFromWatchlist = async (studentId, accommodationId) => {
    if (!studentId || !accommodationId) {
        throw new Error("Student and accommodation identifiers are required.");
    }

    return apiClient.delete(`/watchlist?studentId=${encodeURIComponent(studentId)}&accommodationId=${encodeURIComponent(accommodationId)}`);
};

const watchlistService = {
    addToWatchlist,
    listWatchlistItems,
    removeFromWatchlist,
};

export default watchlistService;