package com.example.ratelimiter.service;


import com.example.ratelimiter.model.RateLimiterConfig;
import java.util.concurrent.atomic.AtomicReference;

import com.example.ratelimiter.stores.RateLimiterStore;
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
    private final RateLimiterStore store;

    public RateLimiterService(RateLimiterConfig config, RateLimiterStore store) {
        if (config.getLimit() <= 0 || config.getInterval() <= 0) {
            throw new IllegalArgumentException(String.format("Invalid rate limiter configurataion Limit %d, Interval %d",
                    config.getLimit(), config.getInterval()));
        }
        this.config.set(new RateLimiterConfig(config.getInterval(), config.getLimit()));
        this.store = store;
    }

    public void updateConfig(int interval, int limit) {
        this.config.set(new RateLimiterConfig(interval, limit));
    }

    public boolean isRateLimited(String uniqueToken) {
        return store.isRateLimited(uniqueToken, this.config.get());
    }

    public RateLimiterConfig getRateLimiterConfig() {
        RateLimiterConfig config = this.config.get();
        return new RateLimiterConfig(config.getInterval(), config.getLimit());

    }
}
