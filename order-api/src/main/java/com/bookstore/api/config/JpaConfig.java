package com.bookstore.api.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.bookstore.ds.config.DataSourceConfig;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@AutoConfigureAfter(DataSourceConfig.class)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.bookstore.api.repository.jpa", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
public class JpaConfig {


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("routingDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.bookstore.api.entity"); // Update to your entity package
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        // Explicitly set properties to avoid Hibernate auto-guessing
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "none"); // Set to none
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); 
        em.setJpaProperties(properties);
        
        return em;
    }


    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
