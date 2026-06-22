package com.bookstore.ds.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Automatically reads the routing flag set by the ThreadLocal aspect
        // Defaults to PRIMARY if the context variable has not been initialized yet
        return DataSourceContextHolder.get() != null ? DataSourceContextHolder.get() : DataSourceContextHolder.DataSourceType.PRIMARY;
    }
}

