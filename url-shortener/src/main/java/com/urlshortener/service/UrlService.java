package com.urlshortener.service;

import com.urlshortener.dto.UrlDto;
import com.urlshortener.model.Url;
import com.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.default-expiry-days:30}")
    private int defaultExpiryDays;

    @Transactional
    public UrlDto.ShortenResponse shorten(UrlDto.ShortenRequest request) {
        // Use custom alias if provided
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            if (urlRepository.existsByCustomAlias(request.getCustomAlias())) {
                throw new IllegalArgumentException("Custom alias '" + request.getCustomAlias() + "' is already taken");
            }
        }

        // Generate deterministic short code
        String shortCode = generateUniqueShortCode(request.getOriginalUrl());

        // Check if this URL was already shortened (idempotent)
        var existing = urlRepository.findByShortCode(shortCode);
        if (existing.isPresent() && existing.get().getOriginalUrl().equals(request.getOriginalUrl())) {
            return toResponse(existing.get());
        }

        int expiryDays = request.getExpiryDays() != null ? request.getExpiryDays() : defaultExpiryDays;

        Url url = Url.builder()
            .originalUrl(request.getOriginalUrl())
            .shortCode(request.getCustomAlias() != null ? request.getCustomAlias() : shortCode)
            .customAlias(request.getCustomAlias())
            .expiresAt(LocalDateTime.now().plusDays(expiryDays))
            .build();

        Url saved = urlRepository.save(url);
        log.info("Created short URL: {} -> {}", saved.getShortCode(), saved.getOriginalUrl());
        return toResponse(saved);
    }

    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    @Transactional(readOnly = true)
    public String resolve(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Short URL not found: " + shortCode));

        if (!url.isActive()) {
            throw new IllegalStateException("This short URL has been deactivated");
        }
        if (url.isExpired()) {
            throw new IllegalStateException("This short URL has expired");
        }

        return url.getOriginalUrl();
    }

    @Transactional
    public void trackClick(String shortCode) {
        urlRepository.incrementClickCount(shortCode);
    }

    @Transactional(readOnly = true)
    public UrlDto.UrlAnalytics getAnalytics(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Short URL not found: " + shortCode));

        return UrlDto.UrlAnalytics.builder()
            .shortCode(url.getShortCode())
            .originalUrl(url.getOriginalUrl())
            .clickCount(url.getClickCount())
            .createdAt(url.getCreatedAt())
            .expiresAt(url.getExpiresAt())
            .active(url.isActive())
            .build();
    }

    @CacheEvict(value = "urls", key = "#shortCode")
    @Transactional
    public void deactivate(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Short URL not found: " + shortCode));
        url.setActive(false);
        urlRepository.save(url);
        log.info("Deactivated short URL: {}", shortCode);
    }

    @Scheduled(cron = "0 0 2 * * *") // daily at 2AM
    @Transactional
    public void cleanupExpiredUrls() {
        int deleted = urlRepository.deleteExpiredUrls(LocalDateTime.now());
        log.info("Cleaned up {} expired URLs", deleted);
    }

    private String generateUniqueShortCode(String originalUrl) {
        String code = base62Encoder.encode(originalUrl);
        int attempts = 0;
        while (urlRepository.existsByShortCode(code) && attempts < 5) {
            code = base62Encoder.encodeRandom();
            attempts++;
        }
        return code;
    }

    private UrlDto.ShortenResponse toResponse(Url url) {
        return UrlDto.ShortenResponse.builder()
            .shortUrl(baseUrl + "/" + url.getShortCode())
            .shortCode(url.getShortCode())
            .originalUrl(url.getOriginalUrl())
            .expiresAt(url.getExpiresAt())
            .createdAt(url.getCreatedAt())
            .build();
    }
}
