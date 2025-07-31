# Build
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime
FROM openjdk:21
WORKDIR /app
COPY --from=builder /app/target/reduceme.jar reduceme.jar
EXPOSE 8080
CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "reduceme.jar"]