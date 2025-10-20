import React, { useEffect, useMemo, useState } from "react";
import { FaPaperPlane, FaStar } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import StudentNavigation from "../../../components/student/StudentNavigation";
import { getCurrentUser } from "../../../services/authService";
import { listApplicationsForStudent } from "../../../services/bookingService";
import { submitReview } from "../../../services/reviewService";

const MAX_RATING = 5;

function SubmitReview() {
    const navigate = useNavigate();
    const currentUser = useMemo(() => getCurrentUser(), []);
    const [confirmedBookings, setConfirmedBookings] = useState([]);
    const [selectedBookingId, setSelectedBookingId] = useState("");
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [feedback, setFeedback] = useState(null);

    useEffect(() => {
        if (!currentUser || currentUser.role !== "student") {
            navigate("/student/login", {
                replace: true,
                state: { message: "Please sign in to submit a review." },
            });
            return;
        }

        const loadApplications = async () => {
            try {
                const applications = await listApplicationsForStudent(currentUser.userId);
                const confirmed = applications.filter((application) => application.bookingStatus === "CONFIRMED");
                setConfirmedBookings(confirmed);
                if (confirmed.length > 0) {
                    setSelectedBookingId(confirmed[0].bookingId ?? "");
                }
            } catch (error) {
                setFeedback({ type: "error", message: error.message || "Unable to load eligible bookings." });
            }
        };

        loadApplications();
    }, [currentUser, navigate]);

    const handleRatingClick = (value) => {
        setRating((previous) => (previous === value ? 0 : value));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!selectedBookingId) {
            setFeedback({ type: "error", message: "Select a confirmed booking to review." });
            return;
        }

        setIsSubmitting(true);
        setFeedback(null);

        try {
            await submitReview(selectedBookingId, rating, comment.trim());
            setFeedback({ type: "success", message: "Thank you! Your review has been submitted." });
            setComment("");
            setRating(0);
        } catch (submitError) {
            setFeedback({ type: "error", message: submitError.message || "Unable to submit your review." });
        } finally {
            setIsSubmitting(false);
        }
    };
    return (
        <div className="student-dashboard-page">
            <StudentNavigation />
            <main className="student-dashboard-content">
                <section className="student-welcome-panel">
                    <p className="student-badge">Share your experience</p>
                    <h1>Help other students make confident choices</h1>
                    <p>
                        Provide honest feedback about your stay, the landlord and the surrounding community. Reviews
                        help the CPUT housing team maintain high standards across every listing.
                    </p>
                </section>

                <section className="student-review-card" aria-label="Submit a review">
                    <header>
                        <FaStar aria-hidden="true" />
                        <div>
                            <h2>Submit a residence review</h2>
                            <p>Select the property and let us know how your stay has been so far.</p>
                        </div>
                    </header>
                    {feedback && <div className={`alert ${feedback.type}`}>{feedback.message}</div>}
                    <form className="student-review-form" onSubmit={handleSubmit}>                        <label>
                            <span>Which accommodation are you reviewing?</span>
                        <select
                            value={selectedBookingId}
                            onChange={(event) => setSelectedBookingId(event.target.value)}
                            required
                            disabled={confirmedBookings.length === 0}
                        >
                            {confirmedBookings.length === 0 ? (
                                <option value="">No confirmed stays available</option>
                            ) : (
                                confirmedBookings.map((booking) => (
                                    <option key={booking.bookingId} value={booking.bookingId}>
                                        {booking.accommodationAddress || booking.accommodationSuburb || `Booking #${booking.bookingId}`}
                                    </option>
                                ))
                            )}
                            </select>
                        </label>
                        <label>
                            <span>How would you rate your overall experience?</span>
                            <div className="student-rating-input">
                                {Array.from({ length: MAX_RATING }, (_, index) => index + 1).map((value) => (
                                    <button
                                        type="button"
                                        key={value}
                                        aria-label={`${value} star rating`}
                                        className={value <= rating ? "active" : ""}
                                        onClick={() => handleRatingClick(value)}
                                    >
                                        <FaStar aria-hidden="true" />
                                    </button>
                                ))}
                            </div>
                        </label>
                        <label>
                            <span>Your comments</span>
                            <textarea
                                rows={5}
                                placeholder="Describe the accommodation, landlord communication and neighbourhood."
                                value={comment}
                                onChange={(event) => setComment(event.target.value)}
                            />                        </label>
                        <button type="submit" className="student-primary" disabled={isSubmitting || !selectedBookingId}>
                            <FaPaperPlane aria-hidden="true" />
                            {isSubmitting ? "Submitting..." : "Submit review"}
                        </button>
                    </form>
                </section>
            </main>
        </div>
    );
}

export default SubmitReview;
