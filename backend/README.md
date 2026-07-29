# INTER A to Z — Backend (Spring Boot)

인터엑스 핵심가치 연계형 신규 입사자 온보딩 포털 백엔드.

## 로컬 실행 (JDK 17 + Maven 필요)

```bash
cd backend
mvn spring-boot:run
```

기본 프로필은 `local`이며 인메모리 H2 DB를 사용합니다. 서버 기동 시 `data.sql`이 자동 실행되어
12가지 핵심가치, 카드, 퀴즈, 배지, 샘플 미션이 시딩되고, 관리자/데모 계정도 자동 생성됩니다.

- API 베이스: `http://localhost:8080`
- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:onboarding`)

## 테스트 계정 (기본값, 환경변수로 변경 가능)

| 구분 | 이메일 | 비밀번호 |
|---|---|---|
| 관리자(인사팀) | admin@interx.io | admin1234! |
| 신입사원(데모) | newbie@interx.io | demo1234! |

## 주요 환경변수 (Railway 배포 시 설정)

| 변수 | 설명 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` 로 설정 |
| `MYSQLHOST` `MYSQLPORT` `MYSQLDATABASE` `MYSQLUSER` `MYSQLPASSWORD` | Railway MySQL 플러그인 연결 시 자동 주입 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상 임의 문자열) |
| `CORS_ALLOWED_ORIGINS` | 배포된 프론트엔드 URL (콤마로 여러 개 가능) |
| `ADMIN_EMAIL` `ADMIN_PASSWORD` `DEMO_EMAIL` `DEMO_PASSWORD` | 테스트 계정 커스터마이즈 |
| `CHATBOT_PROVIDER` | `mock`(기본, API 키 불필요) 또는 `openai` |
| `CHATBOT_API_KEY` | `CHATBOT_PROVIDER=openai` 일 때만 필요 |

## API 요약

- `POST /api/auth/signup`, `POST /api/auth/login`
- `GET /api/dashboard`
- `GET /api/core-values`, `GET /api/core-values/{id}/cards`, `POST /api/core-values/quiz/submit`
- `GET /api/missions`, `POST /api/missions/{id}/submit`
- `POST /api/chatbot/ask`
- `POST /api/feedbacks`, `GET /api/feedbacks/me`
- `GET /api/admin/users`, `GET /api/admin/missions`, `POST /api/admin/missions`,
  `GET /api/admin/missions/submissions`, `POST /api/admin/missions/submissions/{id}/review`,
  `GET /api/admin/feedbacks`, `POST /api/admin/feedbacks/{id}/reply`
