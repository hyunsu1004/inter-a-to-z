# INTER A to Z — Frontend (React + Vite)

인터엑스 핵심가치 연계형 신규 입사자 온보딩 포털 프론트엔드.

## 로컬 실행

```bash
cd frontend
npm install
cp .env.example .env   # VITE_API_BASE_URL을 백엔드 주소로 설정
npm run dev
```

## 빌드

```bash
npm run build
```

`dist/` 폴더가 생성됩니다. Railway 등에 정적 배포 시 이 폴더를 서빙하거나,
`npm run preview`로 로컬에서 프로덕션 빌드를 확인할 수 있습니다.

## 화면 구성

- `/login` — 로그인 / 회원가입
- `/` — 홈 대시보드 (스트릭·포인트·배지, 이번주 미션, 핵심가치 12종 그리드)
- `/values/:id` — 핵심가치 카드 스와이프 학습 + 퀴즈
- `/missions` — 주차별 미션 제출
- `/admin` — 인사팀 대시보드 (진행현황 / 미션 검토 / 피드백함), ADMIN 계정만 접근 가능
- 우측 하단 플로팅 버튼 — AI 챗봇
