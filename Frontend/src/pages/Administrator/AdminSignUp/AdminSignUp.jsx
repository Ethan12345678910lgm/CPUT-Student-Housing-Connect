import React, { useEffect, useMemo, useState } from "react";
import {
    FaCheckCircle,
    FaClipboardList,
    FaEnvelope,
    FaInfoCircle,
    FaPhone,
    FaShieldAlt,
    FaSync,
    FaUserLock,
    FaUserPlus,
} from "react-icons/fa";
import AdminNavigation from "../../../components/admin/AdminNavigation";
import {
    applyForAdministrator,
    approveAdminApplication,
    fetchPendingAdminApplications,
} from "../../../services/adminService";
import { getCurrentUser } from "../../../services/authService";

const pageStyles = {
    minHeight: "100vh",
    background: "linear-gradient(180deg, #f7f9fc 0%, #eef2f9 100%)",
    padding: "48px 16px 64px",
    fontFamily: '"Segoe UI", sans-serif',
    color: "#1f2a44",
};

const contentStyles = {
    maxWidth: "1100px",
    margin: "0 auto",
    display: "grid",
    gap: "24px",
};

const cardStyles = {
    backgroundColor: "#ffffff",
    borderRadius: "20px",
    boxShadow: "0 24px 60px rgba(15, 23, 42, 0.08)",
    padding: "32px",
    border: "1px solid rgba(148, 163, 184, 0.25)",
};

const formGridStyles = {
    display: "grid",
    gap: "18px",
    gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
};

const labelStyles = {
    display: "grid",
    gap: "8px",
    fontSize: "14px",
    fontWeight: 600,
    color: "#1e293b",
};

const inputStyles = {
    width: "100%",
    padding: "12px 16px",
    borderRadius: "12px",
    border: "1px solid #cbd5f5",
    fontSize: "15px",
    color: "#0f172a",
    backgroundColor: "#f9fbff",
};

const buttonStyles = {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px",
    padding: "12px 20px",
    background: "linear-gradient(135deg, #3056d3, #5b8dff)",
    color: "#ffffff",
    border: "none",
    borderRadius: "12px",
    cursor: "pointer",
    fontSize: "15px",
    fontWeight: 600,
    transition: "transform 0.2s ease, box-shadow 0.2s ease",
};

const secondaryButtonStyles = {
    ...buttonStyles,
    background: "transparent",
    color: "#3056d3",
    border: "1px solid rgba(48, 86, 211, 0.35)",
};

const badgeStyles = {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    padding: "6px 14px",
    borderRadius: "999px",
    fontSize: "13px",
    fontWeight: 500,
    backgroundColor: "rgba(37, 99, 235, 0.1)",
    color: "#1d4ed8",
};

const errorStyles = {
    background: "rgba(239, 68, 68, 0.12)",
    border: "1px solid rgba(239, 68, 68, 0.2)",
    color: "#b91c1c",
    padding: "12px 16px",
    borderRadius: "12px",
    fontSize: "14px",
};

const successStyles = {
    background: "rgba(34, 197, 94, 0.12)",
    border: "1px solid rgba(34, 197, 94, 0.2)",
    color: "#047857",
    padding: "12px 16px",
    borderRadius: "12px",
    fontSize: "14px",
};

const tableStyles = {
    width: "100%",
    borderCollapse: "collapse",
    marginTop: "16px",
};

const tableHeadStyles = {
    textAlign: "left",
    fontSize: "13px",
    textTransform: "uppercase",
    letterSpacing: "0.04em",
    color: "#64748b",
    borderBottom: "1px solid rgba(148, 163, 184, 0.35)",
    padding: "12px 16px",
};

const tableCellStyles = {
    padding: "14px 16px",
    borderBottom: "1px solid rgba(226, 232, 240, 0.7)",
    fontSize: "14px",
    color: "#1f2937",
};

const initialFormState = {
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    alternatePhoneNumber: "",
    password: "",
    confirmPassword: "",
};

