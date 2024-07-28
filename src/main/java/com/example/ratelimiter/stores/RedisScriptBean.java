package com.example.ratelimiter.stores;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * This class and the encapsulated LUA script contains the heart of
 * the sliding window logic for Redis. The LUA script is executed
 * as a single atomic unit on Redis Server and that provides the
 * accuracy guarntees for sliding window rate limiting.
 */
@Configuration
public class RedisScriptBean {

    /**
     * LUA Script for implemneting sliding window algorithm.
     * @return
     */
    @Bean
    public RedisScript<Boolean> redisScript() {
        String script = """
                -- Get the current time
                local current_time = redis.call('TIME')
                
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local interval = tonumber(ARGV[2])
           
                -- calculate current sliding window start time
                local start_time = current_time[1] - interval
                
                -- Remove all entries outside of the current sliding window from sorted set
                redis.call('ZREMRANGEBYSCORE', key, 0, start_time)
                
                -- Now count the current time
                local current_count = redis.call('ZCARD', key)
                
                if current_count < limit then
                    -- Add key
                    redis.call('ZADD', key, current_time[1], current_time[1]..current_time[2])
                    
                    -- Set Expiry
                    redis.call('EXPIRE', key, limit)
                    return false
                end
                
                return true
                """;
        return RedisScript.of(script, Boolean.class);
    }
}
