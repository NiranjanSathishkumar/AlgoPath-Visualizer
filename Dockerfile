# ─────────────────────────────────────────────
#  Stage 1 — Build the JAR with Maven
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy dependency manifests first for Docker layer caching
COPY pom.xml .
COPY src ./src

# Build, skipping tests (tests need a running env)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# ─────────────────────────────────────────────
#  Stage 2 — Lean runtime image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the executable JAR
COPY --from=builder /app/target/algopath-visualizer.jar app.jar

# Expose default port (overridden by PORT env var at runtime on Render)
EXPOSE 8080

# Start the application — Spring reads PORT env var via application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]
