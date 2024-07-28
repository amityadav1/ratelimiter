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

	@Value("${"+RatelimiterApplication.RATE_LIMITER_CONFIG_LIMIT+"}")
	private int initLimit;

	@Value("${" + RatelimiterApplication.RATE_LIMITER_CONFIG_INTERVAL + "}")
	private int initInterval;

	@Test
	void contextLoads() {
		assertNotNull(rtController);
		System.out.println(port);
	}

	@Test
	void testInitConfiguration() {
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> response = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody().getInterval(), initInterval);
		assertEquals(response.getBody().getLimit(), initLimit);
	}


	@Test
	@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
	void testConfigurationChange() {
		String configURL = "http://localhost:" + port + "/api/configure";
		ResponseEntity<RateLimiterConfig> getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(getResponse.getBody().getInterval(), initInterval);
		assertEquals(getResponse.getBody().getLimit(), initLimit);

		RateLimiterConfig config = new RateLimiterConfig(100, 200);
		ResponseEntity<Boolean> postResponse = this.restTemplate.postForEntity(configURL, config, Boolean.class);
		assertEquals(postResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(postResponse.getBody(), Boolean.TRUE);

		getResponse = this.restTemplate.getForEntity(configURL, RateLimiterConfig.class);
		assertEquals(getResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(getResponse.getBody().getInterval(), 100);
		assertEquals(getResponse.getBody().getLimit(), 200);
	}


	@Test
	void testIsRateLimited() {
		String checkLimitURL = "http://localhost:" + port + "/api/is_rate_limited/";
		ResponseEntity<Boolean> response = this.restTemplate.getForEntity(checkLimitURL + "abcd", Boolean.class);
		assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(response.getBody(), Boolean.FALSE);
	}	
}
