package com.example.ratelimiter.stores;

import com.example.ratelimiter.model.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;


/**
 * A {@link RateLimiterStore} implementation using Redis as the backend store.
 */
@Configuration
@Component
@Slf4j
public class RedisRateLimiterStore implements RateLimiterStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> redisScript;

    public RedisRateLimiterStore(RedisTemplate<String, String> redisTemplate, RedisScript<String> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    /**
     *
     * @param key - Key or token for limiting
     * @param config - Rate Limiter Configuration. This provides the flexibility of
     *               specifying different configuration for different context if needed
     *               or allows for dynamically changing the configuration for a given context.
     *               Concretely:
     *               1. If the limit/interval is increased or decreased then all the requests
     *               in the current interval, evn  the ones which were made will count towards the limits.
     *               2. However if the interval is increased - requests which were already expired
     *               may not count towards the limit. The behavior is dependent on whether any requests
     *               were made after the end time of the last configured interval and before the new
     *               configuration is provided.
     * @return TRUE if the request is rate limited, FALSE otherwise.
     */
    @Override
    public Integer isRateLimited(String key, RateLimiterConfig config) {
        String result = redisTemplate.execute(redisScript,
                            Collections.singletonList(key),
                            String.valueOf(config.getLimit()),
                            String.valueOf(config.getInterval()));
        log.info("Received {} for {}", result, key);
        Integer retryAfter = 0;
        try {
            retryAfter = Integer.valueOf(result);
        } catch (NumberFormatException e) {
            log.info("Number format exception {}", e.getMessage());
        }
        // We fail open if the call to redis does not succeed.
        return retryAfter;
    }
}
