package com.example.ratelimiter.stores;

import com.example.ratelimiter.model.RateLimiterConfig;
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
public class RedisRateLimiterStore implements RateLimiterStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Boolean> redisScript;

    public RedisRateLimiterStore(RedisTemplate<String, String> redisTemplate, RedisScript<Boolean> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    @Override
    public boolean isRateLimited(String key, RateLimiterConfig config) {
        return redisTemplate.execute(redisScript,
                Collections.singletonList(key),
                String.valueOf(config.getLimit()),
                String.valueOf(config.getInterval()));
    }

}
