FROM amazoncorretto:21-alpine
ADD build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]