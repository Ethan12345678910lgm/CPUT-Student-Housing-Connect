import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import LandlordLayout from "../../../components/landlord/LandlordLayout";
import { getCurrentUser } from "../../../services/authService";
import { listApplicationsForLandlord, updateApplicationStatus } from "../../../services/bookingService";

const statusConfig = {
  IN_PROGRESS: {
    badgeClass: "pending",
    label: "In review",
    actionLabel: "Approve",
    nextStatus: "CONFIRMED",
  },
  CONFIRMED: {
    badgeClass: "approved",
    label: "Approved",
    actionLabel: "Reopen",
    nextStatus: "IN_PROGRESS",
  },
  FAILED: {
    badgeClass: "revoked",
    label: "Declined",
    actionLabel: "Reopen",
    nextStatus: "IN_PROGRESS",
  },
};

const statusFilters = [
  { value: "ALL", label: "All" },
  { value: "IN_PROGRESS", label: "In review" },
  { value: "CONFIRMED", label: "Approved" },
  { value: "FAILED", label: "Declined" },
];

const formatCurrency = (value) =>
    new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR", maximumFractionDigits: 0 }).format(
        Number(value ?? 0)
    );

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

export default function ApplicationRequests() {
  const navigate = useNavigate();
  const currentUser = useMemo(() => getCurrentUser(), []);
  const [applications, setApplications] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    if (!currentUser || currentUser.role !== "landlord") {
      navigate("/landlord/login", {
        replace: true,
        state: { message: "Please sign in as a landlord to review applications." },
      });
    }
  }, [currentUser, navigate]);

  const loadApplications = useCallback(async () => {
    if (!currentUser?.userId) {
      return;
    }

    setIsLoading(true);
    setError("");
    try {
      const bookings = await listApplicationsForLandlord(currentUser.userId);
      setApplications(Array.isArray(bookings) ? bookings : []);
    } catch (requestError) {
      setError(requestError.message || "Unable to load applications at this time.");
    } finally {
      setIsLoading(false);
    }
  }, [currentUser]);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  const filteredApplications = useMemo(() => {
    if (statusFilter === "ALL") {
      return applications;
    }
    return applications.filter((application) => application.bookingStatus === statusFilter);
  }, [applications, statusFilter]);

  const handleStatusUpdate = async (application) => {
    const config = statusConfig[application.bookingStatus] ?? statusConfig.IN_PROGRESS;
    const nextStatus = config.nextStatus;

    if (!nextStatus) {
      return;
    }

    setUpdatingId(application.bookingId);
    setError("");

    try {
      const updated = await updateApplicationStatus(application.bookingId, nextStatus);
      setApplications((previous) =>
          previous.map((item) =>
              item.bookingId === updated.bookingId ? { ...item, bookingStatus: updated.bookingStatus } : item
          )
      );
    } catch (updateError) {
      setError(updateError.message || "We could not update the application status.");
    } finally {
      setUpdatingId(null);
    }
  };

  return (
      <LandlordLayout
          title="Application requests"
          description="Review booking requests, approve qualified students and keep your occupancy pipeline on track."
          actions={(handleLogout) => (
              <button type="button" className="btn-secondary" onClick={handleLogout}>
                Sign out
              </button>
          )}
      >
        <section className="applications-card">
          <header className="auth-card-header" style={{ marginBottom: 16 }}>
            <h2 style={{ margin: 0, fontSize: 22 }}>Recent applications</h2>
            <p style={{ margin: "4px 0 0", color: "var(--color-slate-500)" }}>
              Manage all booking submissions linked to your listings.
            </p>
          </header>

          <div style={{ display: "flex", gap: 12, flexWrap: "wrap", marginBottom: 16 }}>
            <label className="form-field" style={{ flex: "0 0 220px" }}>
              <span>Filter by status</span>
              <select
                  className="select"
                  value={statusFilter}
                  onChange={(event) => setStatusFilter(event.target.value)}
              >
                {statusFilters.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                ))}
              </select>
            </label>
            <button type="button" className="btn-secondary" onClick={loadApplications} disabled={isLoading}>
              Refresh
            </button>
          </div>

          {error && <div className="alert error">{error}</div>}
          {isLoading ? (
              <p style={{ margin: 0 }}>Loading applications...</p>
          ) : filteredApplications.length === 0 ? (
              <p style={{ margin: 0 }}>No applications found for the selected filters.</p>
          ) : (
              <table>
                <thead>
                <tr>
                  <th>Student</th>
                  <th>Requested listing</th>
                  <th>Submitted</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th aria-label="Actions" />
                </tr>
                </thead>
                <tbody>
                {filteredApplications.map((application) => {
                  const config = statusConfig[application.bookingStatus] ?? statusConfig.IN_PROGRESS;
                  const studentName = [application.studentFirstName, application.studentLastName]

                      .filter(Boolean)
                      .join(" ")
                      .trim();
                  const listingAddress = application.accommodationAddress || application.accommodationSuburb || "—";

                  return (
                      <tr key={application.bookingId}>
                        <td>{studentName || application.studentEmail || "Unknown"}</td>
                        <td>{listingAddress}</td>
                        <td>{formatDate(application.requestDate)}</td>
                        <td>{formatCurrency(application.monthlyRent)}</td>
                        <td>
                          <span className={`badge ${config.badgeClass}`}>{config.label}</span>
                        </td>
                        <td>
                          <button
                              type="button"
                              className="btn-primary"
                              onClick={() => handleStatusUpdate(application)}
                              disabled={updatingId === application.bookingId}
                          >
                            {updatingId === application.bookingId ? "Updating..." : config.actionLabel}
                          </button>
                        </td>
                      </tr>
                  );
                })}
                </tbody>
              </table>
          )}
        </section>
      </LandlordLayout>
  );
}
