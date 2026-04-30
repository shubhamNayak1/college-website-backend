# ---- Stage 1: build the jar with Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (changes rarely)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Then copy sources and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Stage 2: slim runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as non-root for security
RUN useradd --system --uid 1001 --no-create-home spring
USER spring

COPY --from=build /app/target/portal-backend-0.1.0.jar app.jar

# Render injects $PORT; default to 8080 for local docker run
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT}"]
