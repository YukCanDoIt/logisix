# 애플리케이션 빌드
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# Gradle Wrapper 및 프로젝트 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/
COPY src /app/src

# 실행 권한 부여 및 테스트를 제외하고 애플리케이션 빌드
RUN chmod +x /app/gradlew
RUN ./gradlew build -x test --info

# 우분투 기반 실행환경 구성
FROM ubuntu:20.04

# JRE 설치
RUN apt-get update && apt-get install -y openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*

# JAR 파일을 app 내 복사
COPY --from=build /app/build/libs/eureka-server-*.jar /eureka-server.jar

# 애플리케이션 사용 포트번호
EXPOSE 19090

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/eureka-server.jar"]
