# Spring Boot 4.0 Dependency Conflicts - Resolution Guide

## Overview
This document outlines all the dependency conflicts that arose during migration to Spring Boot 4.0 and their resolutions.

---

## 1. Package Reorganization Issues

### Issue: Removed JPA Auto-Configuration Classes

**Error:**
```
package org.springframework.boot.autoconfigure.orm.jpa does not exist
package org.springframework.boot.orm.jpa does not exist
cannot find symbol: EntityManagerFactoryBuilder
cannot find symbol: JpaProperties
```

**Root Cause:**
Spring Boot 4.0 removed auto-configuration builder classes that were deprecated in favor of direct auto-configuration.

**Resolution:**
Remove manual JPA configuration and rely on Spring Boot's auto-configuration:

**Before (SB 3.x):**
```java
@Bean
@Primary
public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        DataSource primaryDataSource,
        EntityManagerFactoryBuilder builder,
        JpaProperties jpaProperties) {
    return builder
            .dataSource(primaryDataSource)
            .packages("com.bookstore.api.entity")
            .properties(jpaProperties.getProperties())
            .build();
}
```

**After (SB 4.0):**
```java
// Removed - Spring Boot auto-configures this based on @Primary DataSource
// and spring.jpa.properties settings in application.yml
```

---

### Issue: Removed JDBC DataSourceProperties

**Error:**
```
package org.springframework.boot.autoconfigure.jdbc does not exist
```

**Root Cause:**
`DataSourceProperties` was moved to internal packages and should not be injected directly.

**Resolution:**
Use `@ConfigurationProperties` on HikariDataSource beans instead:

**Before (SB 3.x):**
```java
@Bean
@Primary
public DataSource primaryDataSource(
        org.springframework.boot.autoconfigure.jdbc.DataSourceProperties properties) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(properties.getUrl());
    // ... set other properties
    return new HikariDataSource(config);
}
```

**After (SB 4.0):**
```java
@Bean("primaryDataSource")
@Primary
@ConfigurationProperties(prefix = "spring.datasource.primary")
public HikariDataSource primaryDataSource() {
    HikariConfig config = new HikariConfig();
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(2);
    config.setConnectionTimeout(20000);
    config.setIdleTimeout(300000);
    config.setPoolName("primary-pool");
    return new HikariDataSource(config);
}
```

**Configuration (application.yml):**
```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/bookstore
      username: bookuser
      password: bookpass123
      driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 2. Spring Security Import Changes

### Issue: Wrong PreAuthorize Import Location

**Error:**
```
package org.springframework.security.access.preauthorize does not exist
cannot find symbol: class PreAuthorize
```

**Root Cause:**
Spring Security 6.2+ reorganized the security access control packages.

**Resolution:**
Use the correct import from `prepost` package:

**Before (Spring Security 5.x):**
```java
import org.springframework.security.access.preauthorize.PreAuthorize;

@PreAuthorize("hasRole('USER')")
public ResponseEntity<Book> getBook(@PathVariable Long id) {
    // ...
}
```

**After (Spring Security 6.2+):**
```java
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('USER')")
public ResponseEntity<Book> getBook(@PathVariable Long id) {
    // ...
}
```

---

### Issue: Wrong Qualifier Import

**Error:**
```
package org.springframework.qualifier does not exist
cannot find symbol: class Qualifier
```

**Root Cause:**
`@Qualifier` was never in a `qualifier` package - incorrect import was used.

**Resolution:**
Use correct import from `beans.factory.annotation`:

**Before (Incorrect):**
```java
import org.springframework.qualifier.Qualifier;

@Component
public class MyClass {
    public MyClass(@Qualifier("replicaDataSource") DataSource replicaDS) {
        // ...
    }
}
```

**After (Correct):**
```java
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class MyClass {
    public MyClass(@Qualifier("replicaDataSource") DataSource replicaDS) {
        // ...
    }
}
```

---

## 3. Reactive vs. Servlet Mode Conflicts

### Issue: API Gateway Using Reactive Classes in MVC Mode

**Error:**
```
cannot access org.springframework.security.oauth2.jwt.Jwt
  class file for org.springframework.security.oauth2.jwt.Jwt not found
