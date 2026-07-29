package com.interx.onboarding.service;

import com.interx.onboarding.domain.CardType;
import com.interx.onboarding.domain.ChatbotLog;
import com.interx.onboarding.domain.CoreValue;
import com.interx.onboarding.domain.ValueCard;
import com.interx.onboarding.repository.ChatbotLogRepository;
import com.interx.onboarding.repository.CoreValueRepository;
import com.interx.onboarding.repository.ValueCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private CoreValueRepository coreValueRepository;
    @Mock
    private ValueCardRepository valueCardRepository;
    @Mock
    private ChatbotLogRepository chatbotLogRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private CoreValue persistence;

    @BeforeEach
    void setUp() {
        // mock 모드로 동작하도록 provider를 비워둠 (실제 @Value 주입 대신 리플렉션으로 세팅)
        ReflectionTestUtils.setField(chatbotService, "provider", "mock");
        ReflectionTestUtils.setField(chatbotService, "apiKey", "");

        persistence = CoreValue.builder()
                .id(3L).name("집요한 끈기").icon("repeat")
                .description("실패를 통해 빠르게 배우고 전략을 수정하며 반복적으로 실행합니다.")
                .sortOrder(3).build();

        CoreValue timeMgmt = CoreValue.builder()
                .id(2L).name("초효율적 시간관리").icon("clock")
                .description("AI 등 다양한 도구와 리소스를 적극 활용하여 업무를 자동화/효율화합니다.")
                .sortOrder(2).build();

        List<CoreValue> values = List.of(timeMgmt, persistence);
        lenient().when(coreValueRepository.findAllByOrderBySortOrderAsc()).thenReturn(values);

        ValueCard intro = ValueCard.builder()
                .id(9L).coreValueId(3L).cardType(CardType.INTRO)
                .content("실패는 데이터일 뿐, 끝은 아니다 🔥").sortOrder(1).build();
        lenient().when(valueCardRepository.findByCoreValueIdOrderBySortOrderAsc(3L)).thenReturn(List.of(intro));
        lenient().when(valueCardRepository.findByCoreValueIdOrderBySortOrderAsc(2L)).thenReturn(List.of());

        lenient().when(chatbotLogRepository.save(any(ChatbotLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void asksAboutSpecificCoreValue_returnsValueSpecificAnswer() {
        String answer = chatbotService.ask(1L, "집요한 끈기가 뭐야?");

        assertThat(answer).contains("집요한 끈기");
        assertThat(answer).contains("실패는 데이터일 뿐"); // INTRO 카드 문구가 포함되어야 함
    }

    @Test
    void asksAboutDifferentCoreValues_returnsDifferentAnswers() {
        String a = chatbotService.ask(1L, "집요한 끈기가 뭐야?");
        String b = chatbotService.ask(1L, "초효율적 시간관리가 뭐야?");

        assertThat(a).isNotEqualTo(b);
        assertThat(a).contains("집요한 끈기");
        assertThat(b).contains("초효율적 시간관리");
    }

    @Test
    void asksAboutPoints_returnsPointRelatedAnswer() {
        String answer = chatbotService.ask(1L, "포인트는 어떻게 쌓여?");

        assertThat(answer).containsAnyOf("포인트", "+5", "+10");
    }

    @Test
    void unmatchedQuestion_stillReturnsNonEmptyFallback() {
        String answer = chatbotService.ask(1L, "오늘 점심 뭐 먹지?");

        assertThat(answer).isNotBlank();
    }

    @Test
    void everyAskCall_isPersistedToChatbotLog() {
        chatbotService.ask(5L, "안녕");

        org.mockito.Mockito.verify(chatbotLogRepository).save(any(ChatbotLog.class));
    }
}
