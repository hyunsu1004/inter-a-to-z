package com.interx.onboarding.controller;

import com.interx.onboarding.domain.Feedback;
import com.interx.onboarding.dto.FeedbackRequest;
import com.interx.onboarding.repository.FeedbackRepository;
import com.interx.onboarding.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    @GetMapping("/me")
    public List<Feedback> myFeedbacks(HttpServletRequest request) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(CurrentUser.id(request));
    }

    @PostMapping
    public Feedback create(@RequestBody FeedbackRequest req, HttpServletRequest request) {
        Feedback feedback = Feedback.builder()
                .userId(CurrentUser.id(request))
                .missionId(req.missionId())
                .content(req.content())
                .build();
        return feedbackRepository.save(feedback);
    }
}
