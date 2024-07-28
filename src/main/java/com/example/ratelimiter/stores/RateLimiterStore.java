package com.example.ratelimiter.stores;

import com.example.ratelimiter.model.RateLimiterConfig;

/*
 * Define interface for a store which stores information
 * required for rate limiting as well as method for determining
 * if limits have been reached or not. 
 */
public interface RateLimiterStore {
    /**
     * @param key - Key or token for limiting
     * @param config - Rate Limiter Configuration
     * @return - True if the rate limited based on 
     * the provided configuration, false otherwise
     */
    public boolean isRateLimited(String key, RateLimiterConfig config);
}
