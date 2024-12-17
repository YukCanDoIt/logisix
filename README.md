<div align="center"> 
  <h1>🚚 물류 배송 시스템 (Logistics Delivery System)</h1>
</div>

<div align="right">  <h3>YukCanDoIt 6조</h3> </div>
<br>

## 프로젝트 진행 기간
2024.12.05 - 2024.12.17

## 프로젝트 개요

마이크로서비스 아키텍처 (MSA) 기반으로 설계된 물류 배송 관리 시스템

주문, 사용자 관리, 배송 추적, 알림 등을 각각의 독립된 서비스로 분리해 유연성과 확장성을 도모

## 🧑🏻‍💻 팀 구성

<table style="width: 100%; text-align: center;">
<tbody>
<tr>
<td style="text-align: center;">
<a href="https://github.com/ewoo14">
<img src="" width="100px;" alt="프로필이미지"/>
<br />
<sub><b>박은우</b></sub>
<br />
</a>
<span>user, gateway</span>
</td>
<td style="text-align: center;">
<a href="https://github.com/sooooooongyi">
<img src="" width="100px;" alt="프로필이미지"/>
<br />
<sub><b>박송이</b></sub>
<br />
</a>
<span>core</span>
</td>
<td style="text-align: center;">
<a href="https://github.com/le-monaaa">
<img src="docs/yubin.jpeg" width="100px;" alt="프로필이미지"/>
<br />
<sub><b>이유빈</b></sub>
</a>
<br />
<span>delivery</span>
</td>
<td style="text-align: center;">
<a href="https://github.com/HanBeom98">
<img src="" width="100px;" alt="프로필이미지"/>
<br />
<sub><b>조한범</b></sub>
<br />
</a>
<span>order</span>
</td>
</tr>
</tbody>
</table>

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

## API Docs
[API 명세서](https://www.notion.so/teamsparta/c64204c1b9804be39687492c54d661b6?v=1532dc3ef5148189975c000c175cb7b0&p=1b76e1b74b0149a4a52ef816bf0fa4db&pm=s)

## 개발 환경

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white) ![JPA](https://img.shields.io/badge/JPA-6DB33F?style=for-the-badge&logo=hibernate&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white) ![QueryDSL](https://img.shields.io/badge/QueryDSL-005571?style=for-the-badge&logo=hibernate&logoColor=white)  ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![KakaoMap API](https://img.shields.io/badge/KakaoMap%20API-FFCD00?style=for-the-badge&logo=kakao&logoColor=black)  ![Google Gemini AI](https://img.shields.io/badge/Google%20Gemini%20AI-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white) ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)

## ERD

<img src="docs/erd-image.png" alt="ERD"/>

## 실행 방법
1. Docker Desktop 설치
2. logisix 최상위 루트에서 `docker-compose up -d` 입력
3. 컨테이너 생성 후 `http://gateway:19091/api/v1/core/` 경로로 API 호출
