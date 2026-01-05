# Rate Limiting Configuration for Spring Cloud Gateway

## Overview
This document provides configuration examples for implementing rate limiting in the Spring Cloud Gateway to prevent brute force attacks and API abuse.

## Maven Dependency
Add the following to your gateway's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

## Redis Configuration
Rate limiting requires Redis. Add to `application.yml`:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

## Rate Limiting Filter Configuration

### Example 1: Basic Rate Limiting
Add to your gateway's `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: userManagement-auth
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=0
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10  # requests per second
                redis-rate-limiter.burstCapacity: 20   # maximum burst
                redis-rate-limiter.requestedTokens: 1  # tokens per request
```

### Example 2: Per-User Rate Limiting
Create a custom KeyResolver bean:

```java
@Configuration
public class RateLimitConfiguration {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Extract user ID from JWT token or use IP address
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId == null) {
                // Fallback to IP address for anonymous users
                return Mono.just(exchange.getRequest().getRemoteAddress()
                    .getAddress().getHostAddress());
            }
            return Mono.just(userId);
        };
    }
}
```

Then reference it in `application.yml`:

```yaml
- name: RequestRateLimiter
  args:
    key-resolver: "#{@userKeyResolver}"
    redis-rate-limiter.replenishRate: 5
    redis-rate-limiter.burstCapacity: 10
```

### Example 3: Different Limits per Endpoint

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Strict rate limiting for login (prevent brute force)
        - id: auth-login
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/login
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 3    # 3 requests per second
                redis-rate-limiter.burstCapacity: 5     # max 5 burst
                
        # Moderate rate limiting for registration
        - id: auth-register
          uri: http://localhost:8081
          predicates:
            - Path=/api/auth/register
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 2
                redis-rate-limiter.burstCapacity: 3
                
        # Normal rate limiting for other endpoints
        - id: userManagement-general
          uri: http://localhost:8081
          predicates:
            - Path=/api/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20
                redis-rate-limiter.burstCapacity: 40
```

## Custom Rate Limiter Implementation

For more control, implement a custom rate limiter:

```java
@Component
public class CustomRateLimiter implements RateLimiter<Object> {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final int defaultReplenishRate = 10;
    private final int defaultBurstCapacity = 20;
    
    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        String key = "rate_limit:" + routeId + ":" + id;
        
        return redisTemplate.opsForValue()
            .increment(key)
            .flatMap(count -> {
                if (count > defaultBurstCapacity) {
                    return Mono.just(new Response(false, Map.of()));
                }
                
                // Set expiration if first request
                if (count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(1))
                        .subscribe();
                }
                
                long remaining = defaultBurstCapacity - count;
                return Mono.just(new Response(true, Map.of(
                    "X-RateLimit-Remaining", String.valueOf(remaining),
                    "X-RateLimit-Burst-Capacity", String.valueOf(defaultBurstCapacity),
                    "X-RateLimit-Replenish-Rate", String.valueOf(defaultReplenishRate)
                )));
            });
    }
}
```

## Response Headers

When rate limiting is active, the following headers are added to responses:

- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Burst-Capacity`: Maximum burst capacity
- `X-RateLimit-Replenish-Rate`: Rate at which tokens are replenished

When rate limit is exceeded:
- HTTP Status: `429 Too Many Requests`
- Response body: Error message indicating rate limit exceeded

## Testing Rate Limiting

Use a tool like Apache Bench or curl to test:

```bash
# Test with Apache Bench
ab -n 100 -c 10 http://localhost:8080/api/auth/login

# Test with curl in a loop
for i in {1..20}; do
  curl -w "\n%{http_code}\n" http://localhost:8080/api/auth/login
  sleep 0.1
done
```

## Production Recommendations

1. **Different limits per environment**:
   - Development: Generous limits
   - Staging: Production-like limits
   - Production: Strict limits

2. **Monitor rate limiting metrics**:
   - Number of rate-limited requests
   - Top rate-limited users/IPs
   - Rate limit threshold trends

3. **Adjust limits based on usage patterns**:
   - Review logs and metrics regularly
   - Increase limits for legitimate high-traffic periods
   - Decrease limits if abuse is detected

4. **Implement graceful degradation**:
   - Return informative error messages
   - Include retry-after header
   - Log rate limit violations

5. **Consider distributed rate limiting**:
   - For multi-region deployments
   - Use Redis Cluster or Redis Sentinel

## Environment Variables

Add to `.env.example`:

```bash
# Redis Configuration for Rate Limiting
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# Rate Limiting Configuration
RATE_LIMIT_LOGIN_REPLENISH=3
RATE_LIMIT_LOGIN_BURST=5
RATE_LIMIT_REGISTER_REPLENISH=2
RATE_LIMIT_REGISTER_BURST=3
RATE_LIMIT_GENERAL_REPLENISH=20
RATE_LIMIT_GENERAL_BURST=40
```

## Security Considerations

1. **Use IP-based rate limiting for anonymous endpoints** (login, register)
2. **Use user-based rate limiting for authenticated endpoints**
3. **Consider implementing exponential backoff** for repeated violations
4. **Log rate limit violations** for security monitoring
5. **Implement account lockout** after repeated login failures
6. **Use CAPTCHA** after certain number of failed attempts

## Additional Resources

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestratelimiter-gatewayfilter-factory)
- [Redis Rate Limiter](https://redis.io/docs/manual/patterns/distributed-locks/)
- [OWASP Rate Limiting Guide](https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html)
