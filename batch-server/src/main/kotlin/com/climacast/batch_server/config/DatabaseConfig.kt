package com.climacast.batch_server.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.beans.factory.annotation.Qualifier
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
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "jpaTransactionManager"
)
class JpaDatabaseConfig {
    companion object {
        const val ENTITY_PACKAGE = "com/climacast/batch_server/model/entity"
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
    fun entityManagerFactory() = LocalContainerEntityManagerFactoryBean().apply {
        dataSource = routingDataSource()
        jpaVendorAdapter = HibernateJpaVendorAdapter()
        setPackagesToScan(ENTITY_PACKAGE)
    }

    @Bean("transactionManager")
    @Primary
    fun jpaTransactionManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: EntityManagerFactory
    ) = JpaTransactionManager(entityManagerFactory)

    @Bean
    fun lazyDataSource() = LazyConnectionDataSourceProxy(routingDataSource())

    enum class DataSourceConfigConstants {
        SOURCE_DATASOURCE,
        REPLICA_DATASOURCE
    }
}