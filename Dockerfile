FROM openjdk:21
LABEL authors="guilhermezuriel"

COPY target/reduceme.jar app/reduceme.jar
EXPOSE 8080
WORKDIR app

CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "reduceme.jar"]
