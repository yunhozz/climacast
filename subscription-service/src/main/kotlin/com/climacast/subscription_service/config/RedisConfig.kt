package com.climacast.subscription_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    companion object {
        const val REDISSON_HOST_PREFIX = "redis://"
        const val ONE_MIN_CACHE = "one-min-cache"
        const val FIVE_MIN_CACHE = "five-min-cache"
        const val TTL_ONE_MINUTE = 1L
        const val TTL_FIVE_MINUTE = 5L
    }

    @Value("\${redis.host}")
    private lateinit var host: String

    @Value("\${redis.port}")
    private lateinit var port: String

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config
            .useSingleServer()
            .setAddress("${REDISSON_HOST_PREFIX}$host:$port")
        return Redisson.create(config)
    }

    @Bean
    fun redisTemplate(redissonClient: RedissonClient) =
        RedisTemplate<String, Any>().apply {
            connectionFactory = RedissonConnectionFactory(redissonClient)
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }

    @Bean
    fun redisCacheManager(redissonClient: RedissonClient): CacheManager =
        RedissonSpringCacheManager(redissonClient, mapOf(
            ONE_MIN_CACHE to CacheConfig(TTL_ONE_MINUTE, 0),
            FIVE_MIN_CACHE to CacheConfig(TTL_FIVE_MINUTE, 0)
        ))
}