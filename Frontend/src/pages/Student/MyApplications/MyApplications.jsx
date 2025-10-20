import React, { useEffect, useMemo, useState } from "react";
import { FaCheckCircle, FaClock, FaFileAlt } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import StudentNavigation from "../../../components/student/StudentNavigation";
import { getCurrentUser } from "../../../services/authService";
import { listApplicationsForStudent } from "../../../services/bookingService";

const formatDate = (value) => {
    if (!value) {
        return "—";
    }
    try {
        return new Date(value).toLocaleDateString();
    } catch (error) {
        return value;
    }
};

const statusBadge = (status) => {
    switch (status) {
        case "CONFIRMED":
            return { label: "Approved", icon: <FaCheckCircle aria-hidden="true" />, className: "approved" };
        case "FAILED":
            return { label: "Declined", icon: <FaClock aria-hidden="true" />, className: "revoked" };
        default:
            return { label: "In review", icon: <FaClock aria-hidden="true" />, className: "pending" };
    }
};

function MyApplications() {
    const navigate = useNavigate();
    const currentUser = useMemo(() => getCurrentUser(), []);
    const [applications, setApplications] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!currentUser || currentUser.role !== "student") {
            navigate("/student/login", {
                replace: true,
                state: { message: "Please sign in to view your applications." },
            });
            return;
        }

        const load = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const results = await listApplicationsForStudent(currentUser.userId);
                setApplications(results);
            } catch (requestError) {
                setError(requestError.message || "Unable to load your applications.");
            } finally {
                setIsLoading(false);
            }
        };

        load();
    }, [currentUser, navigate]);
    return (
        <div className="student-dashboard-page">
            <StudentNavigation />
            <main className="student-dashboard-content">
                <section className="student-welcome-panel">
                    <p className="student-badge">Application centre</p>
                    <h1>Track every step of your housing applications</h1>
                    <p>
                        Submit outstanding information, follow up with landlords and monitor upcoming viewing requests
                        from one place.
                    </p>
                </section>

                <section className="student-applications-card" aria-label="Application list">
                    <header>
                        <FaFileAlt aria-hidden="true" />
                        <div>
                            <h2>Current applications</h2>
                            <p>Your most recent submissions and their next actions.</p>
                        </div>
                    </header>

                    {error && <div className="alert error">{error}</div>}

                    {isLoading ? (
                        <p style={{ margin: 0 }}>Loading your applications...</p>
                    ) : applications.length === 0 ? (
                        <p style={{ margin: 0 }}>You have not submitted any applications yet.</p>
                    ) : (
                        <div className="student-applications-table" role="table">
                            <div className="student-applications-row heading" role="row">
                                <span role="columnheader">Reference</span>
                                <span role="columnheader">Listing</span>
                                <span role="columnheader">Status</span>
                                <span role="columnheader">Submitted</span>
                                <span role="columnheader">Next step</span>
                            </div>
                            {applications.map((application) => {
                                const badge = statusBadge(application.bookingStatus);
                                const reference = application.bookingId ? `APP-${String(application.bookingId).padStart(4, "0")}` : "—";
                                const listing = application.accommodationAddress || application.accommodationSuburb || "—";
                                const nextStep =
                                    application.bookingStatus === "CONFIRMED"
                                        ? "Prepare for move-in"
                                        : application.bookingStatus === "FAILED"
                                            ? "Contact the landlord for feedback"
                                            : "Await landlord response";

                                return (
                                    <div key={application.bookingId ?? listing} className="student-applications-row" role="row">
                                        <span role="cell">{reference}</span>
                                        <span role="cell">{listing}</span>
                                        <span role="cell" className={`status ${badge.className}`}>
                                            {badge.icon}
                                            {badge.label}
                                        </span>
                                        <span role="cell">{formatDate(application.requestDate)}</span>
                                        <span role="cell">{nextStep}</span>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </section>

                <section className="student-next-steps" aria-label="Application tips">
                    <div className="student-next-steps-card">
                        <h2>Improve your chances</h2>
                        <ul>
                            <li>Complete your profile and upload supporting documents.</li>
                            <li>Respond quickly to landlord requests for viewings.</li>
                            <li>Keep a record of your communication in the messages tab.</li>
                        </ul>
                    </div>
                    <div className="student-next-steps-card">
                        <h2>Recently approved</h2>
                        <p className="student-approved">
                            <FaCheckCircle aria-hidden="true" /> Belhar Village Lofts confirmed three new tenants this week.

                        </p>
                    </div>
                </section>
            </main>
        </div>
    );
}

export default MyApplications;
