# 빌드 환경 설정
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/
COPY src /app/src

# 실행 권한 부여
RUN chmod +x /app/gradlew

# 단위 테스트를 제외하고 애플리케이션 빌드
RUN ./gradlew build -x test --info

# 실행 환경 설정
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/order-*.jar app.jar

# JVM 환경 변수 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 애플리케이션 사용 포트 설정
EXPOSE 19094

# JAR 파일 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
