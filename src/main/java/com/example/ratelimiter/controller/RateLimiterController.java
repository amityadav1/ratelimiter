package com.example.ratelimiter.controller;

import com.example.ratelimiter.model.RateLimiterConfig;
import com.example.ratelimiter.service.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api")
// Use lombok to add logger, available as 'log'
@Slf4j
public class RateLimiterController {

    public static final String RETRY_AFTER_HEADER = "Retry-After";
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimitService) {
        this.rateLimiterService = rateLimitService;
    }

    @GetMapping("/configure")
    public ResponseEntity<RateLimiterConfig> getRateLimiterConfiguration() {
        log.debug("Received Get configuration request");
        return ResponseEntity.ok(rateLimiterService.getRateLimiterConfig());
    }
    
    @PostMapping("/configure")
    public ResponseEntity<Boolean> configureRateLimit(@RequestBody RateLimiterConfig config) {
        log.debug("Received Update configuration request");
        rateLimiterService.updateConfig(config.getInterval(), config.getLimit());
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @GetMapping("/is_rate_limited/{uniqueToken}")
    public ResponseEntity<Void> isRateLimited(@PathVariable String uniqueToken) {
        log.debug(String.format("Received Check Rate Limit request for %s", uniqueToken));
        Integer isLimited = rateLimiterService.isRateLimited(uniqueToken);
        if (isLimited > 0) {
            log.info("Rate Limiting applied for {}", uniqueToken);
            HttpHeaders headers = new HttpHeaders();
            headers.add(RETRY_AFTER_HEADER, isLimited.toString());
            return new ResponseEntity<>(headers, HttpStatusCode.valueOf(429));
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
}
