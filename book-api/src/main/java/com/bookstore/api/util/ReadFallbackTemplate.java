package com.bookstore.api.util;

import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReadFallbackTemplate {
    private final JdbcTemplate replicaJdbcTemplate;
    private final JdbcTemplate primaryJdbcTemplate;

    public ReadFallbackTemplate(
            @Qualifier("replicaJdbcTemplate") JdbcTemplate replicaJdbcTemplate,
            @Qualifier("primaryJdbcTemplate") JdbcTemplate primaryJdbcTemplate) {
        this.replicaJdbcTemplate = replicaJdbcTemplate;
        this.primaryJdbcTemplate = primaryJdbcTemplate;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try {
            log.debug("Attempting to read from replica database");
            return replicaJdbcTemplate.queryForObject(sql, rowMapper, args);
        } catch (DataAccessException e) {
            log.warn("Replica database failed, falling back to primary. Error: {}", e.getMessage());
            return primaryJdbcTemplate.queryForObject(sql, rowMapper, args);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try {
            log.debug("Attempting to read from replica database");
            return replicaJdbcTemplate.query(sql, rowMapper, args);
        } catch (DataAccessException e) {
            log.warn("Replica database failed, falling back to primary. Error: {}", e.getMessage());
            return primaryJdbcTemplate.query(sql, rowMapper, args);
        }
    }

    public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) {
        try {
            log.debug("Attempting to read from replica database");
            return replicaJdbcTemplate.queryForList(sql, elementType, args);
        } catch (DataAccessException e) {
            log.warn("Replica database failed, falling back to primary. Error: {}", e.getMessage());
            return primaryJdbcTemplate.queryForList(sql, elementType, args);
        }
    }

    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        try {
            log.debug("Attempting to read from replica database");
            return replicaJdbcTemplate.queryForObject(sql, requiredType, args);
        } catch (DataAccessException e) {
            log.warn("Replica database failed, falling back to primary. Error: {}", e.getMessage());
            return primaryJdbcTemplate.queryForObject(sql, requiredType, args);
        }
    }

    public <T> T execute(Function<JdbcTemplate, T> operation) {
        try {
            log.debug("Attempting to read from replica database");
            return operation.apply(replicaJdbcTemplate);
        } catch (DataAccessException e) {
            log.warn("Replica database failed, falling back to primary. Error: {}", e.getMessage());
            return operation.apply(primaryJdbcTemplate);
        }
    }
}
