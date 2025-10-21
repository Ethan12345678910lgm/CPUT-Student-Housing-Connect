package co.za.cput.config;

public final class CorsConstants {

    private CorsConstants() {
        // Utility class
    }

    public static final String[] LOCAL_DEVELOPMENT_ORIGIN_PATTERNS = {
            "http://localhost",
            "https://localhost",
            "http://localhost:*",
            "https://localhost:*",
            "http://127.0.0.1",
            "https://127.0.0.1",
            "http://127.0.0.1:*",
            "https://127.0.0.1:*",
            "http://[::1]",
            "https://[::1]",
            "http://[::1]:*",
            "https://[::1]:*"
    };
}