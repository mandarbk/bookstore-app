package com.bookstore.ds.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FlywayMigrationInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final Flyway flywayBean;

    FlywayMigrationInitializer(Flyway flywayBean) {
        this.flywayBean = flywayBean;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //log.info("database migration started....");
        flywayBean.migrate();
    }

}