cannot find symbol: setJwtAuthenticationConverter
  method not found in ReactiveJwtAuthenticationConverter
```

**Root Cause:**
The API Gateway was configured with reactive WebFlux dependencies but deployed as servlet-based MVC.

**Resolution:**
Convert API Gateway from reactive to servlet model:

**Before (Reactive/WebFlux):**
```xml
<!-- POM -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- SecurityConfig.java -->
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(authz -> authz.anyExchange().authenticated());
        return http.build();
    }
}
```

**After (Servlet/MVC):**
```xml
<!-- POM -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webmvc</artifactId>
</dependency>

<!-- SecurityConfig.java -->
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated());
        return http.build();
    }
}
```

**application.yml changes:**
```yaml
# From:
spring:
  cloud:
    gateway:
      # Reactive config
      
# To:
spring:
  cloud:
    gateway:
      # MVC config - still works with webmvc variant
```

---

## 4. Dependency Management Improvements

### Parent POM Enhancements

**Added to `<dependencyManagement>`:**

```xml
<dependencyManagement>
    <dependencies>
        <!-- Explicit version management for transitive deps -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <version>1.5.5.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Child POMs now simplified:**
```xml
<!-- Before: hardcoded versions -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.4</version>  <!-- ❌ Outdated -->
</dependency>

<!-- After: uses parent BOM -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <!-- Version inherited from parent -->
</dependency>
```

---

## 5. Compiler Configuration Updates

### Java Version Migration

**Before (Java 17):**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>17</source>
        <target>17</target>
        <!-- ... -->
    </configuration>
</plugin>
```

**After (Java 25 with virtual threads):**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <source>25</source>
        <target>25</target>
        <!-- Virtual threads automatically available -->
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 6. Virtual Threads Configuration

### Enabling Virtual Threads in All Services

**application.yml (all three services):**
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Benefits:**
- Reduced memory footprint (~1 KB per virtual thread vs 1 MB per OS thread)
- Better throughput for I/O-bound operations
- Simpler asynchronous code (no reactive needed)
- Direct compatibility with servlet model

---

## Summary of Files Modified

| Module | File | Change Type | Severity |
|--------|------|------------|----------|
| book-api | DataSourceConfig.java | Source code rewrite | HIGH |
| book-api | JpaConfig.java | Simplified | MEDIUM |
| book-api | JdbcConfig.java | Import fix | LOW |
| book-api | ReadFallbackTemplate.java | Import fix | LOW |
| book-api | BookController.java | Import fix | LOW |
| book-api | OrderController.java | Import fix | LOW |
| book-api | pom.xml | Compiler update | MEDIUM |
| book-api | application.yml | Config addition | LOW |
| auth-server | pom.xml | Compiler update | MEDIUM |
| auth-server | application.yml | Config addition | LOW |
| api-gateway | SecurityConfig.java | Source code rewrite | HIGH |
| api-gateway | pom.xml | Dependency & compiler update | HIGH |
| api-gateway | application.yml | Config addition | LOW |
| root | pom.xml | Dependency management | MEDIUM |

---

## Testing Checklist

- [ ] `mvn clean install` completes successfully
- [ ] All three JAR files generated without errors
- [ ] No deprecation warnings in compilation
- [ ] Docker containers start: `docker-compose up -d`
- [ ] Auth Server starts on port 8082
- [ ] Book API starts on port 8081
- [ ] API Gateway starts on port 8080
- [ ] Token generation works from Auth Server
- [ ] Token validation works through API Gateway
- [ ] Book CRUD operations work through Gateway
- [ ] Dual data source fallback works (stop replica, test read)
- [ ] Virtual threads are active in logs

