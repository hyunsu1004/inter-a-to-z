package com.interx.onboarding.service;

import com.interx.onboarding.domain.Mission;
import com.interx.onboarding.domain.MissionComment;
import com.interx.onboarding.domain.Role;
import com.interx.onboarding.domain.User;
import com.interx.onboarding.domain.UserMission;
import com.interx.onboarding.dto.MissionCommentDto;
import com.interx.onboarding.dto.NotificationEvent;
import com.interx.onboarding.repository.MissionCommentRepository;
import com.interx.onboarding.repository.MissionRepository;
import com.interx.onboarding.repository.UserMissionRepository;
import com.interx.onboarding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionCommentService {

    private final MissionCommentRepository missionCommentRepository;
    private final UserMissionRepository userMissionRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<MissionCommentDto> listForEmployee(Long userId, Long userMissionId) {
        UserMission um = requireOwned(userId, userMissionId);
        return toDtos(missionCommentRepository.findByUserMissionIdOrderByCreatedAtAsc(um.getId()));
    }

    public List<MissionCommentDto> listForAdmin(Long userMissionId) {
        UserMission um = requireExists(userMissionId);
        return toDtos(missionCommentRepository.findByUserMissionIdOrderByCreatedAtAsc(um.getId()));
    }

    @Transactional
    public MissionCommentDto addAsEmployee(Long userId, Long userMissionId, String content) {
        UserMission um = requireOwned(userId, userMissionId);
        User author = requireUser(userId);
        MissionComment saved = save(um, author, content);
        notifyAdmins(um, author);
        return toDto(saved);
    }

    @Transactional
    public MissionCommentDto addAsAdmin(Long adminId, Long userMissionId, String content) {
        UserMission um = requireExists(userMissionId);
        User author = requireUser(adminId);
        MissionComment saved = save(um, author, content);
        notifyEmployee(um);
        return toDto(saved);
    }

    private UserMission requireOwned(Long userId, Long userMissionId) {
        UserMission um = requireExists(userMissionId);
        if (!um.getUserId().equals(userId)) {
            // 본인 제출건이 아닌 코멘트 스레드에 접근하려는 시도 - 인증은 됐지만 권한이 없는 케이스라
            // AccessDeniedException(403)이 맞다 (401은 아예 인증이 안 된 경우를 위한 것).
            throw new AccessDeniedException("본인의 제출건이 아닙니다.");
        }
        return um;
    }

    private UserMission requireExists(Long userMissionId) {
        return userMissionRepository.findById(userMissionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제출건입니다."));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private MissionComment save(UserMission um, User author, String content) {
        MissionComment comment = MissionComment.builder()
                .userMissionId(um.getId())
                .authorId(author.getId())
                .authorName(author.getName())
                .authorRole(author.getRole())
                .content(content)
                .build();
        return missionCommentRepository.save(comment);
    }

    private void notifyAdmins(UserMission um, User author) {
        String title = missionTitle(um.getMissionId());
        List<Long> adminIds = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .map(User::getId)
                .toList();
        notificationService.notifyUsers(adminIds, new NotificationEvent(
                "NEW_COMMENT",
                author.getName() + "님의 새 코멘트",
                "'" + title + "' 제출건에 새 댓글이 달렸습니다.",
                um.getMissionId(), um.getId()
        ));
    }

    private void notifyEmployee(UserMission um) {
        String title = missionTitle(um.getMissionId());
        notificationService.notifyUser(um.getUserId(), new NotificationEvent(
                "NEW_COMMENT",
                "사수님의 새 코멘트",
                "'" + title + "'에 새 댓글이 달렸습니다.",
                um.getMissionId(), um.getId()
        ));
    }

    private String missionTitle(Long missionId) {
        return missionRepository.findById(missionId).map(Mission::getTitle).orElse("미션");
    }

    private List<MissionCommentDto> toDtos(List<MissionComment> comments) {
        return comments.stream().map(this::toDto).toList();
    }

    private MissionCommentDto toDto(MissionComment c) {
        return new MissionCommentDto(
                c.getId(), c.getAuthorId(), c.getAuthorName(), c.getAuthorRole().name(), c.getContent(), c.getCreatedAt()
        );
    }
}
