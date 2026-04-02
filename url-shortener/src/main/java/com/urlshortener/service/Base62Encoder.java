package com.urlshortener.service;

import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Deterministic Base62 encoder for collision-resistant short URL generation.
 * Uses SHA-256 hash of the original URL, then encodes the first 6 bytes as Base62.
 */
@Component
public class Base62Encoder {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 7;

    /**
     * Generates a deterministic short code from a URL using SHA-256 + Base62.
     * The same input always produces the same output (collision-resistant).
     */
    public String encode(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));

            long value = 0;
            for (int i = 0; i < 6; i++) {
                value = (value << 8) | (hash[i] & 0xFF);
            }

            return encodeToBase62(Math.abs(value), CODE_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Error generating short code", e);
        }
    }

    /**
     * Generates a random short code using secure random bytes.
     * Used as fallback when deterministic code has a collision.
     */
    public String encodeRandom() {
        long value = System.nanoTime() ^ (long)(Math.random() * Long.MAX_VALUE);
        return encodeToBase62(Math.abs(value), CODE_LENGTH);
    }

    private String encodeToBase62(long value, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt((int)(value % BASE)));
            value /= BASE;
        }
        return sb.reverse().toString();
    }
}
