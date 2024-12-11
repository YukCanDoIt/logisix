# app 내 gradle 이미지 선택
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# Gradle 빌드 파일 복사
COPY build.gradle settings.gradle /app/

# 소스 코드 복사
COPY src /app/src

# 단위 테스트를 제외하고 애플리케이션 빌드
RUN gradle build -x test

# 우분투 기반 이미지를 사용하여 실행 환경을 준비
FROM ubuntu:20.04

# JRE 설치
RUN apt-get update && apt-get install -y openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*

# 빌드 스테이지에서 생성된 JAR 파일을 복사
COPY --from=build /app/build/libs/gateway-service-*.jar /gateway-service.jar

# 게이트웨이 서비스의 포트 오픈
EXPOSE 19091

# 게이트웨이 서비스 실행
ENTRYPOINT ["java", "-jar", "/gateway-service.jar"]