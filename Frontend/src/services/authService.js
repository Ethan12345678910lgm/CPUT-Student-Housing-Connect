import apiClient from "./apiClient";

const AUTH_STORAGE_KEY = "cput_shc_active_user";

const persistUser = (user) => {
    if (typeof window === "undefined") return;
    window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(user));
};

const clearPersistedUser = () => {
    if (typeof window === "undefined") return;
    window.localStorage.removeItem(AUTH_STORAGE_KEY);
};

const resolveStoredUser = () => {
    if (typeof window === "undefined") return null;
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch (error) {
        window.localStorage.removeItem(AUTH_STORAGE_KEY);
        return null;
    }
};

export const login = async (role, email, password) => {
    if (!email || !password) {
        throw new Error("Email and password are required.");
    }

    const payload = {
        email: email.trim().toLowerCase(),
        password,
        ...(role
            ? {
                role: role.trim().toLowerCase(),
            }
            : {}),
    };

    const response = await apiClient.post("/auth/login", payload);

    if (!response || !response.authenticated) {
        throw new Error(response?.message || "Unable to authenticate with the supplied credentials.");
    }

    const resolvedRole = response.role?.toLowerCase();

    if (role && resolvedRole && role !== resolvedRole) {
        throw new Error("The selected role does not match this account.");
    }

    const sessionUser = {
        role: resolvedRole,
        email: response.email,
        name: [response.firstName, response.lastName].filter(Boolean).join(" ").trim(),
        verified: response.verified,
        userId: response.userId,
        adminRoleStatus: response.adminRoleStatus,
        superAdmin: Boolean(response.superAdmin),
    };

    persistUser(sessionUser);
    return sessionUser;
};

export const logout = () => {
    clearPersistedUser();
};

export const getCurrentUser = () => resolveStoredUser();

export const isAuthenticated = () => Boolean(resolveStoredUser());

const authService = {
    login,
    logout,
    getCurrentUser,
    isAuthenticated,
};
export default authService;