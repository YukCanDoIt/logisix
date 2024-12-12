FROM eclipse-temurin:17-jdk-jammy
LABEL authors="hanti"

WORKDIR /app
COPY build/libs/order-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
