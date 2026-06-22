package com.bookstore.ds.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
public class TransactionRoutingAspect {

    // Intercepts any method containing Spring's Transactional annotation
    @Around("@annotation(transactional)")
    public Object routeTransaction(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        
        DataSourceContextHolder.set(DataSourceContextHolder.DataSourceType.PRIMARY);

        /** Unntil we figure out the replication issues between primary and replica
         * DB instance. Switching back to Primary always.
         */
        // if (transactional.readOnly()) {
        //     DataSourceContextHolder.set(DataSourceContextHolder.DataSourceType.REPLICA);
        // } else {
        //     DataSourceContextHolder.set(DataSourceContextHolder.DataSourceType.PRIMARY);
        // }

        try {
            return joinPoint.proceed();
        } finally {
            // Essential to prevent ThreadLocal cross-talk leakage on pooled worker threads
            DataSourceContextHolder.clear();
        }
    }
}

