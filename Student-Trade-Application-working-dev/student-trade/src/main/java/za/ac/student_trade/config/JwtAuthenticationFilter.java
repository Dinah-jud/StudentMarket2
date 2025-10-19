package za.ac.student_trade.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import za.ac.student_trade.domain.Role;
import za.ac.student_trade.util.JwtUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Define sensitive endpoints that require additional security checks
    private static final List<String> SENSITIVE_ENDPOINTS = Arrays.asList(
        "/api/product/create",
        "/api/product/update",
        "/api/product/delete",
        "/api/transaction/create",
        "/api/student/update",
        "/api/student/delete",
        "/api/admin",
        "/api/superadmin"
    );

    // Define read-only endpoints that require minimal authentication
    private static final List<String> READ_ONLY_ENDPOINTS = Arrays.asList(
        "/api/product/getAllProducts",
        "/api/product/read"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("JwtAuthenticationFilter: Processing " + method + " request to " + requestURI);

        try {
            String jwt = getJwtFromRequest(request);

            System.out.println("JwtAuthenticationFilter: JWT token " + (jwt != null ? "found" : "not found"));

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);
                String roleStr = jwtUtil.getRoleFromToken(jwt);
                String userId = jwtUtil.getUserIdFromToken(jwt);

                System.out.println("JwtAuthenticationFilter: Valid token for user=" + username + ", role=" + roleStr
                        + ", userId=" + userId);

                Role role = Role.valueOf(roleStr);

                // Additional security checks for sensitive endpoints
                if (isSensitiveEndpoint(requestURI)) {
                    System.out.println("JwtAuthenticationFilter: Sensitive endpoint detected, performing additional checks");
                    
                    // Temporarily disable token age check for debugging
                    /*
                    // Check if token is recent (not older than 1 hour for sensitive operations)
                    if (jwtUtil.isTokenOlderThan(jwt, 3600000)) { // 1 hour in milliseconds
                        System.out.println("JwtAuthenticationFilter: Token too old for sensitive operation");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\":\"Token too old for sensitive operation. Please re-authenticate.\"}");
                        return;
                    }
                    */

                    // Additional role validation for sensitive endpoints
                    if (!isAuthorizedForSensitiveOperation(requestURI, method, role)) {
                        System.out.println("JwtAuthenticationFilter: Insufficient permissions for sensitive operation");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"error\":\"Insufficient permissions for this operation.\"}");
                        return;
                    }
                }

                CustomUserPrincipal userPrincipal = new CustomUserPrincipal(username, "", userId, role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal, null, userPrincipal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("JwtAuthenticationFilter: Authentication set in SecurityContext");
            } else {
                System.out.println("JwtAuthenticationFilter: Token validation failed or no token provided");
                
                // For sensitive endpoints, ensure we return 401 if no valid token
                if (isSensitiveEndpoint(requestURI) && !isPublicEndpoint(requestURI, method)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\":\"Authentication required for this operation.\"}");
                    return;
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            System.err.println("JwtAuthenticationFilter error: " + ex.getMessage());
            
            // For sensitive endpoints, return error instead of continuing
            if (isSensitiveEndpoint(request.getRequestURI())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Authentication error.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isSensitiveEndpoint(String requestURI) {
        return SENSITIVE_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }

    private boolean isPublicEndpoint(String requestURI, String method) {
        if ("GET".equals(method) && READ_ONLY_ENDPOINTS.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }

        return requestURI.startsWith("/api/auth/") ||
                requestURI.startsWith("/api/student/forgot-password/send-otp") ||
                requestURI.startsWith("/api/student/forgot-password/reset") ||
                (requestURI.equals("/api/product/getAllProducts") && "GET".equals(method));
    }

    private boolean isAuthorizedForSensitiveOperation(String requestURI, String method, Role role) {
        // Super admin has access to everything
        if (role == Role.SUPER_ADMIN) {
            return true;
        }

        // Admin can access admin endpoints and some sensitive operations
        if (role == Role.ADMIN) {
            return requestURI.startsWith("/api/admin/") || 
                   requestURI.startsWith("/api/product/delete") ||
                   requestURI.startsWith("/api/student/delete");
        }

        // Students can only access their own data and create/update their products
        if (role == Role.STUDENT) {
            return requestURI.startsWith("/api/product/create") ||
                   requestURI.startsWith("/api/product/update") ||
                   requestURI.startsWith("/api/transaction/create") ||
                   (requestURI.startsWith("/api/student/") && !"DELETE".equals(method));
        }

        return false;
    }
}