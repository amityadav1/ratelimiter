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
     * @param config - Rate Limiter Configuration. This provides the flexibility of
     *               specifying different configuration for different context if needed
     *               or allows for dynamically changing the configuration for a given context.
     * @return - 0 if not rate limited, else number of seconds to wait before retrying.
     */
    public Integer isRateLimited(String key, RateLimiterConfig config);
}
