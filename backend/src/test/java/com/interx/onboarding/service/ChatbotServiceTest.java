package com.interx.onboarding.service;

import com.interx.onboarding.domain.CardType;
import com.interx.onboarding.domain.ChatbotLog;
import com.interx.onboarding.domain.CoreValue;
import com.interx.onboarding.domain.ProgressStatus;
import com.interx.onboarding.domain.UserStat;
import com.interx.onboarding.domain.UserValueProgress;
import com.interx.onboarding.domain.ValueCard;
import com.interx.onboarding.repository.ChatbotLogRepository;
import com.interx.onboarding.repository.CoreValueRepository;
import com.interx.onboarding.repository.UserStatRepository;
import com.interx.onboarding.repository.UserValueProgressRepository;
import com.interx.onboarding.repository.ValueCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

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
    @Mock
    private UserValueProgressRepository userValueProgressRepository;
    @Mock
    private UserStatRepository userStatRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private CoreValue persistence;
    private CoreValue timeMgmt;

    @BeforeEach
    void setUp() {
        // mock 모드로 동작하도록 provider를 비워둠 (실제 @Value 주입 대신 리플렉션으로 세팅)
        ReflectionTestUtils.setField(chatbotService, "provider", "mock");
        ReflectionTestUtils.setField(chatbotService, "apiKey", "");

        persistence = CoreValue.builder()
                .id(3L).name("집요한 끈기").icon("repeat")
                .description("실패를 통해 빠르게 배우고 전략을 수정하며 반복적으로 실행합니다.")
                .sortOrder(3).build();

        timeMgmt = CoreValue.builder()
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

        // 코칭 관련 기본값: 진행 이력 없음, UserStat 없음 (개별 테스트에서 필요 시 오버라이드)
        lenient().when(userValueProgressRepository.findByUserId(any())).thenReturn(List.of());
        lenient().when(userStatRepository.findById(any())).thenReturn(Optional.empty());
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

    @Test
    void coachingMessage_forUserWithNoProgress_suggestsStartingFirstValue() {
        // findAllByOrderBySortOrderAsc()는 [timeMgmt(id=2), persistence(id=3)] 순서이므로
        // 진행 이력이 전혀 없으면 첫 번째인 timeMgmt("초효율적 시간관리")를 추천해야 한다.
        String message = chatbotService.coachingMessage(1L);

        assertThat(message).isNotBlank();
        assertThat(message).contains("초효율적 시간관리");
    }

    @Test
    void coachingMessage_forUserWithHighStreak_mentionsStreak() {
        UserValueProgress inProgress = UserValueProgress.builder()
                .id(1L).userId(1L).coreValueId(2L).status(ProgressStatus.IN_PROGRESS).build();
        when(userValueProgressRepository.findByUserId(1L)).thenReturn(List.of(inProgress));
        UserStat stat = UserStat.builder()
                .userId(1L).currentStreak(5).longestStreak(5).totalPoints(30).build();
        when(userStatRepository.findById(1L)).thenReturn(Optional.of(stat));

        String message = chatbotService.coachingMessage(1L);

        assertThat(message).contains("5일");
    }

    @Test
    void coachingMessage_whenNothingLeftToStart_doesNotRecommendSpecificValue() {
        UserValueProgress completedTime = UserValueProgress.builder()
                .id(1L).userId(1L).coreValueId(2L).status(ProgressStatus.COMPLETED).build();
        UserValueProgress completedPersistence = UserValueProgress.builder()
                .id(2L).userId(1L).coreValueId(3L).status(ProgressStatus.COMPLETED).build();
        when(userValueProgressRepository.findByUserId(1L))
                .thenReturn(List.of(completedTime, completedPersistence));

        String message = chatbotService.coachingMessage(1L);

        assertThat(message).isNotBlank();
        // 픽스처의 핵심가치 2개를 모두 완료했으므로 미시작 가치가 없다 — 특정 가치 이름을 추천하는 문구는 없어야 한다.
        assertThat(message).doesNotContain("아직 시작 안 한");
    }

    @Test
    void everyCoachingCall_doesNotPersistToChatbotLog() {
        // 코칭 메시지는 사용자의 질문에 대한 답이 아니라 선제적 제안이므로 채팅 로그에는 남기지 않는다.
        chatbotService.coachingMessage(1L);

        org.mockito.Mockito.verify(chatbotLogRepository, org.mockito.Mockito.never()).save(any(ChatbotLog.class));
    }
}
