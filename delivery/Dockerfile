# Gradle 빌드 단계
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle Wrapper 및 프로젝트 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/
COPY src /app/src

# 실행 권한 부여 및 애플리케이션 빌드
RUN chmod +x /app/gradlew && ./gradlew build -x test --info

# 경량화된 실행 환경
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/delivery-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 포트 노출
EXPOSE 19097

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
