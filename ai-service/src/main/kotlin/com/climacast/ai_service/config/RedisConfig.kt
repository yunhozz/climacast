package com.climacast.ai_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonReactiveClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    @Bean
    @Profile("local")
    fun singleRedissonReactiveClient(): RedissonReactiveClient = Redisson.create(
        Config().also { config ->
            config.useSingleServer()
                .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
                .setConnectTimeout(100)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
        }
    ).reactive()

    @Bean
    @Profile("dev", "prod")
    fun clusterRedissonReactiveClient(): RedissonReactiveClient = Redisson.create(
        Config().also { config ->
            val nodes = redisProperties.cluster.nodes
            val addresses = nodes.map { node -> "redis://$node" }.toTypedArray()

            config.useClusterServers()
                .addNodeAddress(*addresses)
                .setScanInterval(2000)
                .setConnectTimeout(100)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
        }
    ).reactive()

    @Bean
    fun reactiveRedisTemplate(factory: RedissonConnectionFactory): ReactiveRedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        val context = RedisSerializationContext.newSerializationContext<String, String>()
            .key(stringSerializer)
            .value(stringSerializer)
            .hashKey(stringSerializer)
            .hashValue(stringSerializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }
}