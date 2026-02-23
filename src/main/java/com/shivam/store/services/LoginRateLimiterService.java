package com.shivam.store.services;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiterService.class);
    private static final int MAX_ATTEMPTS_PER_MINUTE = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<String, LocalWindow> localWindows = new ConcurrentHashMap<>();

    public boolean isAllowed(String clientIp) {
        // OWASP A07: enforce per-IP login throttling; Redis is used in production, in-memory fallback keeps auth available.
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                String key = "security:login-attempts:" + clientIp;
                Long attempts = redisTemplate.opsForValue().increment(key);
                if (attempts != null && attempts == 1L) {
                    redisTemplate.expire(key, WINDOW);
                }
                return attempts != null && attempts <= MAX_ATTEMPTS_PER_MINUTE;
            } catch (RuntimeException ex) {
                log.warn("security_event=login_rate_limit_redis_fallback reason=redis_unavailable");
            }
        }

        return isAllowedInMemory(clientIp);
    }

    private boolean isAllowedInMemory(String clientIp) {
        long now = System.currentTimeMillis();

        LocalWindow updatedWindow = localWindows.compute(clientIp, (key, current) -> {
            if (current == null || now - current.windowStartMillis() >= WINDOW.toMillis()) {
                return new LocalWindow(now, 1);
            }
            return new LocalWindow(current.windowStartMillis(), current.attempts() + 1);
        });

        localWindows.entrySet().removeIf(entry -> now - entry.getValue().windowStartMillis() >= WINDOW.toMillis());
        return updatedWindow.attempts() <= MAX_ATTEMPTS_PER_MINUTE;
    }

    private record LocalWindow(long windowStartMillis, int attempts) {
    }
}
