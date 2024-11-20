package com.climacast.batch_server.config

import com.zaxxer.hikari.HikariDataSource
import org.hibernate.cfg.SchemaToolingSettings
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.Properties
import javax.sql.DataSource

@Configuration
class BatchDataSourceConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.batch")
    fun batchDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean
    @Primary
    fun batchTransactionManager(): PlatformTransactionManager =
        DataSourceTransactionManager(batchDataSource())

    @Bean
    fun batchJobRepository(): JobRepository =
        JobRepositoryFactoryBean().apply {
            setDataSource(batchDataSource())
            transactionManager = batchTransactionManager()
            afterPropertiesSet()
        }.`object`
}

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = ["com.climacast.batch_server.model.repository"],
    entityManagerFactoryRef = "appEntityManagerFactory",
    transactionManagerRef = "appTransactionManager"
)
class AppDataSourceConfig(
    private val jpaProperties: JpaProperties,
    private val hibernateProperties: HibernateProperties
) {
    companion object {
        const val ENTITY_PACKAGE = "com.climacast.batch_server.model.entity"
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.source.hikari")
    fun sourceDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.weather.replica.hikari")
    fun replicaDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean
    fun routingDataSource(): DataSource = object: AbstractRoutingDataSource() {
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
    fun appDataSource(): DataSource = LazyConnectionDataSourceProxy(routingDataSource())

    @Bean
    fun appEntityManagerFactory() = LocalContainerEntityManagerFactoryBean().apply {
        jpaVendorAdapter = HibernateJpaVendorAdapter()
        dataSource = appDataSource()
        setJpaProperties(Properties().apply {
            putAll(jpaProperties.properties)
            this[SchemaToolingSettings.HBM2DDL_AUTO] = hibernateProperties.ddlAuto
        })
        setPackagesToScan(ENTITY_PACKAGE)
    }

    @Bean
    fun appTransactionManager(): PlatformTransactionManager =
        JpaTransactionManager().apply {
            entityManagerFactory = appEntityManagerFactory().`object`
        }

    @Bean
    fun jdbcTemplate() = JdbcTemplate(appDataSource())

    enum class DataSourceConfigConstants {
        SOURCE_DATASOURCE,
        REPLICA_DATASOURCE
    }
}