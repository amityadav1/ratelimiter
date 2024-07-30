# Rate Limiter
A simple rate limiter REST microservice for rate limiting keys. The rate limiter provides sliding window rate
limiting algorithm. The rate limiter provides the following entities and APIs

## Entities
[RateLimiterConfig](src/main/java/com/example/ratelimiter/model/RateLimiterConfig.java) - Configuration for rate
limiter to specify rate limiting interval (in seconds) and limit (number of calls in the specified interval).


## API
1. GET /api/config --> returns the current rate limiter [configuration]((src/main/java/com/example/ratelimiter/model/RateLimiterConfig.java)).
2. POST /api/config -> Updates the current rate limiter [configuration]((src/main/java/com/example/ratelimiter/model/RateLimiterConfig.java)).
3. GET /api/is_rate_limited/:uniquetoken --> Return if this token is rate limited.  

Checkout http://localhost:8080/swagger-ui/index.html after running the service for api docs.

# Build
```Gradle 
./gradlew build
```
Note - This needs a **local redis** running in order to run tests. [Here](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-mac-os/) are
the installation instructions for redis on macOS.

# Run
```Gradle
./gradlew bootRun
```
Build and run the rate limiter web application.
Note - This needs a **local redis** running in order to run tests.  [Here](https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-mac-os/) are 
the installation instructions for redis on macOS.

```bash
java -jar build/libs/ratelimiter-x.y.z.jar
```
Alternatively directly run the webapp using hava command line.

# Metrics
Uses Graphite as the backend for metrics collection. Use dockers for running a local graphite server (default configuration)
```bash
docker pull graphiteapp/graphite-statsd
docker run -d --name graphite-server -p 80:80 -p 2003-2004:2003-2004 -p 2023-2024:2023-2024 -p 8125:8125/udp -p 8126:8126 graphiteapp/graphite-statsd
```

# TODO
1. Enable SSL for APIs as well as for redis client.
2. Enable spring boot out of the box metrics export to Graphite.
3. Enable Integration testing by launching the redis server from within the test suite.
4. And many more....
