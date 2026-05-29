# Sử dụng official Maven image để build
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml và download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# ============================================
# Runtime stage - sử dụng Eclipse Temurin 17 (lightweight)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy jar từ builder stage
COPY --from=builder /app/target/travelist-backend-*.jar app.jar

# Expose port (Render mặc định sử dụng $PORT)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

