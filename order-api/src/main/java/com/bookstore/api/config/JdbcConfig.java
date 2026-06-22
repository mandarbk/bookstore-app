package com.bookstore.api.config;

import com.bookstore.ds.config.DataSourceConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@AutoConfigureAfter(DataSourceConfig.class)
public class JdbcConfig {

    @Bean
    public JdbcTemplate replicaJdbcTemplate(@Qualifier("replicaDataSource") DataSource replicaDataSource) {
        return new JdbcTemplate(replicaDataSource);
    }

    @Bean
    @Primary
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource primaryDataSource) {
        return new JdbcTemplate(primaryDataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate replicaNamedParameterJdbcTemplate(
            @Qualifier("replicaJdbcTemplate") JdbcTemplate replicaJdbcTemplate) {
        return new NamedParameterJdbcTemplate(replicaJdbcTemplate.getDataSource());
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate primaryNamedParameterJdbcTemplate(
            @Qualifier("primaryJdbcTemplate") JdbcTemplate primaryJdbcTemplate) {
        return new NamedParameterJdbcTemplate(primaryJdbcTemplate.getDataSource());
    }
}
