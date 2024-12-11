# app 내 gradle 이미지 선택
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# Gradle 설정 파일 및 Wrapper 스크립트 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/

# 소스 코드 복사
COPY src /app/src

# 실행 권한 부여
RUN chmod +x /app/gradlew

# 단위 테스트를 제외하고 애플리케이션 빌드
RUN ./gradlew build -x test --info

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