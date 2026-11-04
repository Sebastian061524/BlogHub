package com.sebastian.bloghub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.time.Instant;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public JwtUtil(
            @Value("${jwt.secret-base64}") String secretBase64,
            @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Integer userId){
        if(userId == null || userId <= 0){
            throw new IllegalArgumentException("用户ID必须为正整数");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token){
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getExpirationSeconds() {
        return expirationSeconds;
    }

    public Integer getUserId(String token){
        Claims claims = parseToken(token);
        String userId = claims.getSubject();
        return Integer.parseInt(userId);
    }
}
