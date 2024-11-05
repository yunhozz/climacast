package com.climacast.batch_server.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

@Configuration
class BatchDatabaseConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.batch.hikari")
    fun batchDataSource() = HikariDataSource()

    @Bean
    fun batchJobRepository(batchTransactionManager: PlatformTransactionManager): JobRepository =
        JobRepositoryFactoryBean().apply {
            setDataSource(batchDataSource())
            transactionManager = batchTransactionManager
            afterPropertiesSet()
        }.`object`

    @Bean
    fun batchTransactionManager(): PlatformTransactionManager =
        DataSourceTransactionManager(batchDataSource())
}

@Configuration
class JpaDatabaseConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.hikari.source")
    fun weatherSourceDatasource() = HikariDataSource()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.hikari.replica")
    fun weatherReplicaDataSource() = HikariDataSource()

    @Bean
    @Primary
    fun routingDataSource(
        weatherSourceDatasource: DataSource,
        weatherReplicaDataSource: DataSource
    ) = object : AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): DataSourceConfigConstants {
            val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            return if (isReadOnly) {
                DataSourceConfigConstants.REPLICA_DATASOURCE
            } else DataSourceConfigConstants.SOURCE_DATASOURCE
        }
    }.apply {
        setTargetDataSources(mapOf(
            DataSourceConfigConstants.SOURCE_DATASOURCE to weatherSourceDatasource,
            DataSourceConfigConstants.REPLICA_DATASOURCE to weatherReplicaDataSource
        ))
        setDefaultTargetDataSource(weatherSourceDatasource)
    }

    @Bean
    fun lazyDataSource(routingDataSource: DataSource): LazyConnectionDataSourceProxy =
        LazyConnectionDataSourceProxy(routingDataSource)

    enum class DataSourceConfigConstants {
        SOURCE_DATASOURCE,
        REPLICA_DATASOURCE
    }
}