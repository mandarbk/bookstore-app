package com.bookstore.ds.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ApplicationConfiguration {

    @Bean
    @ConditionalOnBooleanProperty(name = "db.migration.enabled")
    public Flyway flyway(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        return Flyway.configure()
                .dataSource(primaryDataSource)
                .locations("classpath:db/migration")
                .table("books_api_server_schema_history")
                .baselineOnMigrate(false)
                // You can add credentials here if your DataSource doesn't 
                // already have them configured
                .load();
    }

}
