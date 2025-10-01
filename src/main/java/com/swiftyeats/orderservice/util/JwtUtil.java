package com.swiftyeats.orderservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "swiftyeats_secret";
    private static final long EXPIRATION_TIME = 86400000; // 1 day


    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    public static String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public static boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private static Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private static boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}