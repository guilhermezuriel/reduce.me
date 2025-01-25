FROM openjdk:21
LABEL authors="guilhermezuriel"

COPY target/reduceme.jar app/reduceme.jar
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "reduceme.jar"]