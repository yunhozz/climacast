package com.climacast.ai_service.config

import com.climacast.ai_service.common.enums.CacheType
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Primary
    @Bean(name = [CacheBeanNames.DEFAULT_CACHE_MANAGER_BEAN])
    fun defaultCacheManager(): CacheManager {
        val cache = CacheType.DEFAULT_CACHE
        val caffeine = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(cache.expirationTime))

        return CaffeineCacheManager(cache.cacheName).also {
            it.setCaffeine(caffeine)
        }
    }

    @Bean(name = [CacheBeanNames.WEATHER_AI_QUERY_CACHE_MANAGER_BEAN])
    fun weatherAiQueryCacheManager(): CacheManager {
        val cache = CacheType.WEATHER_AI_QUERY_CACHE
        val caffeine = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(cache.expirationTime))

        return CaffeineCacheManager(cache.cacheName).also {
            it.setCaffeine(caffeine)
            it.setAsyncCacheMode(true)
        }
    }
}

object CacheBeanNames {
    const val DEFAULT_CACHE_MANAGER_BEAN = "default-cache-manager-bean"
    const val WEATHER_AI_QUERY_CACHE_MANAGER_BEAN = "weather-ai-query-cache-manager-bean"
}