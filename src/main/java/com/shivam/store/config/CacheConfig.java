package com.shivam.store.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper.copy())));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("products", baseConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("productById", baseConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
