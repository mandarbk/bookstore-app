package com.bookstore.ds.config;

public class DataSourceContextHolder {
    public enum DataSourceType {
        PRIMARY, REPLICA
    }

    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void set(DataSourceType type) {
        CONTEXT.set(type);
    }

    public static DataSourceType get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

