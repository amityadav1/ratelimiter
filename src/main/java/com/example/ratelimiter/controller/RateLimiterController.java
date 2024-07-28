package com.example.ratelimiter.controller;

import com.example.ratelimiter.model.RateLimiterConfig;
import com.example.ratelimiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimitService) {
        this.rateLimiterService = rateLimitService;
    }

    @GetMapping("/configure")
    public ResponseEntity<RateLimiterConfig> getRateLimiterConfiguration() {
        return ResponseEntity.ok(rateLimiterService.getRateLimiterConfig());
    }
    
    @PostMapping("/configure")
    public ResponseEntity<Boolean> configureRateLimit(@RequestBody RateLimiterConfig config) {
        rateLimiterService.updateConfig(config.getInterval(), config.getLimit());
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @GetMapping("/is_rate_limited/{uniqueToken}")
    public ResponseEntity<Boolean> isRateLimited(@PathVariable String uniqueToken) {
        boolean isLimited = rateLimiterService.isRateLimited(uniqueToken);
        return ResponseEntity.ok(isLimited ? Boolean.TRUE : Boolean.FALSE);
    }
}
