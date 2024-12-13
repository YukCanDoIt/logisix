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
FROM ubuntu:20.04

# JRE 설치
RUN apt-get update && apt-get install -y openjdk-17-jre-headless && rm -rf /var/lib/apt/lists/*

# 빌드된 JAR 파일을 컨테이너로 복사
COPY --from=build /app/build/libs/slack-service-*.jar /slack-service.jar

# 애플리케이션 사용 포트번호 설정
EXPOSE 19098

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/slack-service.jar"]
