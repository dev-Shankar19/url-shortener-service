package com.urlshortener.controller;

import com.urlshortener.dto.UrlDto;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "URL Shortener", description = "APIs for shortening, redirecting, and tracking URLs")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/shorten")
    @Operation(
        summary = "Shorten a URL",
        description = "Creates a short code for a given long URL. Supports custom aliases and expiry settings."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "URL shortened successfully",
            content = @Content(schema = @Schema(implementation = UrlDto.ShortenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid URL or alias",
            content = @Content(schema = @Schema(implementation = UrlDto.ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Custom alias already taken",
            content = @Content(schema = @Schema(implementation = UrlDto.ErrorResponse.class)))
    })
    public ResponseEntity<UrlDto.ShortenResponse> shorten(@Valid @RequestBody UrlDto.ShortenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shorten(request));
    }

    @GetMapping("/{shortCode}")
    @Operation(
        summary = "Redirect to original URL",
        description = "Resolves a short code and performs an HTTP 302 redirect to the original URL. Increments click count."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
        @ApiResponse(responseCode = "404", description = "Short code not found"),
        @ApiResponse(responseCode = "410", description = "URL expired or deactivated")
    })
    public void redirect(
        @Parameter(description = "Short code", example = "abc123") @PathVariable String shortCode,
        HttpServletResponse response
    ) throws Exception {
        String originalUrl = urlService.resolve(shortCode);
        urlService.trackClick(shortCode);
        response.sendRedirect(originalUrl);
    }

    @GetMapping("/api/analytics/{shortCode}")
    @Operation(
        summary = "Get URL analytics",
        description = "Returns click count, creation time, expiry, and status for a given short code."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Analytics retrieved",
            content = @Content(schema = @Schema(implementation = UrlDto.UrlAnalytics.class))),
        @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<UrlDto.UrlAnalytics> getAnalytics(
        @Parameter(description = "Short code", example = "abc123") @PathVariable String shortCode
    ) {
        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }

    @DeleteMapping("/api/urls/{shortCode}")
    @Operation(
        summary = "Deactivate a short URL",
        description = "Marks the short URL as inactive. It will no longer redirect."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "URL deactivated"),
        @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<Void> deactivate(
        @Parameter(description = "Short code", example = "abc123") @PathVariable String shortCode
    ) {
        urlService.deactivate(shortCode);
        return ResponseEntity.noContent().build();
    }
}
