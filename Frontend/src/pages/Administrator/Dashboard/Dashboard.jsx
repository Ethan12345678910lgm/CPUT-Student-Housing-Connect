import React, { useEffect, useMemo, useState } from "react";
import {
    FaBell,
    FaClipboardCheck,
    FaClock,
    FaDatabase,
    FaHome,
    FaChartLine,
    FaSearch,
    FaShieldAlt,
    FaUserCheck,
    FaUserShield,
    FaUsers,
} from "react-icons/fa";
import { fetchDashboardOverview } from "../../../services/adminService";
import AdminNavigation from "../../../components/admin/AdminNavigation";

const pageStyles = {
    minHeight: "100vh",
    background: "linear-gradient(180deg, #f6f8fc 0%, #e9eef9 100%)",
    padding: "48px 32px",
    fontFamily: '"Segoe UI", sans-serif',
    color: "#1e293b",
};

const cardStyles = {
    backgroundColor: "#ffffff",
    borderRadius: "22px",
    boxShadow: "0 30px 80px rgba(15, 23, 42, 0.12)",
    padding: "28px",
    border: "1px solid rgba(148, 163, 184, 0.2)",
};

const badgeStyles = {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    padding: "6px 16px",
    borderRadius: "999px",
    fontSize: "13px",
    fontWeight: 600,
    backgroundColor: "rgba(59, 130, 246, 0.1)",
    color: "#1d4ed8",
};

