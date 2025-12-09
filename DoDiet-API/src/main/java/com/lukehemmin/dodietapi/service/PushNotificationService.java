package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupMember;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 푸시 알림 서비스
 * TODO: Firebase Admin SDK 설정 후 실제 푸시 알림 전송 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final GroupMemberRepository groupMemberRepository;

    /**
     * 그룹 멤버들에게 새 피드 알림 전송
     */
    public void notifyNewFeed(Group group, User author, String message) {
        List<User> recipients = getGroupMembersExcept(group, author);
        
        for (User recipient : recipients) {
            sendPushNotification(
                    recipient,
                    "새로운 그룹 활동",
                    author.getName() + "님이 " + group.getName() + "에 " + message
            );
        }
    }

    /**
     * 그룹 멤버들에게 새 댓글 알림 전송
     */
    public void notifyNewComment(Group group, User feedAuthor, User commenter, String preview) {
        sendPushNotification(
                feedAuthor,
                "새로운 댓글",
                commenter.getName() + "님이 댓글을 남겼습니다: " + preview
        );
    }

    /**
     * 그룹 멤버들에게 새 반응 알림 전송
     */
    public void notifyNewReaction(User feedAuthor, User reactor, String reactionEmoji) {
        sendPushNotification(
                feedAuthor,
                "새로운 반응",
                reactor.getName() + "님이 " + reactionEmoji + " 반응을 남겼습니다"
        );
    }

    /**
     * 그룹에 새 멤버 참가 알림
     */
    public void notifyMemberJoined(Group group, User newMember) {
        List<User> recipients = getGroupMembersExcept(group, newMember);
        
        for (User recipient : recipients) {
            sendPushNotification(
                    recipient,
                    group.getName(),
                    newMember.getName() + "님이 그룹에 참가했습니다!"
            );
        }
    }

    /**
     * 챌린지 달성 알림
     */
    public void notifyChallengeAchieved(Group group, User achiever, String achievement) {
        List<User> recipients = getGroupMembersExcept(group, achiever);
        
        for (User recipient : recipients) {
            sendPushNotification(
                    recipient,
                    "🎉 챌린지 달성!",
                    achiever.getName() + "님이 " + achievement
            );
        }
    }

    /**
     * 그룹 마일스톤 달성 알림
     */
    public void notifyMilestone(Group group, String milestone) {
        List<User> recipients = groupMemberRepository.findByGroup(group).stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
        
        for (User recipient : recipients) {
            sendPushNotification(
                    recipient,
                    "🏆 " + group.getName(),
                    milestone
            );
        }
    }

    /**
     * 푸시 알림 전송
     * TODO: Firebase Admin SDK로 실제 푸시 알림 전송 구현
     */
    private void sendPushNotification(User recipient, String title, String body) {
        // TODO: 사용자의 FCM 토큰으로 실제 푸시 알림 전송
        // 현재는 로그만 출력
        log.info("📱 Push notification to {}: [{}] {}", recipient.getName(), title, body);
        
        // Firebase Admin SDK 구현 예시:
        // String fcmToken = recipient.getFcmToken();
        // if (fcmToken != null && !fcmToken.isEmpty()) {
        //     Message message = Message.builder()
        //             .setNotification(Notification.builder()
        //                     .setTitle(title)
        //                     .setBody(body)
        //                     .build())
        //             .setToken(fcmToken)
        //             .build();
        //     FirebaseMessaging.getInstance().send(message);
        // }
    }

    /**
     * 특정 사용자를 제외한 그룹 멤버 목록
     */
    private List<User> getGroupMembersExcept(Group group, User excludeUser) {
        return groupMemberRepository.findByGroup(group).stream()
                .map(GroupMember::getUser)
                .filter(user -> !user.getId().equals(excludeUser.getId()))
                .collect(Collectors.toList());
    }
}
