# Spring-Travel
Spring Boot를 이용한 여행 계획 사이트
<br><br>

## 프로젝트 소개
Spring Boot와 OpenAPI(TourAPI, Kakao map API)를 이용하여 제작한 여행 계획 사이트로, 사용자는 관광지 목록을 보고 선택하여 지도위에서 위치를 확인 할 수 있습니다.
<br><br>

## 개발 인원
- 1명
<br><br>

## 개발 기간
- 2024.01.19 ~ 2024.02.09
<br><br>

## 개발 환경
- Java 17
- SpringBoot 3.2.1
- Thymeleaf
- MySQL
- Spring Security 
- JWT
- RestDocs
<br><br>

## 개발하면서 생겼던 문제점 및 해결방법
- html form에서 아이디 중복체크 후 input을 disabled하였더니 서버로 아이디가 안넘어감
    - disabled를 readonly로 변경하여 해결
- 프론트단에서 서버로 데이터 전송을 어느 부분에서는 form 형식으로, 어느 부분에서는 json 형식으로 일관성이 없어 개발 중 헷갈림
    - ajax를 이용해 모든 데이터 전송 형식을 json으로 통일하여 해결
- ajax를 이용해 시큐리티 로그인을 처리하니 서버단에서 response.sendRedirect("/");이 먹히지 않음
    - response.setStatus(HttpServletResponse.*SC_OK*);를 통해 상태를 OK로 설정하고 페이지 이동은 프론트단에서 처리하여 해결
- 서비스 테스트 코드에서 private으로 설정한 idx 값을 설정이 안됨
    - ReflectionTestUtils.setField()를 통해 private으로 설정된 필드 값을 설정하여 해결
- 엔티티 다대일 관계에서 무한 참조가 발생
    - @JsonIgnoreProperties를 통해 해결
<br><br>

## 페이지 소개
### 홈
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/b8311d79-1b2a-41e4-aee1-b9a921de2c3c)
<br><hr>

### 로그인 및 회원가입
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/e968025e-4ecc-4de4-8aa7-23c8f90eb4c0)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/c075ec76-b894-4706-834d-3b9a87c8d177)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/59ea12aa-7ddd-429d-a0f2-d044ed4d9ab0)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/be50f22b-a87d-4b79-a386-af8548921985)
<br><hr>

### 마이페이지
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/b9fbcefd-43eb-490f-96ed-24794b696522)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/ad942ee3-e170-4ba1-b732-0f9ab4ca6cec)
<br><hr>

### 마이 플랜
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/6a20ced6-fd95-459d-9877-97d44bfc3bae)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/b7f7105f-cd22-486c-87df-3ca8cc029661)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/51d97ee9-8821-476f-bf0f-9aba894c5fdd)
![image](https://github.com/magic7549/Spring-Travel/assets/32091601/8c41d6ca-4c64-4e30-8c20-2e04864fca52)
![Screenshot 2024-02-14 at 18 54 00](https://github.com/magic7549/Spring-Travel/assets/32091601/b631aa80-deb9-418d-a5e0-ef3667e1f22c)


