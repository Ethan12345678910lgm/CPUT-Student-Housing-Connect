const inferDefaultBaseUrl = () => {
    if (typeof window === "undefined" || !window?.location) {
        return "http://localhost:8080/api";
    }

    const { protocol, hostname, port, origin } = window.location;

    const isLocalHost = ["localhost", "127.0.0.1", "::1"].some((value) =>
        hostname?.toLowerCase()?.includes(value),
    );

    // When the frontend runs locally (for example via `npm start` or other dev servers
    // such as Vite) it typically executes on a port other than 8080 while the Spring Boot
    // backend continues to listen on 8080. Without redirecting those requests the browser
    // will call the dev server itself (e.g. http://localhost:3000/api or
    // http://localhost:5173/api) which responds with an HTML error page instead of the JSON
    // payloads expected by the app.
    if (isLocalHost && port && port !== "8080") {
        return `${protocol}//${hostname}:8080/api`;
    }

    return `${origin}/api`;
};

const normaliseBaseUrl = (url) => {
    if (!url) {
        return url;
    }

    return url.replace(/\/+$/, "");
};

const readEnvironmentValue = (...keys) => {
    try {
        const metaEnv = import.meta.env || {};
        for (const key of keys) {
            if (Object.prototype.hasOwnProperty.call(metaEnv, key) && metaEnv[key] !== undefined) {
                return metaEnv[key];
            }
        }
    } catch (error) {
        // `import.meta` is not available (for example when bundled by CRA).
    }

    if (typeof process !== "undefined" && process?.env) {
        for (const key of keys) {
            if (process.env[key] !== undefined) {
                return process.env[key];
            }
        }    }

    return undefined;
};

    const readEnvironmentBaseUrl = () => readEnvironmentValue("VITE_API_BASE_URL", "REACT_APP_API_BASE_URL");

    const parsePositiveInteger = (value, fallback) => {
        if (value === undefined || value === null) {
            return fallback;
        }

        const parsed = Number.parseInt(value, 10);
        if (!Number.isFinite(parsed) || parsed <= 0) {
            return fallback;
        }

        return parsed;
    };


    const API_BASE_URL = normaliseBaseUrl(readEnvironmentBaseUrl()) || normaliseBaseUrl(inferDefaultBaseUrl());
const buildUrl = (path) => {
    if (!path.startsWith("/")) {
        return `${API_BASE_URL}/${path}`;
    }
    return `${API_BASE_URL}${path}`;
};

const defaultHeaders = {
    "Content-Type": "application/json",
};

const createError = (message, code, details) => {
    const error = new Error(message);
    if (code) {
        error.code = code;
    }
    if (details && typeof details === "object") {
        Object.entries(details).forEach(([key, value]) => {
            if (value !== undefined) {
                error[key] = value;
            }
        });
    }
    return error;
};

    const DEFAULT_TIMEOUT_MS = parsePositiveInteger(
        readEnvironmentValue("VITE_API_TIMEOUT_MS", "REACT_APP_API_TIMEOUT_MS"),
        15000,
    );
    const MAX_TIMEOUT_RETRIES = parsePositiveInteger(
        readEnvironmentValue("VITE_API_TIMEOUT_RETRIES", "REACT_APP_API_TIMEOUT_RETRIES"),
        1,
    );
    const MAX_TIMEOUT_LIMIT_MS = parsePositiveInteger(
        readEnvironmentValue("VITE_API_MAX_TIMEOUT_MS", "REACT_APP_API_MAX_TIMEOUT_MS"),
        45000,
    );
    const TIMEOUT_BACKOFF_MULTIPLIER = Math.max(
        1,
        parsePositiveInteger(
            readEnvironmentValue("VITE_API_TIMEOUT_BACKOFF", "REACT_APP_API_TIMEOUT_BACKOFF"),
            2,
        ),
    );
    const TIMEOUT_RETRY_DELAY_MS = parsePositiveInteger(
        readEnvironmentValue("VITE_API_TIMEOUT_RETRY_DELAY_MS", "REACT_APP_API_TIMEOUT_RETRY_DELAY_MS"),
        750,
    );

    const wait = (durationMs) =>
        new Promise((resolve) => {
            if (!Number.isFinite(durationMs) || durationMs <= 0) {
                resolve();
                return;
            }
            setTimeout(resolve, durationMs);
        });

