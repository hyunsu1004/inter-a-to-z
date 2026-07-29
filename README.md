# INTER A to Z

인터엑스(interX) 핵심가치 연계형 신규 입사자 온보딩 포털입니다. AX 인턴 과제로 기획부터 개발, 배포까지 직접 진행했습니다. 12가지 핵심가치를 카드 스와이프 형태로 학습하고, 미션 수행과 게이미피케이션(포인트·스트릭·배지)을 통해 자연스럽게 회사 문화에 녹아들 수 있도록 설계했습니다.

[![CI](https://github.com/hyunsu1004/inter-a-to-z/actions/workflows/ci.yml/badge.svg)](https://github.com/hyunsu1004/inter-a-to-z/actions/workflows/ci.yml)

## 🔗 Live Demo

| | URL |
|---|---|
| 프론트엔드 | https://eloquent-analysis-production-2285.up.railway.app |
| 백엔드 API | https://inter-a-to-z-production.up.railway.app |

**심사/체험용 계정**

| 구분 | 이메일 | 비밀번호 |
|---|---|---|
| 신입사원 (일반 사용자) | `newbie@interx.io` | `demo1234!` |
| 인사팀 관리자 | `admin@interx.io` | `admin1234!` |

## ✨ 주요 기능

- **핵심가치 학습**: 12가지 핵심가치를 INTRO → 상황(SITUATION) → 퀴즈 순서의 카드 스와이프 UI로 학습 (Framer Motion 애니메이션)
- **게이미피케이션**: 활동 시 포인트 적립, 연속 접속 스트릭, 15종 배지(핵심가치 완료/스트릭/올클리어) 자동 지급
- **미션 시스템**: 신입사원이 미션을 제출하면 관리자가 검토·승인/반려, 승인 시 보너스 포인트 지급
- **AI 챗봇**: 규칙 기반 mock 엔진과 OpenAI(gpt-4o-mini) 연동을 런타임에 전환 가능한 이중 구조. 회사 핵심가치 데이터를 시스템 프롬프트에 동적으로 주입
- **피드백 채널**: 신입사원 ↔ 인사팀 양방향 커뮤니케이션
- **관리자 대시보드**: 전체 신입사원 현황, 미션 등록/검토, 피드백 관리
- **JWT 인증**: 액세스(1시간)+리프레시(14일) 토큰 발급/로테이션, 프론트엔드에서 액세스 토큰 만료 시 자동 재발급
- **로그인 브루트포스 방지**: 5회 연속 실패 시 5분간 계정 잠금
- **다크모드**: OS 설정 자동 감지, 사용자 선택 저장
- **자동화 테스트 & CI**: JUnit5 + Mockito + AssertJ 단위 테스트, GitHub Actions로 push/PR마다 백엔드 테스트 + 프론트엔드 빌드 자동 실행

## 🛠 기술 스택

**Backend**
- Java 17, Spring Boot 3.3.4
- Spring Security (Stateless JWT 인증, `jjwt` 0.12.6)
- Spring Data JPA / Hibernate
- MySQL(운영) / H2(로컬)
- Bean Validation, Lombok
- JUnit 5, Mockito, AssertJ

**Frontend**
- React 19 + Vite 8
- React Router 6
- Framer Motion (카드 스와이프 애니메이션)
- Axios (JWT 자동 첨부 + 401 시 자동 토큰 재발급 인터셉터)

**Infra / DevOps**
- Railway (Nixpacks 기반 백엔드/프론트엔드/MySQL 배포)
- GitHub Actions CI

## 📁 프로젝트 구조

```
inter-a-to-z/
├── backend/                        # Spring Boot 백엔드
│   ├── src/main/java/com/interx/onboarding/
│   │   ├── config/                 # SecurityConfig, DataSeeder(시드 데이터 및 스키마 보정)
│   │   ├── controller/             # REST 컨트롤러
│   │   ├── domain/                 # JPA 엔티티
│   │   ├── dto/                    # 요청/응답 DTO (record)
│   │   ├── exception/              # 전역 예외 처리(GlobalExceptionHandler)
│   │   ├── repository/             # Spring Data JPA 리포지토리
│   │   ├── security/               # JwtUtil, JwtAuthFilter
│   │   └── service/                # 비즈니스 로직
│   ├── src/main/resources/
│   │   ├── application.yml         # local/prod 프로필 설정
│   │   └── data.sql                # 핵심가치·카드·배지 등 초기 시드 데이터
│   ├── src/test/java/...           # 단위 테스트
│   ├── nixpacks.toml               # Railway 빌드 설정
│   └── pom.xml
├── frontend/                       # React + Vite 프론트엔드
│   └── src/
│       ├── api/client.js           # Axios 인스턴스 (JWT 인터셉터)
│       ├── components/             # Chatbot 등 공용 컴포넌트
│       ├── context/                # AuthContext, ThemeContext
│       ├── pages/                  # LoginPage, DashboardPage, ValueLearningPage, MissionsPage, AdminPage
│       └── theme.css               # CSS 변수 기반 라이트/다크 테마
├── .github/workflows/ci.yml        # 백엔드 테스트 + 프론트엔드 빌드 CI
└── 기획안.md                        # 기능/DB/화면흐름 기획 문서
```

## 🔌 API 개요

| 도메인 | Base Path | 설명 |
|---|---|---|
| 인증 | `/api/auth` | 회원가입, 로그인, 토큰 재발급 (`/signup`, `/login`, `/refresh`) |
| 대시보드 | `/api/dashboard` | 로그인한 사용자의 포인트/스트릭/배지/미션 현황 |
| 핵심가치 | `/api/core-values` | 핵심가치 목록, 카드 조회, 퀴즈 제출 |
| 미션 | `/api/missions` | 미션 목록 조회, 제출 |
| 피드백 | `/api/feedbacks` | 피드백 작성, 내 피드백 조회 |
| 챗봇 | `/api/chatbot` | AI 챗봇 질의응답 |
| 관리자 | `/api/admin` | 사용자 현황, 미션 등록/검토, 피드백 관리 (ADMIN 권한 필요) |

## 🚀 로컬 실행

### 백엔드 (JDK 17 + Maven)

```bash
cd backend
mvn spring-boot:run
```

기본 `local` 프로필은 인메모리 H2 DB를 사용하며, 기동 시 12가지 핵심가치·카드·퀴즈·배지·샘플 미션과 테스트 계정이 자동으로 시딩됩니다. API는 `http://localhost:8080` 에서 확인할 수 있습니다.

### 프론트엔드 (Node 18+)

```bash
cd frontend
npm install
cp .env.example .env   # VITE_API_BASE_URL을 백엔드 주소로 설정
npm run dev
```

`http://localhost:5173` 에서 접속합니다.

### 테스트 실행

```bash
cd backend
mvn test
```

## ⚙️ 주요 환경변수 (운영 배포 시)

| 변수 | 설명 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `MYSQLHOST` / `MYSQLPORT` / `MYSQLDATABASE` / `MYSQLUSER` / `MYSQLPASSWORD` | Railway MySQL 서비스 연결 시 자동 주입 |
| `JWT_SECRET` | JWT 서명 키 (32자 이상 권장) |
| `JWT_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` | 액세스/리프레시 토큰 만료 시간(ms), 기본 1시간/14일 |
| `CORS_ALLOWED_ORIGINS` | 프론트엔드 배포 URL |
| `CHATBOT_PROVIDER` / `CHATBOT_API_KEY` | `openai`로 설정 시 실제 LLM 연동, 미설정 시 규칙 기반 mock 챗봇으로 동작 |

## 📄 기획 문서

프로젝트의 기능 설계, ERD, 화면 흐름 등 상세 기획 내용은 [`기획안.md`](./기획안.md)에서 확인할 수 있습니다.
