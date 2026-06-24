# Stage 1: Extract the application layers using the Spring Boot jarmode tool
FROM eclipse-temurin:25-jre-jammy AS builder
WORKDIR /application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2: Build the minimal, production/development runtime image
FROM eclipse-temurin:25-jre-jammy
WORKDIR /application
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Copy each layer individually. Docker will cache these layers dynamically!
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

# Use Spring Boot's optimized JarLauncher instead of raw java -jar
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
