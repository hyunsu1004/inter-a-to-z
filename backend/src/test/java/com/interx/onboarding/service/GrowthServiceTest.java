package com.interx.onboarding.service;

import com.interx.onboarding.domain.CoreValue;
import com.interx.onboarding.domain.PointsLog;
import com.interx.onboarding.domain.ProgressStatus;
import com.interx.onboarding.domain.UserValueProgress;
import com.interx.onboarding.dto.GrowthResponse;
import com.interx.onboarding.dto.RadarPointDto;
import com.interx.onboarding.repository.CoreValueRepository;
import com.interx.onboarding.repository.PointsLogRepository;
import com.interx.onboarding.repository.UserValueProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrowthServiceTest {

    @Mock
    private CoreValueRepository coreValueRepository;
    @Mock
    private UserValueProgressRepository progressRepository;
    @Mock
    private PointsLogRepository pointsLogRepository;

    @InjectMocks
    private GrowthService growthService;

    private CoreValue timeMgmt;
    private CoreValue persistence;
    private CoreValue leadership;

    @BeforeEach
    void setUp() {
        timeMgmt = CoreValue.builder().id(2L).name("초효율적 시간관리").icon("clock").sortOrder(2).build();
        persistence = CoreValue.builder().id(3L).name("집요한 끈기").icon("repeat").sortOrder(3).build();
        leadership = CoreValue.builder().id(4L).name("가치중심적 문제해결").icon("bulb").sortOrder(4).build();

        lenient().when(coreValueRepository.findAllByOrderBySortOrderAsc())
                .thenReturn(List.of(timeMgmt, persistence, leadership));
        lenient().when(progressRepository.findByUserId(1L)).thenReturn(List.of());
        lenient().when(pointsLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
    }

    @Test
    void radar_scoresEachCoreValueByProgressStatus() {
        UserValueProgress completed = UserValueProgress.builder()
                .id(1L).userId(1L).coreValueId(2L).status(ProgressStatus.COMPLETED).build();
        UserValueProgress inProgress = UserValueProgress.builder()
                .id(2L).userId(1L).coreValueId(3L).status(ProgressStatus.IN_PROGRESS).build();
        when(progressRepository.findByUserId(1L)).thenReturn(List.of(completed, inProgress));

        GrowthResponse response = growthService.getGrowth(1L);

        List<RadarPointDto> radar = response.radar();
        assertThat(radar).hasSize(3);
        assertThat(scoreOf(radar, 2L)).isEqualTo(100); // COMPLETED
        assertThat(scoreOf(radar, 3L)).isEqualTo(50);  // IN_PROGRESS
        assertThat(scoreOf(radar, 4L)).isEqualTo(0);   // 진행 이력 없음 -> NOT_STARTED
    }

    @Test
    void timeline_mapsSourceTypeToKoreanLabel_andPreservesOrder() {
        LocalDateTime now = LocalDateTime.now();
        PointsLog quiz = PointsLog.builder()
                .id(1L).userId(1L).sourceType("QUIZ_CORRECT").sourceId(10L).points(5).createdAt(now).build();
        PointsLog valueComplete = PointsLog.builder()
                .id(2L).userId(1L).sourceType("VALUE_COMPLETE").sourceId(2L).points(10).createdAt(now.minusMinutes(5)).build();
        when(pointsLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(quiz, valueComplete));

        GrowthResponse response = growthService.getGrowth(1L);

        assertThat(response.timeline()).hasSize(2);
        assertThat(response.timeline().get(0).label()).isEqualTo("퀴즈 정답");
        assertThat(response.timeline().get(0).points()).isEqualTo(5);
        assertThat(response.timeline().get(1).label()).isEqualTo("핵심가치 완료");
    }

    @Test
    void timeline_unknownSourceType_fallsBackToRawValue() {
        PointsLog unknown = PointsLog.builder()
                .id(1L).userId(1L).sourceType("SOMETHING_NEW").sourceId(1L).points(1)
                .createdAt(LocalDateTime.now()).build();
        when(pointsLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(unknown));

        GrowthResponse response = growthService.getGrowth(1L);

        assertThat(response.timeline().get(0).label()).isEqualTo("SOMETHING_NEW");
    }

    private int scoreOf(List<RadarPointDto> radar, Long coreValueId) {
        return radar.stream()
                .filter(r -> r.coreValueId().equals(coreValueId))
                .findFirst()
                .orElseThrow()
                .score();
    }
}
