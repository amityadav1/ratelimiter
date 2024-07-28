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

# Build
```Gradle 
./gradlew build
```
Note - This needs a local redis running in order to run tests. 
TODO - Change the tests to use a in-memory store or move the tests to integration testing.
# Run
```Gradle
./gradlew bootRun
```
Build and run the rate limiter web application.
Note - This needs a local redis running in order to run tests.
TODO - Change the tests to use a in-memory store or move the tests to integration testing.

```bash
java -jar build/libs/ratelimiter-x.y.z.jar
```
Alternatively directly run the webapp using hava command line.
