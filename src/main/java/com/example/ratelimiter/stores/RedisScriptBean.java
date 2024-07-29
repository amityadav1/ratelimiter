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
    public RedisScript<String> redisScript() {
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
                    return tostring(0) -- Not being rate limited
                end
                
                -- Get the oldest entry in the set
                 local oldest_ts = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')[2]
                    if oldest_ts then
                        local retry_after = math.ceil((oldest_ts + interval * 1000 - current_time[1]) / 1000)
                        return retry_after > 0 and tostring(retry_after) or tostring(1)  -- Ensure we always return at least 1 second
                    else
                        return tostring(interval)  -- If for some reason oldest_ts is nil, return the full interval
                    end
                """;
        return RedisScript.of(script, String.class);
    }
}