const parseResponse = async (response) => {
    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers?.get("content-type") ?? "";

    const text = await response.text();
    if (!text) {
        return null;
    }

    if (contentType.includes("application/json")) {
        try {
            return JSON.parse(text);
        } catch (error) {
            throw new Error("Received an unexpected response from the server.");
        }
    }

    return text;
};

const request = async (path, options = {}) => {
    const url = buildUrl(path);
    const {
        timeout,
        headers,
        body,
        retryOnTimeout = true,
        signal: externalSignal,
        ...rest
    } = options;

    const resolvedTimeout = Number.isFinite(timeout) && timeout > 0 ? timeout : DEFAULT_TIMEOUT_MS;
    const totalAttempts = retryOnTimeout ? Math.max(1, MAX_TIMEOUT_RETRIES + 1) : 1;

    const baseConfig = {        mode: "cors",
        cache: "no-store",
        headers: {
            ...defaultHeaders,
            ...(headers || {}),
        },
        ...rest,
    };

    if (body !== undefined) {
        baseConfig.body = typeof body === "string" ? body : JSON.stringify(body);
    }

    let currentTimeout = resolvedTimeout;


    for (let attempt = 0; attempt < totalAttempts; attempt += 1) {
        const isLastAttempt = attempt === totalAttempts - 1;
        const controller = typeof AbortController !== "undefined" ? new AbortController() : null;
        let timeoutId;
        let didTimeout = false;
        let externalAbortCleanup;

        const config = { ...baseConfig };


        if (controller) {
            config.signal = controller.signal;

            if (Number.isFinite(currentTimeout) && currentTimeout > 0) {
                timeoutId = setTimeout(() => {
                    didTimeout = true;
                    controller.abort();
                }, currentTimeout);
            }

            if (externalSignal) {
                if (externalSignal.aborted) {
                    controller.abort();
                } else {
                    const abortHandler = () => controller.abort();
                    externalSignal.addEventListener("abort", abortHandler, { once: true });
                    externalAbortCleanup = () => externalSignal.removeEventListener("abort", abortHandler);
                }
            }
        } else if (externalSignal) {
            config.signal = externalSignal;
        }

        try {
            const response = await fetch(url, config);

            if (!response.ok) {
                const errorBody = await parseResponse(response);
                const message =
                    (typeof errorBody === "string" && errorBody.trim()) ||
                    errorBody?.message ||
                    `Request failed with status ${response.status}`;
                throw createError(message, "HTTP_ERROR", {
                    status: response.status,
                    responseBody: errorBody,
                });            }

            return parseResponse(response);
        } catch (error) {
            if (error?.name === "AbortError") {
                if (didTimeout) {
                    if (!isLastAttempt) {
                        if (timeoutId) {
                            clearTimeout(timeoutId);
                            timeoutId = undefined;
                        }
                        if (externalAbortCleanup) {
                            externalAbortCleanup();
                            externalAbortCleanup = undefined;
                        }
                        const nextTimeout = Math.min(
                            currentTimeout * TIMEOUT_BACKOFF_MULTIPLIER,
                            MAX_TIMEOUT_LIMIT_MS,
                        );
                        currentTimeout = Math.max(nextTimeout, currentTimeout);
                        await wait(TIMEOUT_RETRY_DELAY_MS);
                        continue;
                    }
                    throw createError("The request timed out. Please try again.", "TIMEOUT", {
                        timeout: currentTimeout,
                        attempts: attempt + 1,
                    });
                }

                throw createError("The request was aborted.", "ABORTED");
            }
            if (error instanceof TypeError) {
                throw createError(
                    "Unable to reach the server. Please check your connection and try again.",
                    "NETWORK",
                );
            }
            throw error;
        } finally {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
            if (externalAbortCleanup) {
                externalAbortCleanup();
            }
        }
    }
    throw createError("Unable to complete the request. Please try again.", "UNKNOWN");
};

const get = (path, options = {}) => request(path, { ...options, method: "GET" });
const post = (path, body, options = {}) => request(path, { ...options, method: "POST", body });
const put = (path, body, options = {}) => request(path, { ...options, method: "PUT", body });
const del = (path, options = {}) => request(path, { ...options, method: "DELETE" });

const apiClient = {
    get,
    post,
    put,
    delete: del,
};

export default apiClient;
export { get, post, put, del as delete, API_BASE_URL };
