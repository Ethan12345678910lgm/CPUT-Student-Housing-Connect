import apiClient from "./apiClient";

export const submitReview = async (bookingId, rating, comment) => {
    if (!bookingId) {
        throw new Error("A booking reference is required to submit a review.");
    }

    if (!rating) {
        throw new Error("Please select a rating before submitting your review.");
    }

    const payload = {
        rating,
        comment,
    };

    return apiClient.post(`/reviews/addToBooking/${bookingId}`, payload);
};

const reviewService = {
    submitReview,
};

export default reviewService;