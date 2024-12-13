# 베이스 이미지 설정
FROM openjdk:17-jdk-slim

# 작업 디렉터리 생성
WORKDIR /app

# 애플리케이션 JAR 복사
COPY build/libs/order-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 포트 노출
EXPOSE 19094

# 환경 변수 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
