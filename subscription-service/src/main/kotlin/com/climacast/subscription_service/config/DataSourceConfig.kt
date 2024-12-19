package com.climacast.subscription_service.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

@Configuration
@EnableJpaAuditing
class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source.hikari")
    fun sourceDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    fun replicaDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean
    fun routingDataSource(): DataSource = object : AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): DataSourceConfigConstants {
            val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            return if (isReadOnly) {
                DataSourceConfigConstants.REPLICA_DATASOURCE
            } else DataSourceConfigConstants.SOURCE_DATASOURCE
        }
    }.apply {
        setTargetDataSources(mapOf(
            DataSourceConfigConstants.SOURCE_DATASOURCE to sourceDataSource(),
            DataSourceConfigConstants.REPLICA_DATASOURCE to replicaDataSource()
        ))
        setDefaultTargetDataSource(sourceDataSource())
    }

    @Bean
    @Primary
    fun appDataSource() = LazyConnectionDataSourceProxy(routingDataSource())

    enum class DataSourceConfigConstants {
        SOURCE_DATASOURCE,
        REPLICA_DATASOURCE
    }
}