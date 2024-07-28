package com.example.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.example.ratelimiter.controller.RateLimiterController;
import com.example.ratelimiter.model.RateLimiterConfig;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@TestPropertySource("/test.properties")
class RatelimiterApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private RateLimiterController rtController;

	@Autowired
	private TestRestTemplate restTemplate;

	// Initial configuration from the properties file for limit.
	@Value("${"+RatelimiterApplication.RATE_LIMITER_CONFIG_LIMIT+"}")
	private int initLimit;

	// Initial configuration from the properties file for interval.
	@Value("${" + RatelimiterApplication.RATE_LIMITER_CONFIG_INTERVAL + "}")
	private int initInterval;

	/*
	 * Test that the spring boot is able to construct and inject the 
	 * RateLimiterController.
	 */
	@Test
	void contextLoads() {
		assertNotNull(rtController);
	}

	/*
	 * Test that the rate limiter configuration from the properties 
	 * file is used to construct the rate limiter service.
	 */
	@Test
	void testInitConfiguration() {
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> response = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody().getInterval(), initInterval);
		assertEquals(response.getBody().getLimit(), initLimit);
	}


	/*
	 * Test changing the rate limiter configuration and reading it back to ensure that 
	 * the configuration change took effect.
	 */
	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	void testConfigurationChange() {

		// Get the current configuration
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(getResponse.getBody().getInterval(), initInterval);
		assertEquals(getResponse.getBody().getLimit(), initLimit);

		// Update the rate limiting configuration
		RateLimiterConfig config = new RateLimiterConfig(100, 200);
		ResponseEntity<Boolean> postResponse = this.restTemplate.postForEntity(configURL, config, Boolean.class);
		assertEquals(postResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(postResponse.getBody(), Boolean.TRUE);

		// Fetch the configuration again and ensure that it matches what was configured.
		getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(getResponse.getBody().getInterval(), 100);
		assertEquals(getResponse.getBody().getLimit(), 200);
	}

	/**
	 * Test the rate limiting functionality.
	 */

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	void testIsRateLimited() {

		// Get the current configuration
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));

		int limit = getResponse.getBody().getLimit();
		int interval = getResponse.getBody().getInterval();

		String checkLimitURL = "http://localhost:" + port + "/api/is_rate_limited/";
		ResponseEntity<Boolean> response;

		// Let's note the current time in seconds;
		long startTime = System.currentTimeMillis()/1000;

		// Test that request within the limit within the interval are allowed.
		for (int i = 0 ; i < limit; i++) {
			response = this.restTemplate.getForEntity(checkLimitURL + "abcd", Boolean.class);
			assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
			assertEquals(response.getBody(), Boolean.FALSE);
		}

		// Test once the limit is reached, rate limiting kicks in.
		for (int i = 0 ; i < limit; i++) {
			response = this.restTemplate.getForEntity(checkLimitURL + "abcd", Boolean.class);
			assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
			assertEquals(response.getBody(), Boolean.TRUE);
		}

		// Test any other key still are allowed - so rate limiting is per key.
		for (int i = 0 ; i < limit/2; i++) {
			response = this.restTemplate.getForEntity(checkLimitURL + "def", Boolean.class);
			assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
			assertEquals(response.getBody(), Boolean.FALSE);
		}

		// Wait until the interval has elapsed.
		try {
			long endTime = System.currentTimeMillis()/1000;
			long waitTime = interval - (endTime - startTime);
			assert  waitTime >= 0;
			Thread.sleep(waitTime * 1000);
		} catch (InterruptedException e) {
			assert false;
		}

		// Ensure requests are allowed again.
		response = this.restTemplate.getForEntity(checkLimitURL + "abcd", Boolean.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody(), Boolean.FALSE);

		response = this.restTemplate.getForEntity(checkLimitURL + "def", Boolean.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody(), Boolean.FALSE);
	}


	/**
	 * Test rate limiting functionality when the limits are changed.
	 */

	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	void testIsRateLimitedWithLimitConfigChanges() {

		// Get the current configuration
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));

		int limit = getResponse.getBody().getLimit();
		int interval = getResponse.getBody().getInterval();

		String checkLimitURL = "http://localhost:" + port + "/api/is_rate_limited/";
		ResponseEntity<Boolean> response;

		// Lets note the current time in seconds;
		long startTime = System.currentTimeMillis()/1000;

		// Send all the requests which are allowed (i.e upto 'limit' number of requests).
		for (int i = 0 ; i < limit; i++) {
			response = this.restTemplate.getForEntity(checkLimitURL + "xyz", Boolean.class);
			assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
			assertEquals(response.getBody(), Boolean.FALSE);
		}

		// Change Config and increase the limit (Doubling the limits).
		RateLimiterConfig config = new RateLimiterConfig(limit*2, interval);
		ResponseEntity<Boolean> postResponse = this.restTemplate.postForEntity(configURL, config, Boolean.class);
		assertEquals(postResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(postResponse.getBody(), Boolean.TRUE);


		// We can send additional `limit` number of requests now and they should be allowed
		for (int i = 0 ; i < limit; i++) {
			response = this.restTemplate.getForEntity(checkLimitURL + "xyz", Boolean.class);
			assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
			assertEquals(response.getBody(), Boolean.FALSE);
		}

		// We have reached the new configured limits, so rate limiting should kick in.
		response = this.restTemplate.getForEntity(checkLimitURL + "xyz", Boolean.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody(), Boolean.TRUE);


		try {
			long endTime = System.currentTimeMillis()/1000;
			long waitTime = interval - (endTime - startTime);
			assert  waitTime >= 0;
			Thread.sleep(waitTime * 1000);
		} catch (InterruptedException e) {
			assert false;
		}

		// After the interval has passed , requests should be allowed
		response = this.restTemplate.getForEntity(checkLimitURL + "xyz", Boolean.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody(), Boolean.FALSE);
	}


}
