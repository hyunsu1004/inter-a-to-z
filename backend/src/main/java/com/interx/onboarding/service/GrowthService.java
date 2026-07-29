package com.interx.onboarding.service;

import com.interx.onboarding.domain.ProgressStatus;
import com.interx.onboarding.domain.UserValueProgress;
import com.interx.onboarding.dto.GrowthResponse;
import com.interx.onboarding.dto.RadarPointDto;
import com.interx.onboarding.dto.TimelineEntryDto;
import com.interx.onboarding.repository.CoreValueRepository;
import com.interx.onboarding.repository.PointsLogRepository;
import com.interx.onboarding.repository.UserValueProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 배지 목록처럼 정적으로 나열하는 대신, 신입사원이 12가지 핵심가치를 얼마나 성장시켰는지를
 * 레이더 차트 데이터로, 활동 이력을 시간 흐름에 따른 타임라인으로 변환해 "성장 서사"를 보여준다.
 * 두 데이터 모두 기존에 이미 쌓이고 있던 UserValueProgress / PointsLog를 재구성한 것으로,
 * 별도의 새 테이블이나 배치 작업 없이 조회 시점에 계산한다.
 */
@Service
@RequiredArgsConstructor
public class GrowthService {

    private static final Map<String, String> SOURCE_TYPE_LABELS = Map.of(
            "QUIZ_CORRECT", "퀴즈 정답",
            "VALUE_COMPLETE", "핵심가치 완료",
            "MISSION_SUBMIT", "미션 제출",
            "MISSION_APPROVED", "미션 승인"
    );

    private final CoreValueRepository coreValueRepository;
    private final UserValueProgressRepository progressRepository;
    private final PointsLogRepository pointsLogRepository;

    public GrowthResponse getGrowth(Long userId) {
        Map<Long, UserValueProgress> progressByValue = progressRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserValueProgress::getCoreValueId, p -> p));

        List<RadarPointDto> radar = coreValueRepository.findAllByOrderBySortOrderAsc().stream()
                .map(cv -> new RadarPointDto(
                        cv.getId(),
                        cv.getName(),
                        cv.getIcon(),
                        scoreFor(progressByValue.get(cv.getId()))))
                .collect(Collectors.toList());

        List<TimelineEntryDto> timeline = pointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(log -> new TimelineEntryDto(
                        log.getCreatedAt(),
                        log.getSourceType(),
                        SOURCE_TYPE_LABELS.getOrDefault(log.getSourceType(), log.getSourceType()),
                        log.getPoints()))
                .collect(Collectors.toList());

        return new GrowthResponse(radar, timeline);
    }

    /** NOT_STARTED=0, IN_PROGRESS=50, COMPLETED=100 으로 레이더 차트 축 점수를 산정한다. */
    private int scoreFor(UserValueProgress progress) {
        if (progress == null || progress.getStatus() == ProgressStatus.NOT_STARTED) return 0;
        if (progress.getStatus() == ProgressStatus.IN_PROGRESS) return 50;
        return 100;
    }
}
