# Gradle 빌드 단계
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle /app/

# 소스 코드 복사
COPY src /app/src

# 애플리케이션 빌드
RUN gradle build -x test

# 경량화된 실행 환경
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/delivery-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 포트 노출
EXPOSE 19097

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
