package com.climacast.subscription_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    @Bean
    fun redissonClient(): RedissonClient = Redisson.create(
        Config().also { config ->
            config.useSingleServer()
                .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
        }
    )

    @Bean
    fun redisTemplate(factory: RedissonConnectionFactory): RedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        return RedisTemplate<String, String>().apply {
            connectionFactory = factory
            keySerializer = stringSerializer
            hashKeySerializer = stringSerializer
            valueSerializer = stringSerializer
            hashValueSerializer = stringSerializer
        }
    }
}