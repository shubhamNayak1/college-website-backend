package com.baseras.portal.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            @Value("${baseras.jwt.secret}") String secret,
            @Value("${baseras.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${baseras.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String issueAccessToken(String userId, String role) {
        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .claim("typ", "access")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofSeconds(accessTtlSeconds))))
                .signWith(key)
                .compact();
    }

    public String issueRefreshToken(String userId) {
        return Jwts.builder()
                .subject(userId)
                .claim("typ", "refresh")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(Duration.ofSeconds(refreshTtlSeconds))))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
