package co.za.cput.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final List<String> DEFAULT_ALLOWED_ORIGIN_PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
    );

    @Bean
        public CorsConfigurationSource corsConfigurationSource(
                @Value("${app.security.cors.allowed-origin-patterns:}") String allowedOriginPatterns
    ) {
            List<String> resolvedPatterns = Arrays.stream(allowedOriginPatterns.split(","))
                    .map(String::trim)
                    .filter(pattern -> !pattern.isEmpty())
                    .toList();

            if (resolvedPatterns.isEmpty()) {
                resolvedPatterns = DEFAULT_ALLOWED_ORIGIN_PATTERNS;
            }

            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(resolvedPatterns);
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Bean
            public SecurityFilterChain filterChain(HttpSecurity http,
                    CorsConfigurationSource corsConfigurationSource) throws Exception {
                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Enable CORS
                        .csrf(csrf -> csrf.disable()) // Disable CSRF for Postman/React testing
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()//This allows all OPTIONS requests to pass through without authentication
                                .requestMatchers(
                                        "/UserAuthentication/login",           // login endpoint is public
                                        "/UserAuthentication/api/auth/signup/**", // legacy signup path
                                        "/HouseConnect/UserAuthentication/**",  // REST tests and new base path
                                        "/api/auth/**",                         // new consolidated auth base path
                                        "/Student/**",                           // student API is public
                                        "/HouseConnect/Student/**",
                                        "/Contact/**",
                                        "/HouseConnect/Contact/**",
                                        "/Landlord/**",
                                        "/HouseConnect/Landlord/**",
                                        "/Address/**",
                                        "/HouseConnect/Address/**",
                                        "/Accommodation/**",
                                        "/HouseConnect/Accommodation/**",
                                        "/Booking/**",
                                        "/HouseConnect/Booking/**",
                                        "/Review/**",
                                        "/HouseConnect/Review/**",
                                        "/Verification/**",
                                        "/HouseConnect/Verification/**",
                                        "/HouseConnect/Administrator/**",
                                        "/api/**",
                                        "/error"
                                ).permitAll()
                        .anyRequest().authenticated() // everything else requires login
                );

        return http.build();
    }
}