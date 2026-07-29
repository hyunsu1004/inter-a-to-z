package com.interx.onboarding.service;

import com.interx.onboarding.domain.*;
import com.interx.onboarding.dto.*;
import com.interx.onboarding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreValueService {

    private final CoreValueRepository coreValueRepository;
    private final ValueCardRepository valueCardRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final UserValueProgressRepository progressRepository;
    private final GamificationService gamificationService;

    public List<CoreValueDto> listWithProgress(Long userId) {
        Map<Long, ProgressStatus> progressMap = progressRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserValueProgress::getCoreValueId, UserValueProgress::getStatus));

        return coreValueRepository.findAllByOrderBySortOrderAsc().stream()
                .map(cv -> new CoreValueDto(
                        cv.getId(),
                        cv.getName(),
                        cv.getIcon(),
                        cv.getDescription(),
                        cv.getSortOrder(),
                        progressMap.getOrDefault(cv.getId(), ProgressStatus.NOT_STARTED).name()
                ))
                .collect(Collectors.toList());
    }

    public List<ValueCardDto> getCards(Long coreValueId, Long userId) {
        markInProgress(userId, coreValueId);
        List<ValueCard> cards = valueCardRepository.findByCoreValueIdOrderBySortOrderAsc(coreValueId);
        List<ValueCardDto> result = new ArrayList<>();
        for (ValueCard card : cards) {
            List<QuizOptionDto> options = new ArrayList<>();
            if (card.getCardType() == CardType.QUIZ) {
                options = quizOptionRepository.findByCardId(card.getId()).stream()
                        .map(o -> new QuizOptionDto(o.getId(), o.getText()))
                        .collect(Collectors.toList());
            }
            result.add(new ValueCardDto(card.getId(), card.getCardType().name(), card.getContent(),
                    card.getSortOrder(), options));
        }
        return result;
    }

    @Transactional
    public void markInProgress(Long userId, Long coreValueId) {
        UserValueProgress progress = progressRepository.findByUserIdAndCoreValueId(userId, coreValueId)
                .orElseGet(() -> UserValueProgress.builder()
                        .userId(userId).coreValueId(coreValueId).status(ProgressStatus.NOT_STARTED).build());
        if (progress.getStatus() == ProgressStatus.NOT_STARTED) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
            progressRepository.save(progress);
        }
    }

    @Transactional
    public QuizSubmitResponse submitQuiz(Long userId, QuizSubmitRequest req) {
        ValueCard card = valueCardRepository.findById(req.cardId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다."));
        List<QuizOption> options = quizOptionRepository.findByCardId(card.getId());
        boolean correct = options.stream()
                .anyMatch(o -> o.getId().equals(req.selectedOptionId()) && o.isCorrect());

        int pointsEarned = 0;
        boolean valueCompleted = false;
        List<String> newBadges = new ArrayList<>();

        UserStat stat = gamificationService.recordActivity(userId);

        if (correct) {
            pointsEarned += 5;
            gamificationService.awardPoints(userId, "QUIZ_CORRECT", card.getId(), 5);

            UserValueProgress progress = progressRepository
                    .findByUserIdAndCoreValueId(userId, card.getCoreValueId())
                    .orElseGet(() -> UserValueProgress.builder()
                            .userId(userId).coreValueId(card.getCoreValueId())
                            .status(ProgressStatus.IN_PROGRESS).build());

            if (progress.getStatus() != ProgressStatus.COMPLETED) {
                progress.setStatus(ProgressStatus.COMPLETED);
                progress.setCompletedAt(LocalDateTime.now());
                progress.setQuizScore(100);
                progressRepository.save(progress);
                valueCompleted = true;

                pointsEarned += 10;
                stat = gamificationService.awardPoints(userId, "VALUE_COMPLETE", card.getCoreValueId(), 10);
            }
            newBadges = gamificationService.checkAndAwardBadges(userId);
        }

        return new QuizSubmitResponse(correct, pointsEarned, valueCompleted, newBadges,
                stat.getCurrentStreak(), stat.getTotalPoints());
    }
}
