package com.example.ratelimiter.service;


import com.example.ratelimiter.model.RateLimiterConfig;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;


/**
 * A service which provides rate limiting functionality. 
 * Having a service provides a clean seperation between the core
 * rate limiting functionality and the REST interface exposed to the
 * customers.
 */

@Service
public class RateLimiterService {

    // Since multiple requests can alter the rate limiter's configuration concurrently
    // use a AtomicRefrence to make the change in configuration thread safe.
    private AtomicReference<RateLimiterConfig> config = new AtomicReference<>();

    public RateLimiterService(RateLimiterConfig config) {
        this.config.set(new RateLimiterConfig(config.getInterval(), config.getLimit())); 
    }

    public void updateConfig(int interval, int limit) {
        this.config.set(new RateLimiterConfig(interval, limit));
    }

    public boolean isRateLimited(String uniqueToken) {
        return false;
    }

    public RateLimiterConfig getRateLimiterConfig() {
        RateLimiterConfig config = this.config.get();
        return new RateLimiterConfig(config.getInterval(), config.getLimit());

    }
}