function Dashboard() {
    const [timeframe, setTimeframe] = useState("monthly");
    const [overview, setOverview] = useState(null);
    const [isOverviewLoading, setIsOverviewLoading] = useState(true);
    const [overviewError, setOverviewError] = useState("");

    useEffect(() => {
        const resolveOverview = async () => {
            try {
                const data = await fetchDashboardOverview();
                setOverview(data);
            } catch (error) {
                setOverviewError(error.message);
            } finally {
                setIsOverviewLoading(false);
            }
        };

        resolveOverview();
    }, []);

    const overviewMetrics = useMemo(() => {
        if (!overview) return [];
        return [
            { label: "Students", value: overview.totalStudents, icon: <FaUsers size={20} color="#2563eb" /> },
            { label: "Landlords", value: overview.totalLandlords, icon: <FaUserCheck size={20} color="#16a34a" /> },
            { label: "Verified landlords", value: overview.verifiedLandlords, icon: <FaShieldAlt size={20} color="#0f766e" /> },
            { label: "Listings", value: overview.totalAccommodations, icon: <FaHome size={20} color="#f97316" /> },
            { label: "Pending checks", value: overview.pendingVerifications, icon: <FaClipboardCheck size={20} color="#d97706" /> },
            { label: "Active bookings", value: overview.activeBookings, icon: <FaClock size={20} color="#7c3aed" /> },
        ];
    }, [overview]);

    const occupancyText = useMemo(() => {
        if (!overview) return "0% occupancy";
        const value = typeof overview.occupancyRate === "number" ? overview.occupancyRate : Number(overview.occupancyRate || 0);
        return `${value.toFixed(2)}% occupancy`;
    }, [overview]);

    const metricSets = useMemo(
        () => ({
            daily: {
                headline: "Today",
                summary: [
                    { title: "New student sign-ups", value: 38, change: "+6", icon: <FaUsers size={22} color="#2563eb" /> },
                    { title: "Landlord verifications", value: 9, change: "+2", icon: <FaUserCheck size={22} color="#16a34a" /> },
                    { title: "Listings approved", value: 12, change: "-1", icon: <FaHome size={22} color="#f97316" /> },
                    { title: "Incidents escalated", value: 3, change: "+1", icon: <FaBell size={22} color="#ef4444" /> },
                ],
                trend: [
                    { label: "00h", total: 4 },
                    { label: "06h", total: 9 },
                    { label: "12h", total: 18 },
                    { label: "18h", total: 26 },
                    { label: "24h", total: 38 },
                ],
            },
            weekly: {
                headline: "This week",
                summary: [
                    { title: "New student sign-ups", value: 214, change: "+12", icon: <FaUsers size={22} color="#2563eb" /> },
                    { title: "Landlord verifications", value: 54, change: "+6", icon: <FaUserCheck size={22} color="#16a34a" /> },
                    { title: "Listings approved", value: 61, change: "+4", icon: <FaHome size={22} color="#f97316" /> },
                    { title: "Incidents escalated", value: 11, change: "-3", icon: <FaBell size={22} color="#ef4444" /> },
                ],
                trend: [
                    { label: "Mon", total: 42 },
                    { label: "Tue", total: 76 },
                    { label: "Wed", total: 128 },
                    { label: "Thu", total: 167 },
                    { label: "Fri", total: 214 },
                ],
            },
            monthly: {
                headline: "April 2024",
                summary: [
                    { title: "New student sign-ups", value: 868, change: "+34", icon: <FaUsers size={22} color="#2563eb" /> },
                    { title: "Landlord verifications", value: 219, change: "+14", icon: <FaUserCheck size={22} color="#16a34a" /> },
                    { title: "Listings approved", value: 247, change: "+18", icon: <FaHome size={22} color="#f97316" /> },
                    { title: "Incidents escalated", value: 39, change: "-8", icon: <FaBell size={22} color="#ef4444" /> },
                ],
                trend: [
                    { label: "Week 1", total: 180 },
                    { label: "Week 2", total: 382 },
                    { label: "Week 3", total: 624 },
                    { label: "Week 4", total: 868 },
                ],
            },
        }),
        []
    );

    const { headline, summary, trend } = metricSets[timeframe];

    const verificationQueue = useMemo(
        () => [
            {
                id: "REQ-3022",
                applicant: "Neo Daniels",
                type: "Landlord",
                submitted: "2 hours ago",
                riskScore: "Medium",
                status: "Pending checks",
            },
            {
                id: "LIST-441",
                applicant: "Maverick Residences",
                type: "Listing",
                submitted: "5 hours ago",
                riskScore: "Low",
                status: "Ready for approval",
            },
            {
                id: "REQ-3014",
                applicant: "Khanyisa Properties",
                type: "Landlord",
                submitted: "1 day ago",
                riskScore: "High",
                status: "Flagged for review",
            },
            {
                id: "LIST-437",
                applicant: "Belhar Village Loft 21",
                type: "Listing",
                submitted: "2 days ago",
                riskScore: "Low",
                status: "Awaiting documents",
            },
        ],
        []
    );

    const activityFeed = useMemo(
        () => [
            {
                time: "09:24",
                actor: "Agnes Moyo",
                action: "Approved landlord application",
                context: "Greenleaf Properties",
            },
            {
                time: "08:57",
                actor: "Ethan Jacobs",
                action: "Escalated listing for compliance review",
                context: "Atlantic View 204",
            },
            {
                time: "07:33",
                actor: "Admin Bot",
                action: "Automated reminder sent",
                context: "Documents outstanding: LIST-437",
            },
            {
                time: "06:48",
                actor: "Agnes Moyo",
                action: "New admin invite issued",
                context: "lerato.maseko@cput.ac.za",
            },
        ],
        []
    );

    const statusColors = {
        "Pending checks": { bg: "rgba(59, 130, 246, 0.16)", color: "#1d4ed8" },
        "Ready for approval": { bg: "rgba(16, 185, 129, 0.16)", color: "#0f766e" },
        "Flagged for review": { bg: "rgba(239, 68, 68, 0.16)", color: "#b91c1c" },
        "Awaiting documents": { bg: "rgba(249, 115, 22, 0.18)", color: "#c2410c" },
    };

    const complianceAreas = useMemo(
        () => [
            { name: "Identity", completion: 96 },
            { name: "Background", completion: 88 },
            { name: "Compliance", completion: 92 },
            { name: "Health & Safety", completion: 84 },
        ],
        []
    );

    const renderProgressBar = (value, color) => (
        <div
            style={{
                background: "rgba(148, 163, 184, 0.35)",
                borderRadius: "999px",
                overflow: "hidden",
                height: "10px",
            }}
        >
            <div
                style={{
                    width: `${Math.min(value, 100)}%`,
                    background: color,
                    height: "100%",
                    transition: "width 0.3s ease",
                }}
            />
        </div>
    );

    return (
        <div className="admin-page-shell">
            <AdminNavigation />
            <main className="admin-page-content" style={pageStyles}>
                <div style={{ maxWidth: "1220px", margin: "0 auto", display: "grid", gap: "28px" }}>
                    <header
                        style={{
                            display: "flex",
                            flexWrap: "wrap",
                            alignItems: "flex-start",
                            justifyContent: "space-between",
                            gap: "20px",
                        }}
                    >
                        <div>
                            <div style={badgeStyles}>
                                <FaShieldAlt size={14} />
                                Admin Overview
                            </div>
                            <h1 style={{ fontSize: "34px", margin: "18px 0 8px", fontWeight: 700 }}>
                                Operational health dashboard
                            </h1>
                            <p style={{ maxWidth: "560px", lineHeight: 1.65, color: "#475569" }}>
                                Monitor verification throughput, stay ahead of escalations, and keep a pulse on platform activity
                                across students, landlords and listings.
                            </p>
                        </div>
                        <div style={{ ...cardStyles, width: "min(320px, 100%)", display: "grid", gap: "14px" }}>
                            <div style={{ display: "flex", alignItems: "center", gap: "14px" }}>
                                <FaClipboardCheck size={28} color="#2563eb" />
                                <div>
                                    <span style={{ color: "#94a3b8", fontSize: "13px" }}>Verification success rate</span>
                                    <h3 style={{ margin: 0, fontSize: "28px" }}>94.7%</h3>
                                </div>
                            </div>
                            {renderProgressBar(94.7, "linear-gradient(135deg, #2563eb, #60a5fa)")}
                            <div style={{ display: "flex", justifyContent: "space-between", fontSize: "13px", color: "#64748b" }}>
                                <span>Target: 92%</span>
                                <span>+2.7 pts vs last month</span>
                            </div>
                            <button
                                type="button"
                                style={{
                                    marginTop: "6px",
                                    padding: "10px 14px",
                                    borderRadius: "12px",
                                    border: "1px solid rgba(37, 99, 235, 0.35)",
                                    background: "rgba(59, 130, 246, 0.08)",
                                    color: "#1d4ed8",
                                    fontWeight: 600,
                                    cursor: "pointer",
                                }}
                            >
                                View verification rules
                            </button>
                        </div>
                    </header>

                    <section style={{ ...cardStyles, padding: "24px 28px" }}>
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: "16px" }}>
                            <div>
                                <div style={badgeStyles}>Live platform stats</div>
                                <h2 style={{ margin: "12px 0 4px", fontSize: "24px" }}>Current system overview</h2>
                                <p style={{ margin: 0, color: "#475569" }}>{occupancyText}</p>
                            </div>
                            <div style={{ color: "#64748b", fontSize: "14px" }}>
                                {overview?.generatedAt
                                    ? `Updated ${new Date(overview.generatedAt).toLocaleString()}`
                                    : isOverviewLoading
                                        ? "Fetching metrics..."
                                        : null}
                            </div>
                        </div>

                        {overviewError && <div style={overviewErrorStyles}>{overviewError}</div>}

                        {!overviewError && (
                            <div style={overviewGridStyles}>
                                {isOverviewLoading && overviewMetrics.length === 0 ? (
                                    <div style={loadingPillStyles}>Loading live metrics...</div>
                                ) : (
                                    overviewMetrics.map((metric) => (
                                        <div key={metric.label} style={overviewMetricStyles}>
                                            <div>{metric.icon}</div>
                                            <div style={{ fontSize: "28px", fontWeight: 700 }}>{metric.value}</div>
                                            <div style={{ color: "#475569", fontSize: "14px" }}>{metric.label}</div>
                                        </div>
                                    ))
                                )}
                            </div>
                        )}
                    </section>

                    <section style={{ ...cardStyles, padding: "24px 28px" }}>
                        <div
                            style={{
                                display: "flex",
                                flexWrap: "wrap",
                                justifyContent: "space-between",
                                alignItems: "center",
                                gap: "16px",
                            }}
                        >
                            <div>
                                <h2 style={{ margin: "0 0 6px", fontSize: "22px" }}>{headline} at a glance</h2>
                                <p style={{ margin: 0, color: "#64748b" }}>
                                    Consolidated performance indicators across registrations, verification and compliance.
                                </p>
                            </div>
                            <div
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: "10px",
                                    padding: "10px 16px",
                                    borderRadius: "12px",
                                    background: "#f8fbff",
                                    border: "1px solid rgba(148, 163, 184, 0.3)",
                                }}
                            >
                                <FaClock size={15} color="#64748b" />
                                <select
                                    value={timeframe}
                                    onChange={(event) => setTimeframe(event.target.value)}
                                    style={{
                                        border: "none",
                                        background: "transparent",
                                        outline: "none",
                                        fontSize: "15px",
                                        color: "#0f172a",
                                        fontWeight: 600,
                                        cursor: "pointer",
                                    }}
                                >
                                    <option value="daily">Today</option>
                                    <option value="weekly">This week</option>
                                    <option value="monthly">This month</option>
                                </select>
                            </div>
                        </div>

                        <div
                            style={{
                                marginTop: "24px",
                                display: "grid",
                                gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                                gap: "18px",
                            }}
                        >
                            {summary.map((item) => (
                                <div
                                    key={item.title}
                                    style={{
                                        border: "1px solid rgba(226, 232, 240, 0.8)",
                                        borderRadius: "18px",
                                        padding: "18px",
                                        background: "linear-gradient(135deg, rgba(248, 250, 255, 0.85), rgba(229, 236, 255, 0.65))",
                                        display: "grid",
                                        gap: "12px",
                                    }}
                                >
                                    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                                        <div
                                            style={{
                                                width: "44px",
                                                height: "44px",
                                                borderRadius: "50%",
                                                display: "grid",
                                                placeItems: "center",
                                                background: "rgba(59, 130, 246, 0.08)",
                                            }}
                                        >
                                            {item.icon}
                                        </div>
                                        <div>
                                            <span style={{ fontSize: "13px", color: "#64748b" }}>{item.title}</span>
                                            <h3 style={{ margin: "6px 0 0", fontSize: "24px" }}>{item.value}</h3>
                                        </div>
                                    </div>
                                    <span
                                        style={{
                                            fontSize: "13px",
                                            fontWeight: 600,
                                            color: item.change.startsWith("-") ? "#dc2626" : "#16a34a",
                                        }}
                                    >
                                        {item.change} vs previous {timeframe === "daily" ? "day" : timeframe === "weekly" ? "week" : "month"}
                                    </span>
                                </div>
                            ))}
                        </div>

                        <div style={{ marginTop: "26px", display: "grid", gap: "12px" }}>
                            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                <FaChartLine size={18} color="#2563eb" />
                                <strong style={{ fontSize: "16px" }}>Verification throughput trend</strong>
                            </div>
                            <div
                                style={{
                                    display: "grid",
                                    gridTemplateColumns: `repeat(${trend.length}, minmax(0, 1fr))`,
                                    gap: "16px",
                                    alignItems: "end",
                                    minHeight: "160px",
                                }}
                            >
                                {trend.map((point) => (
                                    <div key={point.label} style={{ display: "grid", gap: "12px", justifyItems: "center" }}>
                                        <div
                                            style={{
                                                height: `${Math.max(point.total / (trend[trend.length - 1].total || 1), 0.08) * 140}px`,
                                                width: "100%",
                                                borderRadius: "16px 16px 12px 12px",
                                                background: "linear-gradient(180deg, rgba(37, 99, 235, 0.85), rgba(96, 165, 250, 0.7))",
                                                boxShadow: "0 16px 24px rgba(37, 99, 235, 0.25)",
                                            }}
                                        />
                                        <span style={{ fontSize: "12px", color: "#64748b" }}>{point.label}</span>
                                        <span style={{ fontSize: "13px", fontWeight: 600, color: "#1e293b" }}>{point.total}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </section>

                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
                            gap: "24px",
                        }}
                    >
                        <section style={cardStyles}>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "14px" }}>
                                <h2 style={{ margin: 0, fontSize: "22px" }}>Verification queue</h2>
                                <div style={{ display: "flex", alignItems: "center", gap: "8px", color: "#64748b", fontSize: "13px" }}>
                                    <FaSearch size={14} />
                                    Smart triage enabled
                                </div>
                            </div>
                            <div style={{ display: "grid", gap: "12px" }}>
                                {verificationQueue.map((item) => (
                                    <div
                                        key={item.id}
                                        style={{
                                            border: "1px solid rgba(226, 232, 240, 0.8)",
                                            borderRadius: "16px",
                                            padding: "16px",
                                            display: "grid",
                                            gap: "8px",
                                            background: "linear-gradient(135deg, rgba(248, 250, 255, 0.85), rgba(229, 236, 255, 0.65))",
                                        }}
                                    >
                                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                            <strong style={{ fontSize: "15px" }}>{item.applicant}</strong>
                                            <span
                                                style={{
                                                    padding: "6px 12px",
                                                    borderRadius: "999px",
                                                    fontSize: "12px",
                                                    fontWeight: 600,
                                                    background: statusColors[item.status]?.bg,
                                                    color: statusColors[item.status]?.color,
                                                }}
                                            >
                                                {item.status}
                                            </span>
                                        </div>
                                        <div style={{ display: "flex", justifyContent: "space-between", color: "#64748b", fontSize: "13px" }}>
                                            <span>{item.type}</span>
                                            <span>{item.submitted}</span>
                                        </div>
                                        <div
                                            style={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                fontSize: "13px",
                                                color: "#475569",
                                            }}
                                        >
                                            <span>Risk: {item.riskScore}</span>
                                            <button
                                                type="button"
                                                style={{
                                                    padding: "8px 12px",
                                                    borderRadius: "10px",
                                                    border: "1px solid rgba(37, 99, 235, 0.4)",
                                                    background: "rgba(59, 130, 246, 0.08)",
                                                    color: "#1d4ed8",
                                                    fontWeight: 600,
                                                    cursor: "pointer",
                                                }}
                                            >
                                                Review dossier
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <section style={{ ...cardStyles, display: "grid", gap: "20px" }}>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                <h2 style={{ margin: 0, fontSize: "20px" }}>Activity feed</h2>
                                <FaDatabase size={16} color="#94a3b8" />
                            </div>
                            <div style={{ display: "grid", gap: "16px" }}>
                                {activityFeed.map((item) => (
                                    <div key={item.time} style={{ display: "grid", gap: "4px" }}>
                                        <span style={{ fontSize: "12px", color: "#94a3b8" }}>{item.time}</span>
                                        <div style={{ fontWeight: 600 }}>{item.actor}</div>
                                        <div style={{ color: "#475569" }}>{item.action}</div>
                                        <div style={{ fontSize: "13px", color: "#64748b" }}>{item.context}</div>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <section style={{ ...cardStyles, display: "grid", gap: "18px" }}>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                <h2 style={{ margin: 0, fontSize: "20px" }}>Compliance coverage</h2>
                                <FaUserShield size={18} color="#16a34a" />
                            </div>
                            <div style={{ display: "grid", gap: "14px" }}>
                                {complianceAreas.map((area) => (
                                    <div key={area.name} style={{ display: "grid", gap: "6px" }}>
                                        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "13px" }}>
                                            <span>{area.name}</span>
                                            <span>{area.completion}%</span>
                                        </div>
                                        {renderProgressBar(area.completion, "linear-gradient(135deg, #16a34a, #4ade80)")}
                                    </div>
                                ))}
                            </div>
                        </section>
                    </div>
                </div>
            </main>
        </div>
    );
}

const overviewGridStyles = {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
    gap: "18px",
    marginTop: "24px",
};

const overviewMetricStyles = {
    display: "flex",
    flexDirection: "column",
    gap: "8px",
    padding: "16px",
    borderRadius: "18px",
    backgroundColor: "rgba(241, 245, 249, 0.7)",
    border: "1px solid rgba(148, 163, 184, 0.18)",
};

const loadingPillStyles = {
    gridColumn: "1 / -1",
    padding: "16px",
    borderRadius: "14px",
    textAlign: "center",
    backgroundColor: "rgba(59, 130, 246, 0.12)",
    color: "#1d4ed8",
    fontWeight: 600,
};

const overviewErrorStyles = {
    marginTop: "24px",
    padding: "16px",
    borderRadius: "14px",
    backgroundColor: "rgba(239, 68, 68, 0.12)",
    color: "#b91c1c",
};

export default Dashboard;
