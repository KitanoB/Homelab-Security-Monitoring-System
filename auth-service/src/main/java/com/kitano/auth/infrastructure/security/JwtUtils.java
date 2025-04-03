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
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utility class for generating and validating JWT tokens.
 * It uses a secret key to sign the tokens and provides methods to check their validity.
 * The class also manages a blacklist of tokens that have been invalidated.
 */
@Component
public class JwtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> expirationMap = new ConcurrentHashMap<>();
    @Value("${jwt.secret:homelabSecretKey12345678901234567890}")
    private String jwtSecret;
    @Value("${jwt.expirationMs:3600000}") // 1 hour
    private long jwtExpirationMs;
    private SecretKey key;

    /**
     * Initializes the JwtUtils class by configuring the secret key and starting a scheduled task
     * to remove expired tokens from the blacklist.
     */
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

    /**
     * Generates a JWT token for the given user.
     * <p>
     * This method creates a JWT token with the user's username as the subject,
     * the current date as the issued date, and an expiration date based on the configured expiration time.
     * The token is signed with the secret key.
     *
     * @param user The user for whom the token is generated.
     * @return The generated JWT token.
     */
    public String generateToken(HomelabUserDTO user) {
        return Jwts.builder()
                .subject(user.username())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token The JWT token.
     * @return The username extracted from the token.
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates the given JWT token.
     * <p>
     * This method checks if the token is not null, not empty, not blacklisted,
     * and if it is well-formed. It also checks if the token is expired.
     *
     * @param token The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     */
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

    /**
     * Blacklists the given JWT token.
     * <p>
     * This method adds the token to the blacklist and sets its expiration time.
     * If the token is already blacklisted, it does nothing.
     *
     * @param token The JWT token to blacklist.
     */
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

    /**
     * Checks if the given JWT token is blacklisted.
     * <p>
     * This method checks if the token is present in the blacklist.
     *
     * @param token The JWT token to check.
     * @return true if the token is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(String token) {
        boolean blackListed = blacklist.contains(token);
        LOGGER.debug("Token is blacklisted: {}", blackListed);
        return blackListed;
    }

    /**
     * Removes expired tokens from the blacklist.
     * <p>
     * This method iterates over the expiration map and removes tokens that have expired.
     * It also removes tokens from the blacklist that are no longer present in the expiration map.
     */
    private void removeExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        expirationMap.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        blacklist.removeIf(token -> !expirationMap.containsKey(token));
    }
}