function AdminSignUp() {
    const currentUser = useMemo(() => getCurrentUser(), []);
    const isAdminSignedIn = currentUser?.role === "admin";
    const isSuperAdmin = Boolean(currentUser?.superAdmin);

    const [formState, setFormState] = useState(initialFormState);
    const [formErrors, setFormErrors] = useState({});
    const [submissionState, setSubmissionState] = useState({ status: "idle", message: "" });

    const [pendingRequests, setPendingRequests] = useState([]);
    const [pendingError, setPendingError] = useState("");
    const [isLoadingPending, setIsLoadingPending] = useState(false);
    const [processingId, setProcessingId] = useState(null);
    const [refreshKey, setRefreshKey] = useState(0);

    useEffect(() => {
        if (!isSuperAdmin) {
            return;
        }

        let isMounted = true;
        const loadPending = async () => {
            setIsLoadingPending(true);
            setPendingError("");
            try {
                const applications = await fetchPendingAdminApplications(currentUser.userId);
                if (isMounted) {
                    setPendingRequests(Array.isArray(applications) ? applications : []);
                }
            } catch (error) {
                if (isMounted) {
                    setPendingError(error.message || "Unable to load pending administrator requests.");
                }
            } finally {
                if (isMounted) {
                    setIsLoadingPending(false);
                }
            }
        };

        loadPending();

        return () => {
            isMounted = false;
        };
    }, [currentUser, isSuperAdmin, refreshKey]);

    const handleFormChange = (event) => {
        const { name, value } = event.target;
        setFormState((previous) => ({ ...previous, [name]: value }));
        setFormErrors((previous) => ({ ...previous, [name]: "" }));
    };

    const validateForm = () => {
        const errors = {};
        if (!formState.firstName.trim()) {
            errors.firstName = "First name is required.";
        }
        if (!formState.lastName.trim()) {
            errors.lastName = "Last name is required.";
        }
        if (!formState.email.trim()) {
            errors.email = "Email address is required.";
        }
        if (!formState.password.trim()) {
            errors.password = "A secure password is required.";
        } else if (formState.password.trim().length < 8) {
            errors.password = "Password should contain at least 8 characters.";
        }
        if (!formState.confirmPassword.trim()) {
            errors.confirmPassword = "Please confirm your password.";
        } else if (formState.password.trim() !== formState.confirmPassword.trim()) {
            errors.confirmPassword = "The passwords do not match.";
        }
        return errors;
    };

    const handleApplicationSubmit = async (event) => {
        event.preventDefault();
        setSubmissionState({ status: "idle", message: "" });

        const errors = validateForm();
        if (Object.keys(errors).length > 0) {
            setFormErrors(errors);
            return;
        }

        const payload = {
            adminName: formState.firstName.trim(),
            adminSurname: formState.lastName.trim(),
            adminPassword: formState.password.trim(),
            contact: {
                email: formState.email.trim().toLowerCase(),
                phoneNumber: formState.phoneNumber.trim() || null,
                alternatePhoneNumber: formState.alternatePhoneNumber.trim() || null,
                isEmailVerified: false,
                isPhoneVerified: false,
                preferredContactMethod: "EMAIL",
            },
        };

        try {
            setSubmissionState({ status: "pending", message: "Submitting your application..." });
            await applyForAdministrator(payload);
            setSubmissionState({
                status: "success",
                message:
                    "Your administrator application has been received. The super administrator will review it shortly.",
            });
            setFormState(initialFormState);
            setFormErrors({});
        } catch (error) {
            setSubmissionState({
                status: "error",
                message: error.message || "Unable to submit your application. Please try again.",
            });
        }
    };

    const handleApprove = async (applicantId) => {
        setPendingError("");
        setProcessingId(applicantId);
        try {
            await approveAdminApplication(applicantId, currentUser.userId);
            setPendingRequests((previous) => previous.filter((request) => request.adminID !== applicantId));
        } catch (error) {
            setPendingError(error.message || "Unable to approve the selected administrator.");
        } finally {
            setProcessingId(null);
        }
    };

    const renderApplicationForm = () => (
        <section style={cardStyles}>
            <div style={{ display: "grid", gap: "12px", marginBottom: "24px" }}>
                <span style={badgeStyles}>
                    <FaUserPlus aria-hidden="true" /> Become a housing administrator
                </span>
                <h1 style={{ fontSize: "28px", margin: 0 }}>Apply to join the administrator team</h1>
                <p style={{ margin: 0, color: "#475569" }}>
                    Submit your details below. A super administrator will verify your information before granting
                    access to the administration portal.
                </p>
            </div>

            {submissionState.status === "error" && <div style={errorStyles}>{submissionState.message}</div>}
            {submissionState.status === "success" && <div style={successStyles}>{submissionState.message}</div>}

            <form onSubmit={handleApplicationSubmit} style={{ display: "grid", gap: "24px", marginTop: "16px" }}>
                <div style={formGridStyles}>
                    <label style={labelStyles}>
                        First name
                        <input
                            style={inputStyles}
                            type="text"
                            name="firstName"
                            value={formState.firstName}
                            onChange={handleFormChange}
                            placeholder="Jane"
                            required
                        />
                        {formErrors.firstName && <span style={{ color: "#b91c1c", fontSize: "13px" }}>{formErrors.firstName}</span>}
                    </label>
                    <label style={labelStyles}>
                        Last name
                        <input
                            style={inputStyles}
                            type="text"
                            name="lastName"
                            value={formState.lastName}
                            onChange={handleFormChange}
                            placeholder="Doe"
                            required
                        />
                        {formErrors.lastName && <span style={{ color: "#b91c1c", fontSize: "13px" }}>{formErrors.lastName}</span>}
                    </label>
                    <label style={labelStyles}>
                        Email address
                        <div style={{ position: "relative" }}>
                            <FaEnvelope
                                aria-hidden="true"
                                style={{ position: "absolute", top: "50%", left: "16px", transform: "translateY(-50%)", color: "#64748b" }}
                            />
                            <input
                                style={{ ...inputStyles, paddingLeft: "42px" }}
                                type="email"
                                name="email"
                                value={formState.email}
                                onChange={handleFormChange}
                                placeholder="you@example.com"
                                required
                            />
                        </div>
                        {formErrors.email && <span style={{ color: "#b91c1c", fontSize: "13px" }}>{formErrors.email}</span>}
                    </label>
                    <label style={labelStyles}>
                        Phone number (optional)
                        <div style={{ position: "relative" }}>
                            <FaPhoneIcon />
                            <input
                                style={{ ...inputStyles, paddingLeft: "42px" }}
                                type="tel"
                                name="phoneNumber"
                                value={formState.phoneNumber}
                                onChange={handleFormChange}
                                placeholder="071 234 5678"
                            />
                        </div>
                    </label>
                    <label style={labelStyles}>
                        Alternate phone (optional)
                        <div style={{ position: "relative" }}>
                            <FaPhoneIcon />
                            <input
                                style={{ ...inputStyles, paddingLeft: "42px" }}
                                type="tel"
                                name="alternatePhoneNumber"
                                value={formState.alternatePhoneNumber}
                                onChange={handleFormChange}
                                placeholder="082 345 6789"
                            />
                        </div>
                    </label>
                    <label style={labelStyles}>
                        Password
                        <input
                            style={inputStyles}
                            type="password"
                            name="password"
                            value={formState.password}
                            onChange={handleFormChange}
                            placeholder="Create a secure password"
                            required
                        />
                        {formErrors.password && <span style={{ color: "#b91c1c", fontSize: "13px" }}>{formErrors.password}</span>}
                    </label>
                    <label style={labelStyles}>
                        Confirm password
                        <input
                            style={inputStyles}
                            type="password"
                            name="confirmPassword"
                            value={formState.confirmPassword}
                            onChange={handleFormChange}
                            placeholder="Re-enter your password"
                            required
                        />
                        {formErrors.confirmPassword && (
                            <span style={{ color: "#b91c1c", fontSize: "13px" }}>{formErrors.confirmPassword}</span>
                        )}
                    </label>
                </div>

                <button
                    type="submit"
                    style={{ ...buttonStyles, justifySelf: "flex-start" }}
                    disabled={submissionState.status === "pending"}
                >
                    <FaCheckCircle aria-hidden="true" />
                    {submissionState.status === "pending" ? "Submitting..." : "Submit application"}
                </button>
            </form>
        </section>
    );

    const renderSuperAdminPanel = () => (
        <section style={cardStyles}>
            <div style={{ display: "grid", gap: "12px", marginBottom: "24px" }}>
                <span style={{ ...badgeStyles, backgroundColor: "rgba(16, 185, 129, 0.12)", color: "#0f766e" }}>
                    <FaShieldAlt aria-hidden="true" /> Super administrator controls
                </span>
                <h1 style={{ fontSize: "28px", margin: 0 }}>Administrator applications</h1>
                <p style={{ margin: 0, color: "#475569" }}>
                    Review and approve new administrator applications. Only verified administrators will gain access
                    to the management console.
                </p>
            </div>

            <div style={{ display: "flex", gap: "12px", flexWrap: "wrap", alignItems: "center", marginBottom: "12px" }}>
                <button
                    type="button"
                    style={buttonStyles}
                    onClick={() => setRefreshKey((previous) => previous + 1)}
                    disabled={isLoadingPending}
                >
                    <FaSync aria-hidden="true" /> {isLoadingPending ? "Refreshing..." : "Refresh list"}
                </button>
                <span style={{ display: "inline-flex", alignItems: "center", gap: "6px", color: "#475569" }}>
                    <FaInfoCircle aria-hidden="true" /> Applicants will receive an email once approved.
                </span>
            </div>

            {pendingError && <div style={errorStyles}>{pendingError}</div>}

            {isLoadingPending ? (
                <p style={{ color: "#475569" }}>Loading pending administrator applications...</p>
            ) : pendingRequests.length === 0 ? (
                <div style={successStyles}>
                    <FaCheckCircle aria-hidden="true" style={{ marginRight: "8px" }} />
                    There are currently no pending administrator applications.
                </div>
            ) : (
                <div style={{ overflowX: "auto" }}>
                    <table style={tableStyles}>
                        <thead>
                        <tr>
                            <th style={tableHeadStyles}>Applicant</th>
                            <th style={tableHeadStyles}>Email</th>
                            <th style={tableHeadStyles}>Phone</th>
                            <th style={tableHeadStyles}>Status</th>
                            <th style={tableHeadStyles}>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pendingRequests.map((request) => (
                            <tr key={request.adminID}>
                                <td style={tableCellStyles}>
                                    <div style={{ display: "grid", gap: "4px" }}>
                                            <span style={{ fontWeight: 600 }}>
                                                {[request.adminName, request.adminSurname].filter(Boolean).join(" ")}
                                            </span>
                                        <span style={{ fontSize: "12px", color: "#64748b" }}>Pending approval</span>
                                    </div>
                                </td>
                                <td style={tableCellStyles}>{request.contact?.email || "—"}</td>
                                <td style={tableCellStyles}>{request.contact?.phoneNumber || "—"}</td>
                                <td style={tableCellStyles}>
                                        <span style={{ ...badgeStyles, backgroundColor: "rgba(59, 130, 246, 0.08)", color: "#1d4ed8" }}>
                                            Awaiting review
                                        </span>
                                </td>
                                <td style={tableCellStyles}>
                                    <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                                        <button
                                            type="button"
                                            style={buttonStyles}
                                            onClick={() => handleApprove(request.adminID)}
                                            disabled={processingId === request.adminID}
                                        >
                                            <FaCheckCircle aria-hidden="true" />
                                            {processingId === request.adminID ? "Approving..." : "Approve"}
                                        </button>
                                        <button
                                            type="button"
                                            style={secondaryButtonStyles}
                                            disabled
                                            title="Rejections will be supported in a future update"
                                        >
                                            <FaUserLock aria-hidden="true" /> Reject
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );

    const renderAdminNotice = () => (
        <section style={cardStyles}>
            <div style={{ display: "grid", gap: "12px" }}>
                <span style={{ ...badgeStyles, backgroundColor: "rgba(248, 113, 113, 0.15)", color: "#b91c1c" }}>
                    <FaClipboardList aria-hidden="true" /> Limited access
                </span>
                <h1 style={{ fontSize: "28px", margin: 0 }}>Administrator applications require super admin approval</h1>
                <p style={{ margin: 0, color: "#475569" }}>
                    Only the designated super administrator can manage new administrator applications. Please contact
                    your super administrator to request access.
                </p>
            </div>
        </section>
    );

    return (
        <div style={pageStyles}>
            {isAdminSignedIn && <AdminNavigation />}
            <div style={contentStyles}>
                {!isAdminSignedIn && (
                    <section style={cardStyles}>
                        <div style={{ display: "grid", gap: "12px" }}>
                            <span style={badgeStyles}>
                                <FaShieldAlt aria-hidden="true" /> Secure admin onboarding
                            </span>
                            <h1 style={{ fontSize: "30px", margin: 0 }}>Administrator onboarding</h1>
                            <p style={{ margin: 0, color: "#475569" }}>
                                Submit your details below to request access to the CPUT Student Housing Connect
                                administration console. Once approved, you will be notified via email and can sign in
                                using your registered credentials.
                            </p>
                        </div>
                    </section>
                )}

                {isSuperAdmin && renderSuperAdminPanel()}
                {!isSuperAdmin && isAdminSignedIn && renderAdminNotice()}
                {!isAdminSignedIn && renderApplicationForm()}
            </div>
        </div>
    );
}

function FaPhoneIcon() {
    return (
        <FaPhone
            aria-hidden="true"
            style={{ position: "absolute", top: "50%", left: "16px", transform: "translateY(-50%)", color: "#64748b" }}
        />
    );
}

export default AdminSignUp;
