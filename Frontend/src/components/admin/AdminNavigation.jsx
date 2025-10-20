import React, { useCallback, useMemo } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { FaCheckCircle, FaDoorOpen, FaHome, FaListAlt, FaUserShield } from "react-icons/fa";
import AppName from "../shared/AppName";
import { getCurrentUser, logout } from "../../services/authService";

const adminNavItems = [
    { path: "/admin/dashboard", label: "Dashboard", icon: FaHome },
    { path: "/admin/verify-landlords", label: "Verify landlords", icon: FaCheckCircle },
    { path: "/admin/verify-listings", label: "Verify listings", icon: FaListAlt },
    { path: "/admin/signup", label: "Manage admins", icon: FaUserShield, superAdminOnly: true },
];

function AdminNavigation() {
    const navigate = useNavigate();
    const user = useMemo(() => getCurrentUser(), []);
    const visibleNavItems = useMemo(() => {
        return adminNavItems.filter((item) => !item.superAdminOnly || user?.superAdmin);
    }, [user]);

    const handleLogout = useCallback(() => {
        logout();
        navigate("/login", { replace: true });
    }, [navigate]);

    const displayName = user?.name || "Administrator";

    return (
        <header className="admin-nav-shell">
            <div className="admin-nav-bar">
                <Link to="/admin/dashboard" className="admin-nav-brand" aria-label="Administrator home">
                    <AppName compact />
                </Link>

                <nav aria-label="Administrator navigation" className="admin-nav-links">
                    {visibleNavItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) => `admin-nav-link${isActive ? " active" : ""}`}
                        >
                            <item.icon aria-hidden="true" />
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>

                <div className="admin-nav-actions">
                    <span className="admin-nav-user">{displayName}</span>
                    <button type="button" className="admin-signout" onClick={handleLogout}>
                        <FaDoorOpen aria-hidden="true" />
                        <span>Sign out</span>
                    </button>
                </div>
            </div>
        </header>
    );
}

export default AdminNavigation;
