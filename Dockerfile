# Multi-stage Dockerfile: build with Maven, run with JRE
# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
# Copy everything and build
COPY . /workspace
# Build the careverify-api module and its reactor dependencies. The api module itself will run spring-boot repackage (configured in careverify-api/pom.xml).
RUN mvn -T1C -DskipTests clean package -pl careverify-api -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the API module jar from the build stage
COPY --from=build /workspace/careverify-api/target/careverify-api-1.0.0.jar /app/app.jar
# Expose the port the app runs on
EXPOSE 8080
# Use a non-root user (optional)
#RUN addgroup -S appgroup && adduser -S appuser -G appgroup
#USER appuser

ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
