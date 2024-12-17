<div align="center"> 
  <h1>🚚 물류 배송 시스템 (Logistics Delivery System)</h1>
</div>

<div align="right">  <h3>YukCanDoIt 6조</h3> </div>
<br>

## 프로젝트 개요
본 프로젝트는 마이크로서비스 아키텍처 (MSA) 기반으로 설계된 물류 배송 관리 시스템 설계<br>
주문, 사용자 관리, 배송 추적, 알림 등을 각각의 독립된 서비스로 나누어 확장성과 유연성 증가

## 아키텍처 개요
본 시스템은 Spring Cloud 기반으로 구현된 MSA 구조를 사용하며 다음과 같은 핵심 기능을 제공

## 인프라 구성
- Eureka Server: 마이크로서비스 등록 및 디스커버리 서버
- API Gateway: 외부 요청에 대한 JWT 인증과 라우팅 처리
- User Service: 사용자 관리
- Core Service: 주문, 업체, 상품 관리
- Delivery Service: 배송 추적 및 관리
- Slack Service: Slack 알림 전송
- Database: PostgreSQL을 통한 데이터 관리 (서비스마다 분산)
- Zipkin: 분산 추적을 통해 서비스 간 호출 흐름을 시각화

![스크린샷 2024-12-17 오후 4 36 46](https://github.com/user-attachments/assets/7a95115b-ab2e-4ad9-a800-32ef3693a0ec)

## 설정
1. Docker Desktop 설치
2. logisix 최상위 루트에서 `docker-compose up -d` 입력
3. 컨테이너 생성 후 `http://gateway:19091/api/v1/core/` 경로로 API 호출
