package com.interx.onboarding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interx.onboarding.domain.ChatbotLog;
import com.interx.onboarding.domain.CoreValue;
import com.interx.onboarding.domain.ValueCard;
import com.interx.onboarding.repository.ChatbotLogRepository;
import com.interx.onboarding.repository.CoreValueRepository;
import com.interx.onboarding.repository.ValueCardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChatbotService {

    private final CoreValueRepository coreValueRepository;
    private final ValueCardRepository valueCardRepository;
    private final ChatbotLogRepository chatbotLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${app.chatbot.provider}")
    private String provider;

    @Value("${app.chatbot.api-key}")
    private String apiKey;

    public ChatbotService(CoreValueRepository coreValueRepository,
                           ValueCardRepository valueCardRepository,
                           ChatbotLogRepository chatbotLogRepository) {
        this.coreValueRepository = coreValueRepository;
        this.valueCardRepository = valueCardRepository;
        this.chatbotLogRepository = chatbotLogRepository;
    }

    public String ask(Long userId, String message) {
        String answer;
        if ("openai".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank()) {
            try {
                answer = askOpenAi(message);
            } catch (Exception e) {
                answer = fallbackAnswer(message);
            }
        } else {
            answer = fallbackAnswer(message);
        }

        ChatbotLog log = ChatbotLog.builder()
                .userId(userId).question(message).answer(answer).build();
        chatbotLogRepository.save(log);
        return answer;
    }

    private String askOpenAi(String message) throws Exception {
        String systemPrompt = buildSystemPrompt();
        ChatCompletionRequest payload = new ChatCompletionRequest(
                "gpt-4o-mini",
                List.of(new ChatMessage("system", systemPrompt), new ChatMessage("user", message)),
                0.6
        );
        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? fallbackAnswer(message) : content.asText(fallbackAnswer(message));
    }

    private String buildSystemPrompt() {
        List<CoreValue> values = coreValueRepository.findAllByOrderBySortOrderAsc();
        StringBuilder sb = new StringBuilder();
        sb.append("너는 인터엑스 신규 입사자 온보딩 포털 'INTER A to Z'의 AI 챗봇이야. ");
        sb.append("친근하고 캐주얼한 톤으로 신입사원의 질문에 답해줘. 아래는 인터엑스의 12가지 핵심가치야:\n");
        for (CoreValue v : values) {
            sb.append("- ").append(v.getName()).append(": ").append(v.getDescription()).append("\n");
        }
        sb.append("답변은 3~4문장 이내로 간결하게 해줘.");
        return sb.toString();
    }

    // =========================================================================
    // Mock 모드: 규칙 기반 응답 엔진
    // 우선순위: 인사말/자기소개 > 특정 핵심가치(토큰 매칭) > 전체 목록 요청
    //          > 기능(미션/포인트/배지/스트릭/로그인/사용법 등) > 감사 인사 > 범용 폴백
    // 같은 카테고리라도 매번 문구를 바꿔가며 답하도록 후보군에서 랜덤 선택한다.
    // =========================================================================
    private String fallbackAnswer(String message) {
        String raw = message == null ? "" : message.trim();
        String m = raw.toLowerCase().replaceAll("\\s+", "");

        // 1) 인사말
        if (matchesAny(m, "안녕", "hi", "hello", "하이", "반가")) {
            return pick(
                    "안녕하세요! INTER A to Z 챗봇이에요 🙂 핵심가치, 미션, 포인트/배지, 로그인 방법까지 뭐든 물어보세요.",
                    "반가워요! 궁금한 건 편하게 물어보세요. 특히 12가지 핵심가치 중 헷갈리는 게 있으면 이름만 말해줘도 설명해드려요."
            );
        }

        // 2) 챗봇 자기소개 / 사용법
        if (matchesAny(m, "너는누구", "너뭐야", "챗봇이뭐", "무엇을도와", "뭘도와줄수", "뭐물어봐야", "사용법", "어떻게써", "어떻게사용")) {
            return pick(
                    "저는 INTER A to Z의 온보딩 도우미예요. '핵심가치 이름', '이번주 미션', '포인트 어떻게 쌓여', '배지 종류', '스트릭이 뭐야' 같은 질문에 답할 수 있어요.",
                    "이렇게 물어보세요 — 예) '집요한 끈기가 뭐야?', '포인트는 어떻게 쌓여?', '이번주 미션 뭐 있어?'. 12가지 핵심가치 이름만 말해도 바로 설명해드려요."
            );
        }

        // 3) 특정 핵심가치 — 이름 전체/부분 토큰 매칭
        List<CoreValue> values = coreValueRepository.findAllByOrderBySortOrderAsc();
        CoreValue matchedValue = findMatchingValue(values, m);
        if (matchedValue != null) {
            return valueAnswer(matchedValue);
        }

        // 4) 핵심가치 전체 목록 요청
        if (matchesAny(m, "핵심가치목록", "핵심가치다", "핵심가치전체", "가치몇개", "가치가뭐가있", "가치리스트", "12가지", "핵심가치가뭐야")) {
            StringBuilder sb = new StringBuilder("인터엑스 핵심가치 12가지예요: ");
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i).getName());
                if (i < values.size() - 1) sb.append(" · ");
            }
            sb.append(". 궁금한 가치 이름을 말해주면 더 자세히 알려드릴게요!");
            return sb.toString();
        }

        // 5) 미션
        if (matchesAny(m, "미션", "과제", "숙제")) {
            return pick(
                    "이번주 미션은 홈 대시보드 상단 '이번주 미션' 배너에서 확인할 수 있어요. 수행 내용을 적어 제출하면 인사팀 검토 후 포인트와 스트릭이 올라가요.",
                    "미션은 주차별로 새로 등록돼요. '미션 전체보기'에서 진행 전/검토중/승인 상태를 한눈에 볼 수 있고, 승인되면 보너스 포인트도 함께 지급돼요."
            );
        }

        // 6) 포인트
        if (matchesAny(m, "포인트", "점수")) {
            return pick(
                    "포인트는 이렇게 쌓여요 — 퀴즈 정답 +5, 핵심가치 학습 완료 +10, 미션 제출 +20, 미션 승인 보너스 +10.",
                    "가장 빠르게 포인트를 쌓는 방법은 핵심가치 카드를 하나씩 완주하는 거예요. 완료 시 +10, 퀴즈까지 맞히면 +5가 추가로 붙어요."
            );
        }

        // 7) 배지
        if (matchesAny(m, "배지", "뱃지")) {
            return pick(
                    "배지는 총 15종이에요. 핵심가치 12개를 하나씩 완료할 때마다 전용 배지 1개, 7일/14일 연속 학습 시 스트릭 배지, 12개 전부 완료 시 올클리어 배지를 받을 수 있어요.",
                    "배지함은 마이페이지에서 모아볼 수 있어요. 아직 못 받은 배지가 있다면 완료하지 않은 핵심가치를 먼저 확인해보세요!"
            );
        }

        // 8) 스트릭
        if (matchesAny(m, "스트릭", "연속", "streak")) {
            return pick(
                    "스트릭은 하루에 한 번이라도 학습카드를 완료하거나 미션을 제출하면 +1일 올라가요. 하루라도 활동이 없으면 초기화되니 매일 한 번씩 들러주세요!",
                    "연속 접속일이 7일, 14일이 되면 각각 전용 스트릭 배지도 받을 수 있어요. 습관처럼 매일 카드 한 장씩만 넘겨도 충분해요."
            );
        }

        // 9) 로그인/계정
        if (matchesAny(m, "로그인", "비밀번호", "계정", "회원가입", "아이디")) {
            return pick(
                    "로그인이 안 되면 이메일/비밀번호를 다시 확인해주세요. 심사용 테스트 계정은 로그인 화면 하단에 안내되어 있어요 (신입사원: newbie@interx.io / demo1234!).",
                    "계정 관련 문제가 지속되면 화면 하단 '피드백' 메뉴로 인사팀에 바로 문의할 수 있어요."
            );
        }

        // 10) 관리자/인사팀 문의
        if (matchesAny(m, "인사팀", "관리자", "담당자", "문의")) {
            return pick(
                    "인사팀에 직접 문의하고 싶다면 피드백 채널을 이용해주세요. 신입사원 ↔ 인사팀 양방향으로 코멘트를 주고받을 수 있어요.",
                    "미션 승인/반려나 진행현황 관련 문의는 관리자 대시보드를 통해 인사팀이 확인하고 있어요. 급한 건은 피드백으로 남겨주세요!"
            );
        }

        // 11) 회사/서비스 소개
        if (matchesAny(m, "인터엑스가뭐", "무슨회사", "어떤회사", "회사소개", "interx가뭐")) {
            return "인터엑스는 AI·기술 기반으로 새로운 가치를 만드는 회사예요. 이 포털은 신입사원이 12가지 핵심가치를 카드스와이프 학습과 미션으로 자연스럽게 익히도록 돕는 온보딩 서비스입니다.";
        }

        // 12) 감사 인사
        if (matchesAny(m, "고마워", "감사", "thanks", "thank")) {
            return pick(
                    "천만에요! 또 궁금한 게 생기면 언제든 물어보세요 🙂",
                    "도움이 됐다니 다행이에요! 핵심가치 학습도 화이팅입니다."
            );
        }

        // 13) 범용 폴백 — 매번 다른 문구로 안내
        return pick(
                "음, 정확히는 못 알아들었어요. '집요한 끈기'처럼 핵심가치 이름이나, '포인트'·'배지'·'미션'·'스트릭' 같은 키워드로 물어봐 주실래요?",
                "아직 그 질문엔 자신 있게 답하긴 어렵네요. 대신 12가지 핵심가치 이름이나 이번주 미션에 대해 물어보시면 바로 알려드릴 수 있어요!",
                "제가 답할 수 있는 범위는 핵심가치·미션·포인트·배지·스트릭·계정 관련이에요. 이 중 궁금한 걸로 다시 물어봐 주세요.",
                "흠, 조금 더 구체적으로 물어봐 줄래요? 예를 들어 '초효율적 시간관리가 뭐야?' 처럼요."
        );
    }

    /** 핵심가치 이름을 통째로 포함하거나, 이름을 공백/슬래시로 쪼갠 토큰(2자 이상)을 포함하면 매칭 */
    private CoreValue findMatchingValue(List<CoreValue> values, String normalizedMessage) {
        // 1차: 전체 이름(공백 제거) 완전 포함
        for (CoreValue v : values) {
            String nameNoSpace = v.getName().replaceAll("[\\s/]", "");
            if (normalizedMessage.contains(nameNoSpace)) {
                return v;
            }
        }
        // 2차: 이름을 공백/슬래시 기준으로 쪼갠 토큰 매칭 (2자 이상만)
        for (CoreValue v : values) {
            String[] tokens = v.getName().split("[\\s/]+");
            for (String token : tokens) {
                if (token.length() >= 2 && normalizedMessage.contains(token)) {
                    return v;
                }
            }
        }
        return null;
    }

    private String valueAnswer(CoreValue v) {
        String intro = valueCardRepository.findByCoreValueIdOrderBySortOrderAsc(v.getId()).stream()
                .filter(c -> "INTRO".equals(c.getCardType().name()))
                .map(ValueCard::getContent)
                .findFirst()
                .orElse(null);

        String base = "'" + v.getName() + "' — " + v.getDescription();
        String cta = pick(
                "홈 화면에서 카드를 넘기면서 실제 상황 예시와 퀴즈도 풀어보세요!",
                "관련 카드와 미니 퀴즈를 완료하면 포인트와 전용 배지도 받을 수 있어요.",
                "핵심가치 학습 화면에서 실제 업무 상황 카드로 더 자세히 볼 수 있어요."
        );

        if (intro != null && !intro.isBlank()) {
            return base + "\n\"" + intro + "\" " + cta;
        }
        return base + " " + cta;
    }

    private boolean matchesAny(String message, String... keywords) {
        for (String k : keywords) {
            if (message.contains(k)) return true;
        }
        return false;
    }

    private String pick(String... options) {
        return options[ThreadLocalRandom.current().nextInt(options.length)];
    }

    private record ChatMessage(String role, String content) {}
    private record ChatCompletionRequest(String model, List<ChatMessage> messages, double temperature) {}
}
