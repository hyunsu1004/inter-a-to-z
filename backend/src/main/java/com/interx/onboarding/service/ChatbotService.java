package com.interx.onboarding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interx.onboarding.domain.ChatbotLog;
import com.interx.onboarding.domain.CoreValue;
import com.interx.onboarding.repository.ChatbotLogRepository;
import com.interx.onboarding.repository.CoreValueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service
public class ChatbotService {

    private final CoreValueRepository coreValueRepository;
    private final ChatbotLogRepository chatbotLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${app.chatbot.provider}")
    private String provider;

    @Value("${app.chatbot.api-key}")
    private String apiKey;

    public ChatbotService(CoreValueRepository coreValueRepository, ChatbotLogRepository chatbotLogRepository) {
        this.coreValueRepository = coreValueRepository;
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

    private String fallbackAnswer(String message) {
        String m = message == null ? "" : message.toLowerCase();
        List<CoreValue> values = coreValueRepository.findAllByOrderBySortOrderAsc();

        for (CoreValue v : values) {
            if (m.contains(v.getName().replace(" ", ""))) {
                return "'" + v.getName() + "'는 " + v.getDescription() + " 관련 미션이나 카드도 홈 화면에서 확인해보세요!";
            }
        }
        if (m.contains("미션") || m.contains("과제")) {
            return "이번주 미션은 홈 대시보드의 '이번주 미션' 카드에서 확인할 수 있어요. 완료 후 제출하면 포인트와 스트릭이 올라가요.";
        }
        if (m.contains("포인트") || m.contains("점수")) {
            return "가치 학습 완료 시 +10, 퀴즈 정답 시 +5, 미션 제출 시 +20, 인사팀 승인 시 +10 포인트가 추가돼요.";
        }
        if (m.contains("배지")) {
            return "핵심가치를 하나씩 완료할 때마다 전용 배지를 얻을 수 있고, 12개를 모두 완료하면 올클리어 배지도 얻을 수 있어요!";
        }
        if (m.contains("스트릭") || m.contains("연속")) {
            return "하루에 한 번이라도 학습카드를 완료하거나 미션을 제출하면 스트릭이 올라가요. 하루라도 쉬면 초기화되니 매일 들러주세요!";
        }
        return "좋은 질문이에요! 인터엑스의 12가지 핵심가치, 이번주 미션, 포인트·배지 시스템에 대해 물어보시면 더 자세히 답해드릴 수 있어요.";
    }

    private record ChatMessage(String role, String content) {}
    private record ChatCompletionRequest(String model, List<ChatMessage> messages, double temperature) {}
}
