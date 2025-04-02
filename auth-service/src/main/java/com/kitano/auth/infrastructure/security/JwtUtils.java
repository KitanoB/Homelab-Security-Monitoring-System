package com.kitano.auth.infrastructure.security;

import com.kitano.auth.model.HomelabUserDTO;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class JwtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret:homelabSecretKey12345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expirationMs:3600000}") // 1 hour
    private long jwtExpirationMs;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private SecretKey key;

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> expirationMap = new ConcurrentHashMap<>();



    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("jwtSecret must be configured");
        }
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("JwtUtils initialized with secure key.");
        scheduler.scheduleAtFixedRate(this::removeExpiredTokens, 1, 1, java.util.concurrent.TimeUnit.HOURS);
    }

    public String generateToken(HomelabUserDTO user) {
        return Jwts.builder()
                .subject(user.username())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {

        if (token == null || token.isEmpty()) {
            return false;
        }
        if (isBlacklisted(token)) {
            return false;
        }

        try {
            // check if token is malformed
            if (token.length() < 10) {
                LOGGER.debug("Token is malformed.");
                return false;
            }

            // check if token is valid
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

            // check is token expired
            Date expiration = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            if (expiration != null && expiration.before(new Date())) {
                LOGGER.debug("Token is expired.");
                return false;
            }

            LOGGER.debug("Token is Valid.");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.debug("Token is Invalid: {}", e.getMessage());
            return false;
        }
    }

    public void blacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        if (blacklist.contains(token)) {
            return;
        }

        expirationMap.put(token, System.currentTimeMillis() + jwtExpirationMs);
        LOGGER.debug("Token blacklisted: {}", token);
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        boolean blackListed =  blacklist.contains(token);
        LOGGER.debug("Token is blacklisted: {}", blackListed);
        return blackListed;
    }

    private void removeExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        expirationMap.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        blacklist.removeIf(token -> !expirationMap.containsKey(token));
    }
}