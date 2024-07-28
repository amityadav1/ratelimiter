package com.example.ratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RatelimiterApplication {

	// static public constants for rate limiter configuration
	public final static String RATE_LIMITER_CONFIG_PREFIX = "rate.limit";
	public final static String RATE_LIMITER_CONFIG_INTERVAL =  RATE_LIMITER_CONFIG_PREFIX + ".interval";
	public final static String RATE_LIMITER_CONFIG_LIMIT =  RATE_LIMITER_CONFIG_PREFIX +  ".limit";

	public static void main(String[] args) {
		SpringApplication.run(RatelimiterApplication.class, args);
	}

}
