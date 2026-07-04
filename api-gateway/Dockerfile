# Stage 1: Extract the application layers using the Spring Boot jarmode tool
FROM eclipse-temurin:25-jre-jammy AS builder
WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2: Build the minimal, production/development runtime image
FROM eclipse-temurin:25-jre-jammy
WORKDIR /application

RUN groupadd -r -g 999 bookstoregrp && useradd -r -u 999 -g bookstoregrp -m bookstoreuser
RUN mkdir /application/data && chown bookstoreuser:bookstoregrp /application/data

# Copy each layer individually. Docker will cache these layers dynamically!
COPY --from=builder --chown=bookstoreuser:bookstoregrp /application/dependencies/ ./
COPY --from=builder --chown=bookstoreuser:bookstoregrp /application/spring-boot-loader/ ./
COPY --from=builder --chown=bookstoreuser:bookstoregrp /application/snapshot-dependencies/ ./
COPY --from=builder --chown=bookstoreuser:bookstoregrp /application/application/ ./

USER bookstoreuser:bookstoregrp

# Use Spring Boot's optimized JarLauncher instead of raw java -jar
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
