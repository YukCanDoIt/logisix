# 애플리케이션 빌드
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# gradle 설정 파일, gradlew 스크립트, 그리고 wrapper 파일들 복사
COPY build.gradle settings.gradle gradlew /app/
COPY gradle/wrapper /app/gradle/wrapper

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 소스 코드 복사
COPY src /app/src

# 테스트를 제외하고 애플리케이션 빌드
RUN ./gradlew clean build -x test

# 우분투 기반 실행환경 구성
FROM ubuntu:20.04

# JRE 설치
RUN apt-get update && apt-get install -y openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*

# JAR 파일을 app 내 복사
COPY --from=build /app/build/libs/server.jar /eureka-server.jar

# 애플리케이션 사용 포트번호
EXPOSE 19090

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/eureka-server.jar"]