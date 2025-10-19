package za.ac.student_trade.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import za.ac.student_trade.domain.Role;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, Role role, String userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public boolean isTokenOlderThan(String token, long maxAgeMs) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date issuedAt = claims.getIssuedAt();
            long tokenAge = System.currentTimeMillis() - issuedAt.getTime();
            return tokenAge > maxAgeMs;
        } catch (Exception e) {
            System.err.println("Error checking token age: " + e.getMessage());
            return true; // Assume expired if we can't determine age
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            
            // Additional validation checks
            if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
                System.err.println("Invalid JWT token: Empty subject");
                return false;
            }
            
            if (claims.get("role") == null) {
                System.err.println("Invalid JWT token: Missing role claim");
                return false;
            }
            
            if (claims.get("userId") == null) {
                System.err.println("Invalid JWT token: Missing userId claim");
                return false;
            }
            
            return true;
        } catch (JwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}