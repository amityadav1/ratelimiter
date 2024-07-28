package com.example.ratelimiter.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.example.ratelimiter.RatelimiterApplication;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Define the configuration class for the Rate Limiting service.
 * The annotations @configuration and @Configurationproperties
 * enables 
 */
@Configuration
@ConfigurationProperties(prefix = RatelimiterApplication.RATE_LIMITER_CONFIG_PREFIX)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterConfig {
    
    // Time interval in seconds for rate limiting
    private int interval;

    // The maximum number of calls within the time interval
    private int limit;
} 
