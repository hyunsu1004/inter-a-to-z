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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionCommentServiceTest {

    @Mock
    private MissionCommentRepository missionCommentRepository;
    @Mock
    private UserMissionRepository userMissionRepository;
    @Mock
    private MissionRepository missionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    private MissionCommentService service;

    private final UserMission userMission = UserMission.builder()
            .id(100L).userId(1L).missionId(10L).build();

    private final User employee = User.builder().id(1L).name("최현수").role(Role.EMPLOYEE).build();
    private final User admin = User.builder().id(99L).name("인사팀 관리자").role(Role.ADMIN).build();

    @BeforeEach
    void setUp() {
        service = new MissionCommentService(
                missionCommentRepository, userMissionRepository, missionRepository, userRepository, notificationService
        );
        lenient().when(userMissionRepository.findById(100L)).thenReturn(Optional.of(userMission));
        lenient().when(missionRepository.findById(10L))
                .thenReturn(Optional.of(Mission.builder().id(10L).title("사수에게 자기소개 메시지 보내기").build()));
        lenient().when(missionCommentRepository.save(any(MissionComment.class)))
                .thenAnswer(inv -> {
                    MissionComment c = inv.getArgument(0);
                    c.setId(1L);
                    return c;
                });
    }

    @Test
    void addAsEmployee_savesComment_andNotifiesAllAdmins() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findAll()).thenReturn(List.of(employee, admin));

        MissionCommentDto dto = service.addAsEmployee(1L, 100L, "확인 부탁드립니다!");

        assertThat(dto.authorName()).isEqualTo("최현수");
        assertThat(dto.authorRole()).isEqualTo("EMPLOYEE");
        assertThat(dto.content()).isEqualTo("확인 부탁드립니다!");

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService).notifyUsers(eq(List.of(99L)), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("NEW_COMMENT");
    }

    @Test
    void addAsEmployee_whenNotOwner_throwsAccessDenied_andDoesNotSaveOrNotify() {
        assertThatThrownBy(() -> service.addAsEmployee(2L, 100L, "몰래 댓글"))
                .isInstanceOf(AccessDeniedException.class);

        verify(missionCommentRepository, never()).save(any());
        verify(notificationService, never()).notifyUser(any(), any());
        verify(notificationService, never()).notifyUsers(anyList(), any());
    }

    @Test
    void addAsAdmin_savesComment_andNotifiesTheEmployeeWhoOwnsTheSubmission() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));

        MissionCommentDto dto = service.addAsAdmin(99L, 100L, "좋은 시도예요, 다만 조금 더 구체적으로 적어볼까요?");

        assertThat(dto.authorRole()).isEqualTo("ADMIN");

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService).notifyUser(eq(1L), eventCaptor.capture());
        assertThat(eventCaptor.getValue().type()).isEqualTo("NEW_COMMENT");
        assertThat(eventCaptor.getValue().userMissionId()).isEqualTo(100L);
    }

    @Test
    void listForEmployee_whenNotOwner_throwsAccessDenied() {
        assertThatThrownBy(() -> service.listForEmployee(2L, 100L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listForEmployee_whenOwner_returnsCommentsInOrder() {
        MissionComment c1 = MissionComment.builder()
                .id(1L).userMissionId(100L).authorId(1L).authorName("최현수").authorRole(Role.EMPLOYEE)
                .content("첫 댓글").build();
        when(missionCommentRepository.findByUserMissionIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(c1));

        List<MissionCommentDto> result = service.listForEmployee(1L, 100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("첫 댓글");
    }
}
