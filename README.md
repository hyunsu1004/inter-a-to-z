# INTER A to Z

인터엑스 핵심가치 연계형 신규 입사자 온보딩 포털 (AX 인턴 과제 프로토타입)

- Backend: Spring Boot 3 (Java 17) + Spring Security(JWT) + JPA + MySQL(prod)/H2(local)
- Frontend: React + Vite + React Router + Framer Motion
- 상세 기획: `기획안.md` 참고

## 폴더 구조

```
inter-a-to-z/
  backend/    Spring Boot 프로젝트 (pom.xml, src/...)
  frontend/   React + Vite 프로젝트 (package.json, src/...)
  기획안.md    기능/DB/화면흐름 기획 문서
```

## 로컬에서 실행하기

### 1) 백엔드 (JDK 17 + Maven 필요)

```bash
cd backend
mvn spring-boot:run
```

기본 `local` 프로필은 인메모리 H2 DB를 사용하며, 기동 시 12가지 핵심가치·카드·퀴즈·배지·
샘플 미션과 테스트 계정이 자동 시딩됩니다. API는 `http://localhost:8080`.

### 2) 프론트엔드 (Node 18+ 권장)

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

`http://localhost:5173` 에서 접속. `.env`의 `VITE_API_BASE_URL`이 백엔드 주소를 가리켜야 합니다.

## 테스트 계정 (심사용)

| 구분 | 이메일 | 비밀번호 |
|---|---|---|
| 신입사원(데모) | newbie@interx.io | demo1234! |
| 인사팀 관리자 | admin@interx.io | admin1234! |

## Railway 배포 가이드 (백엔드 + MySQL)

1. Railway에서 새 프로젝트 생성 → "New" → "Database" → **MySQL** 추가
2. 같은 프로젝트에 "New" → "GitHub Repo" (또는 "Empty Service"로 이 `backend/` 폴더 배포)
   - 저장소를 쓰는 경우 Root Directory를 `backend`로 지정
   - `backend/nixpacks.toml`이 자동으로 JDK17+Maven 빌드/실행을 설정합니다
3. 백엔드 서비스의 "Variables" 탭에서:
   - MySQL 서비스를 "Connect"로 연결하면 `MYSQLHOST/MYSQLPORT/MYSQLDATABASE/MYSQLUSER/MYSQLPASSWORD`가 자동 주입됩니다
   - `SPRING_PROFILES_ACTIVE=prod` 추가
   - `JWT_SECRET` = 임의의 32자 이상 문자열
   - `CORS_ALLOWED_ORIGINS` = 배포된 프론트엔드 URL
   - (선택) `CHATBOT_PROVIDER=openai`, `CHATBOT_API_KEY=...` — 설정 안 하면 자동으로 규칙기반
     mock 챗봇이 동작하므로 데모 안정성엔 문제 없습니다
4. Deploy 후 발급된 도메인(예: `https://xxxx.up.railway.app`)이 백엔드 API 주소입니다

## Railway 배포 가이드 (프론트엔드)

1. 같은 Railway 프로젝트에 "New" → "Empty Service" (또는 별도 Vercel/Netlify 사용 가능)
2. Root Directory `frontend`, Build Command `npm run build`, Start Command는 정적 서빙이 필요하므로
   `npx serve dist -l $PORT` 사용 권장 (또는 Vercel/Netlify에 정적 배포 시 자동 처리)
3. 환경변수 `VITE_API_BASE_URL` = 위에서 발급받은 백엔드 URL
4. 배포된 프론트 URL을 다시 백엔드의 `CORS_ALLOWED_ORIGINS`에 반영 후 재배포

## 제출 체크리스트 관련 메모

- 라이브 URL은 프론트엔드 배포 주소를 제출하고, PPT에 위 테스트 계정을 명시하면 됩니다
- 8/28까지 접속 유지가 필요하므로 Railway 무료 크레딧 소진 여부를 주기적으로 확인해주세요
