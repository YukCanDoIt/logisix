# 빌드 환경 설정
FROM gradle:7.3.3-jdk17 AS build
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle settings.gradle /app/

# 소스 코드 및 기타 필요 파일 복사
COPY src /app/src

# 실행 권한 부여 및 빌드 실행
RUN chmod +x /app/gradlew
RUN ./gradlew clean build -x publishToMavenLocal -x test