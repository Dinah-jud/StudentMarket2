package za.ac.student_trade.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Authentication/Login (Read-only or initial auth)
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/student/forgot-password/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/product/getAllProducts").permitAll()
                
                // Student endpoints - Require STUDENT or higher role with proper authentication
                .requestMatchers(HttpMethod.GET, "/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/student/**").hasAnyRole("ADMIN", "SUPER_ADMIN") // Only admins can delete students
                
                // Product endpoints - Authenticated users only for state-changing operations
                .requestMatchers(HttpMethod.GET, "/api/product/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/product/**").hasRole("STUDENT") // Only students can create products
                .requestMatchers(HttpMethod.PUT, "/api/product/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.PATCH, "/api/product/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasAnyRole("ADMIN", "SUPER_ADMIN") // Only admins can delete
                
                // Transaction endpoints - Authenticated users only
                .requestMatchers(HttpMethod.GET, "/api/transaction/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/transaction/**").hasRole("STUDENT") // Only students can create transactions
                .requestMatchers(HttpMethod.PUT, "/api/transaction/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/transaction/**").hasRole("SUPER_ADMIN")
                
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // Super Admin endpoints - Only SUPER_ADMIN
                .requestMatchers("/api/superadmin/**").hasRole("SUPER_ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}