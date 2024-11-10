package com.climacast.batch_server.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager

@Configuration
class BatchDatabaseConfig {
    @Bean
    @BatchDataSource
    @ConfigurationProperties(prefix = "spring.datasource.batch.hikari")
    fun batchDataSource() = HikariDataSource()

    @Bean
    fun batchJobRepository(): JobRepository =
        JobRepositoryFactoryBean().apply {
            setDataSource(batchDataSource())
            transactionManager = batchTransactionManager()
            afterPropertiesSet()
        }.`object`

    @Bean
    fun batchTransactionManager(): PlatformTransactionManager = DataSourceTransactionManager(batchDataSource())
}

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.climacast.batch_server.model.repository"],
    entityManagerFactoryRef = "entityManagerFactory"
)
class JpaDatabaseConfig {
    companion object {
        const val ENTITY_PACKAGE = "com.climacast.batch_server.model.entity"
        const val HIBERNATE_DDL_AUTO = "create"
        const val HIBERNATE_SHOW_SQL = "true"
        const val HIBERNATE_FORMAT_SQL = "true"
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.hikari.source")
    fun jpaSourceDatasource() = HikariDataSource()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.hikari.replica")
    fun jpaReplicaDataSource() = HikariDataSource()

    @Bean
    @Primary
    fun routingDataSource() = object: AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): DataSourceConfigConstants {
            val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            return if (isReadOnly) {
                DataSourceConfigConstants.REPLICA_DATASOURCE
            } else DataSourceConfigConstants.SOURCE_DATASOURCE
        }
    }.apply {
        setTargetDataSources(mapOf(
            DataSourceConfigConstants.SOURCE_DATASOURCE to jpaSourceDatasource(),
            DataSourceConfigConstants.REPLICA_DATASOURCE to jpaReplicaDataSource()
        ))
        setDefaultTargetDataSource(jpaSourceDatasource())
    }

    @Bean
    fun entityManagerFactory(): EntityManagerFactory =
        LocalContainerEntityManagerFactoryBean().apply {
            dataSource = routingDataSource()
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            jpaPropertyMap.putAll(
                mapOf(
                    "hibernate.hbm2ddl.auto" to HIBERNATE_DDL_AUTO,
                    "hibernate.show_sql" to HIBERNATE_SHOW_SQL,
                    "hibernate.format_sql" to HIBERNATE_FORMAT_SQL
                )
            )
            setPackagesToScan(ENTITY_PACKAGE)
            afterPropertiesSet()
        }.`object`!!

    @Bean("transactionManager")
    @Primary
    fun jpaTransactionManager() = JpaTransactionManager(entityManagerFactory())

    @Bean
    fun lazyDataSource() = LazyConnectionDataSourceProxy(routingDataSource())

    enum class DataSourceConfigConstants {
        SOURCE_DATASOURCE,
        REPLICA_DATASOURCE
    }
}