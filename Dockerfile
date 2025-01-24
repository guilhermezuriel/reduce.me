FROM openjdk:21
LABEL authors="guilhermezuriel"

WORKDIR /app
COPY target/seu-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]