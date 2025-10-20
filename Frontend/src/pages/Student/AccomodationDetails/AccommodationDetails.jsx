import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchAccommodation } from "../../../services/accommodationService";
import { applyForListing } from "../../../services/bookingService";
import { addToWatchlist } from "../../../services/watchlistService";
import { getCurrentUser } from "../../../services/authService";

function AccommodationDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [accommodation, setAccommodation] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [isApplying, setIsApplying] = useState(false);
    const [isSavingWatchlist, setIsSavingWatchlist] = useState(false);
    const [feedback, setFeedback] = useState(null);

    const currentUser = useMemo(() => getCurrentUser(), []);

    useEffect(() => {
        const loadAccommodation = async () => {
            try {
                const data = await fetchAccommodation(id);
                setAccommodation(data);
            } catch (serviceError) {
                setError(serviceError.message);
            } finally {
                setLoading(false);
            }
        };

        loadAccommodation();
    }, [id]);

    const parsedAddress = useMemo(() => {
        if (!accommodation?.address) {
            return null;
        }
        const { streetNumber, streetName, suburb, city, postalCode } = accommodation.address;
        const addressLine = [streetNumber, streetName].filter(Boolean).join(" ");
        const locality = [suburb, city].filter(Boolean).join(", ");
        const full = [addressLine, locality, postalCode].filter(Boolean).join(", ");
        return {
            addressLine: addressLine || "Address available upon request",
            locality: locality || "",
            full: full || "Cape Town, South Africa",
        };
    }, [accommodation]);

    const mapUrl = parsedAddress
        ? `https://www.google.com/maps?q=${encodeURIComponent(parsedAddress.full)}&output=embed`
        : "https://www.google.com/maps?q=Cape%20Town%20Campus&output=embed";

    const handleApply = async () => {
        if (!currentUser || currentUser.role !== "student") {
            navigate("/student/login", {
                replace: true,
                state: { message: "Sign in as a student to apply for accommodation." },
            });
            return;
        }

        if (!accommodation?.accommodationID) {
            return;
        }

        setIsApplying(true);
        setFeedback(null);

        try {
            await applyForListing(currentUser.userId, accommodation.accommodationID);
            setFeedback({ type: "success", message: "Application submitted. Track it from your dashboard." });
            navigate("/student/applications", { replace: true });
        } catch (applyError) {
            setFeedback({
                type: "error",
                message: applyError.message || "We could not submit your application. Please try again.",
            });
        } finally {
            setIsApplying(false);
        }
    };

    const handleSaveToWatchlist = async () => {
        if (!currentUser || currentUser.role !== "student") {
            navigate("/student/login", {
                replace: true,
                state: { message: "Sign in as a student to save listings." },
            });
            return;
        }

        if (!accommodation?.accommodationID) {
            return;
        }

        setIsSavingWatchlist(true);
        setFeedback(null);

        try {
            await addToWatchlist(currentUser.userId, accommodation.accommodationID);
            setFeedback({ type: "success", message: "Added to your watchlist." });
        } catch (watchlistError) {
            setFeedback({
                type: "error",
                message: watchlistError.message || "We could not save this listing to your watchlist.",
            });
        } finally {
            setIsSavingWatchlist(false);
        }
    };


    if (loading) {
        return (
            <div style={styles.page}>
                <div style={styles.loadingCard}>Loading accommodation details...</div>
            </div>
        );
    }

    if (error || !accommodation) {
        return (
            <div style={styles.page}>
                <div style={styles.errorCard}>
                    <h2>We could not load this listing</h2>
                    <p>{error || "The accommodation may have been removed."}</p>
                    <button style={styles.primaryButton} onClick={() => navigate(-1)}>
                        Go back
                    </button>
                </div>
            </div>
        );
    }

    const wifiIncluded = accommodation.isWifiAvailable ?? accommodation.wifiAvailable;
    const furnished = accommodation.isFurnished ?? accommodation.furnished;
    const utilitiesIncluded = accommodation.isUtilitiesIncluded ?? accommodation.utilitiesIncluded;
    const roomTypeLabel = accommodation.roomType?.toLowerCase?.();
    const statusLabel = accommodation.accommodationStatus?.toLowerCase?.() || accommodation.status?.toLowerCase?.();
    const landlordVerified =
        accommodation.landlord?.verified ?? accommodation.landlord?.isVerified ?? false;

    const amenities = [
        { label: "Dedicated study spaces", available: accommodation.studyAreaAvailable },
        { label: "Laundry on-site", available: accommodation.laundryAvailable },
        { label: "Secure access", available: accommodation.secureAccess },
        { label: "Cleaning services", available: accommodation.cleaningServices },
        { label: "Parking", available: accommodation.parkingAvailable },
        { label: "Meal plans", available: accommodation.mealPlanAvailable },
    ].filter((item) => item.available !== undefined);

    return (
        <div style={styles.page}>
            <div style={styles.headerBar}>
                <button style={styles.backButton} onClick={() => navigate(-1)}>
                    ← Back to results
                </button>
                <span style={styles.statusBadge}>{statusLabel || "available"}</span>
            </div>

            <div style={styles.heroCard}>
                <div style={styles.heroPricing}>
                    <h1 style={styles.title}>R{accommodation.rent?.toFixed(2)} / month</h1>
                    <p style={styles.subtitle}>
                        {parsedAddress?.addressLine}
                        {parsedAddress?.locality ? `, ${parsedAddress.locality}` : ""}
                    </p>
                    <div style={styles.heroMeta}>
                        <div>
                            <strong>{accommodation.distanceFromCampus?.toFixed(1)} km</strong>
                            <span>from CPUT campus</span>
                        </div>
                        <div>
                            <strong>{accommodation.capacity || "–"}</strong>
                            <span>total beds</span>
                        </div>
                        <div>
                            <strong>{accommodation.availableRooms || "–"}</strong>
                            <span>available now</span>
                        </div>
                    </div>
                </div>
                <div style={styles.heroHighlights}>
                    <p>
                        This {roomTypeLabel} apartment offers a {accommodation.bathroomType?.toLowerCase?.()} bathroom with{" "}
                        {wifiIncluded ? "high-speed Wi-Fi" : "optional Wi-Fi"} and {furnished ? "comes fully furnished" : "allows you to bring your own furniture"}.
                    </p>
                    <div style={styles.pillRow}>
                        <span style={styles.pill}>{wifiIncluded ? "Wi-Fi included" : "Wi-Fi optional"}</span>
                        <span style={styles.pill}>{furnished ? "Furnished" : "Unfurnished"}</span>
                        <span style={styles.pill}>{utilitiesIncluded ? "Utilities included" : "Utilities billed"}</span>
                    </div>
                </div>
            </div>

            <div style={styles.contentGrid}>
                <section style={styles.mainCard}>
                    <h2>About this listing</h2>
                    <p style={styles.paragraph}>
                        Crafted for students who value balance, this home features generous natural light, modern security and effortless
                        access to lecture halls. Live within minutes of cafes, libraries and transport hubs.
                    </p>

                    <div style={styles.featureGrid}>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Wi-Fi</span>
                            <span>{wifiIncluded ? "Included" : "Not included"}</span>
                        </div>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Furnished</span>
                            <span>{furnished ? "Yes" : "No"}</span>
                        </div>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Utilities</span>
                            <span>{utilitiesIncluded ? "Included" : "Excluded"}</span>
                        </div>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Bathroom</span>
                            <span>{accommodation.bathroomType}</span>
                        </div>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Room type</span>
                            <span>{accommodation.roomType}</span>
                        </div>
                        <div style={styles.featureCard}>
                            <span style={styles.featureLabel}>Campus distance</span>
                            <span>{accommodation.distanceFromCampus?.toFixed(1)} km</span>
                        </div>
                    </div>

                    {amenities.length > 0 && (
                        <div style={styles.amenitiesCard}>
                            <h3>Amenities & lifestyle</h3>
                            <ul>
                                {amenities.map((item) => (
                                    <li key={item.label}>{item.label}</li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {accommodation.bookings?.length > 0 && (
                        <div style={styles.notice}>
                            <h3>Recent bookings</h3>
                            <p>
                                There are currently {accommodation.bookings.length} booking(s) associated with this listing. Confirmed students
                                may move in from {accommodation.bookings[0].checkInDate}.
                            </p>
                        </div>
                    )}

                    <div style={styles.mapSection}>
                        <h3>Neighbourhood map</h3>
                        <div style={styles.mapContainer}>
                            <iframe
                                title="listing-map"
                                src={mapUrl}
                                style={styles.mapIframe}
                                allowFullScreen
                                loading="lazy"
                                referrerPolicy="no-referrer-when-downgrade"
                            />
                            <div style={styles.mapDetails}>
                                <p>{parsedAddress?.full}</p>
                                <span>
                  Explore the surroundings, calculate commute routes and find lifestyle hotspots within walking distance.
                </span>
                            </div>
                        </div>
                    </div>
                </section>

                <aside style={styles.sidebar}>
                    <div style={styles.sidebarCard}>
                        <h3>Landlord details</h3>
                        <p style={styles.landlordName}>
                            {accommodation.landlord?.landlordFirstName} {accommodation.landlord?.landlordLastName}
                        </p>
                        <p style={styles.landlordEmail}>{accommodation.landlord?.contact?.email}</p>
                        <p style={styles.landlordPhone}>{accommodation.landlord?.contact?.phoneNumber}</p>
                        <p style={styles.landlordVerification}>
                            {landlordVerified ? "This landlord has been verified by CPUT." : "Awaiting verification."}
                        </p>
                        <button
                            style={{
                                ...styles.primaryButton,
                                opacity: isApplying ? 0.7 : 1,
                                cursor: isApplying ? "wait" : "pointer",
                            }}
                            onClick={handleApply}
                            disabled={isApplying}
                        >
                            {isApplying ? "Submitting..." : "Apply for this listing"}
                        </button>
                    </div>

                    <div style={styles.sidebarCard}>
                        <h3>Need more info?</h3>
                        <p style={styles.sidebarText}>
                            Save this listing to your watchlist or request a virtual viewing. Our housing support team responds within 24 hours.
                        </p>
                        <button
                            style={{
                                ...styles.secondaryButton,
                                opacity: isSavingWatchlist ? 0.7 : 1,
                                cursor: isSavingWatchlist ? "wait" : "pointer",
                            }}
                            onClick={handleSaveToWatchlist}
                            disabled={isSavingWatchlist}
                        >
                            {isSavingWatchlist ? "Saving..." : "Save to watchlist"}
                        </button>
                        <button style={styles.ghostButton}>Request a virtual viewing</button>
                    </div>
                </aside>
            </div>
            {feedback && (
                <div
                    role="alert"
                    style={{
                        marginTop: 16,
                        padding: "12px 16px",
                        borderRadius: 12,
                        backgroundColor: feedback.type === "error" ? "#fee2e2" : "#dcfce7",
                        color: feedback.type === "error" ? "#b91c1c" : "#166534",
                    }}
                >
                    {feedback.message}
                </div>
            )}
        </div>
    );
}

const surface = {
    backgroundColor: "rgba(255, 255, 255, 0.82)",
    borderRadius: "28px",
    border: "1px solid rgba(148, 163, 184, 0.2)",
    boxShadow: "0 28px 60px rgba(15, 23, 42, 0.18)",
    backdropFilter: "blur(18px)",
};

const styles = {
    page: {
        minHeight: "100vh",
        background: "linear-gradient(180deg, #f1f5f9 0%, #e2e8f0 60%, #f8fafc 100%)",
        padding: "48px clamp(24px, 6vw, 80px) 96px",
        fontFamily: "'Inter', 'Segoe UI', sans-serif",
        color: "#0f172a",
        display: "flex",
        flexDirection: "column",
        gap: "32px",
    },
    loadingCard: {
        ...surface,
        maxWidth: "480px",
        margin: "120px auto",
        padding: "32px",
        textAlign: "center",
    },
    errorCard: {
        ...surface,
        maxWidth: "520px",
        margin: "120px auto",
        padding: "36px",
        textAlign: "center",
    },
    headerBar: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        maxWidth: "1120px",
        margin: "0 auto",
        width: "100%",
    },
    backButton: {
        border: "none",
        background: "none",
        color: "#1d4ed8",
        cursor: "pointer",
        fontSize: "15px",
        fontWeight: 600,
    },
    statusBadge: {
        padding: "6px 18px",
        borderRadius: "999px",
        backgroundColor: "rgba(16, 185, 129, 0.16)",
        color: "#047857",
        letterSpacing: "0.08em",
        textTransform: "uppercase",
        fontSize: "12px",
        fontWeight: 700,
    },
    heroCard: {
        ...surface,
        display: "grid",
        gridTemplateColumns: "minmax(0, 1fr) minmax(0, 1fr)",
        gap: "32px",
        padding: "36px",
        maxWidth: "1120px",
        width: "100%",
        margin: "0 auto",
    },
    heroPricing: {
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
    heroHighlights: {
        background: "rgba(37, 99, 235, 0.08)",
        borderRadius: "24px",
        padding: "24px",
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
    heroMeta: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(140px, 1fr))",
        gap: "12px",
    },
    title: {
        margin: 0,
        fontSize: "clamp(2rem, 4vw, 3.2rem)",
    },
    subtitle: {
        margin: 0,
        color: "#475569",
    },
    pillRow: {
        display: "flex",
        gap: "12px",
        flexWrap: "wrap",
    },
    pill: {
        padding: "8px 18px",
        borderRadius: "999px",
        backgroundColor: "#1d4ed8",
        color: "#fff",
        fontWeight: 600,
        fontSize: "13px",
    },
    contentGrid: {
        maxWidth: "1120px",
        width: "100%",
        margin: "0 auto",
        display: "grid",
        gridTemplateColumns: "minmax(0, 2fr) minmax(0, 1fr)",
        gap: "32px",
        alignItems: "flex-start",
    },
    mainCard: {
        ...surface,
        padding: "36px",
        display: "flex",
        flexDirection: "column",
        gap: "28px",
    },
    paragraph: {
        margin: 0,
        color: "#475569",
        lineHeight: 1.8,
    },
    featureGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
        gap: "18px",
    },
    featureCard: {
        backgroundColor: "rgba(248, 250, 252, 0.85)",
        borderRadius: "18px",
        padding: "16px",
        display: "flex",
        flexDirection: "column",
        gap: "6px",
    },
    featureLabel: {
        fontSize: "13px",
        color: "#475569",
        textTransform: "uppercase",
        letterSpacing: "0.06em",
    },
    amenitiesCard: {
        backgroundColor: "rgba(37, 99, 235, 0.06)",
        borderRadius: "20px",
        padding: "24px",
    },
    notice: {
        padding: "20px",
        borderRadius: "20px",
        backgroundColor: "rgba(59, 130, 246, 0.12)",
    },
    mapSection: {
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
    mapContainer: {
        ...surface,
        display: "grid",
        gridTemplateColumns: "minmax(0, 2fr) minmax(0, 1fr)",
        overflow: "hidden",
    },
    mapIframe: {
        width: "100%",
        border: "none",
        minHeight: "320px",
    },
    mapDetails: {
        padding: "24px",
        backgroundColor: "rgba(15, 23, 42, 0.85)",
        color: "#e2e8f0",
        display: "flex",
        flexDirection: "column",
        gap: "12px",
    },
    sidebar: {
        display: "flex",
        flexDirection: "column",
        gap: "24px",
    },
    sidebarCard: {
        ...surface,
        padding: "28px",
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
    landlordName: {
        fontWeight: 700,
        marginBottom: "4px",
    },
    landlordEmail: {
        margin: "4px 0",
        color: "#1d4ed8",
    },
    landlordPhone: {
        margin: 0,
        color: "#475569",
    },
    landlordVerification: {
        margin: 0,
        fontSize: "14px",
        color: "#047857",
    },
    sidebarText: {
        margin: 0,
        color: "#475569",
        lineHeight: 1.6,
    },
    primaryButton: {
        padding: "12px 24px",
        borderRadius: "999px",
        border: "none",
        backgroundColor: "#1d4ed8",
        color: "#fff",
        fontWeight: 600,
        cursor: "pointer",
    },
    secondaryButton: {
        padding: "10px 22px",
        borderRadius: "999px",
        border: "none",
        backgroundColor: "rgba(37, 99, 235, 0.12)",
        color: "#1d4ed8",
        fontWeight: 600,
        cursor: "pointer",
    },
    ghostButton: {
        padding: "10px 22px",
        borderRadius: "999px",
        border: "1px solid rgba(148, 163, 184, 0.35)",
        backgroundColor: "transparent",
        color: "#0f172a",
        fontWeight: 600,
        cursor: "pointer",
    },
};

export default AccommodationDetails;
