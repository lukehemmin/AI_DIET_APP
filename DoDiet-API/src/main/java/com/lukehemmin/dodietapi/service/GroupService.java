package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.GroupCreateRequest;
import com.lukehemmin.dodietapi.dto.response.GroupResponse;
import com.lukehemmin.dodietapi.entity.FeedType;
import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupFeed;
import com.lukehemmin.dodietapi.entity.GroupMember;
import com.lukehemmin.dodietapi.entity.GroupRole;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.GroupFeedRepository;
import com.lukehemmin.dodietapi.repository.GroupMemberRepository;
import com.lukehemmin.dodietapi.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupFeedRepository groupFeedRepository;

    @Transactional
    public GroupResponse createGroup(User user, GroupCreateRequest request) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setChallenge(request.getChallenge());
        group.setChallengeDays(7); // 기본 7일
        group.setChallengeStartDate(java.time.LocalDateTime.now()); // 생성 시점부터 시작
        
        Group savedGroup = groupRepository.save(group);

        // Add creator as leader
        GroupMember member = new GroupMember();
        member.setGroup(savedGroup);
        member.setUser(user);
        member.setRole(GroupRole.LEADER);
        
        groupMemberRepository.save(member);
        
        // Manually add to list for response
        savedGroup.getMembers().add(member);

        return GroupResponse.from(savedGroup);
    }

    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
    }

    public com.lukehemmin.dodietapi.dto.response.GroupDetailResponse getGroupDetail(java.util.UUID groupId, User user) {
        com.lukehemmin.dodietapi.entity.Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // 진행률 계산 (식단 기록 일수 기반)
        int progress = calculateProgress(group);
        
        // 사용자 역할 확인
        String userRole = groupMemberRepository.findByGroupAndUser(group, user)
                .map(m -> m.getRole().name())
                .orElse("NONE");
        
        return com.lukehemmin.dodietapi.dto.response.GroupDetailResponse.from(group, progress, userRole);
    }

    /**
     * 챌린지 진행률 계산
     * 식단 기록 일수 / 챌린지 기간 * 100
     */
    private int calculateProgress(Group group) {
        if (group.getChallengeStartDate() == null || group.getChallengeDays() == null) {
            return 0;
        }

        LocalDateTime startDate = group.getChallengeStartDate();
        LocalDateTime endDate = startDate.plusDays(group.getChallengeDays());
        LocalDateTime now = LocalDateTime.now();

        // 챌린지가 아직 시작하지 않았거나 종료되었으면
        if (now.isBefore(startDate)) {
            return 0;
        }
        if (now.isAfter(endDate)) {
            // 종료 후에는 최종 진행률 반환 (100% 또는 실제 달성률)
            // 여기서는 간단히 경과일 기반으로 계산
        }

        // 그룹 멤버 수
        int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
        if (memberCount == 0) {
            return 0;
        }

        // 챌린지 기간 내 식단 공유한 날의 수 계산
        // 모든 멤버가 식단을 공유한 날을 카운트
        List<GroupFeed> feeds = groupFeedRepository.findByGroupOrderByCreatedAtDesc(group);
        
        // 식단 공유 피드만 필터링하고 날짜별로 그룹핑
        long mealShareDays = feeds.stream()
                .filter(f -> f.getFeedType() == FeedType.MEAL_SHARE)
                .filter(f -> !f.getCreatedAt().isBefore(startDate) && !f.getCreatedAt().isAfter(endDate))
                .map(f -> f.getCreatedAt().toLocalDate())
                .distinct()
                .count();

        // 진행률: (식단 공유 일수 / 챌린지 기간) * 100
        int progress = (int) Math.min(100, (mealShareDays * 100) / group.getChallengeDays());
        
        return progress;
    }

    public List<GroupResponse> getMyGroups(User user) {
        List<GroupMember> memberships = groupMemberRepository.findByUser(user);
        return memberships.stream()
                .map(m -> GroupResponse.from(m.getGroup()))
                .collect(Collectors.toList());
    }

    /**
     * 그룹 정보 수정 (방장만)
     */
    @Transactional
    public GroupResponse updateGroup(java.util.UUID groupId, User user, GroupCreateRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 방장 권한 확인
        validateLeader(group, user);

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        if (request.getChallenge() != null) {
            group.setChallenge(request.getChallenge());
        }

        return GroupResponse.from(groupRepository.save(group));
    }

    /**
     * 그룹 나가기
     */
    @Transactional
    public void leaveGroup(java.util.UUID groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new RuntimeException("Not a member of this group"));

        // 방장이 나가려면 다른 멤버에게 양도하거나 그룹 삭제 필요
        if (member.getRole() == GroupRole.LEADER) {
            List<GroupMember> otherMembers = groupMemberRepository.findByGroup(group).stream()
                    .filter(m -> !m.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());

            if (!otherMembers.isEmpty()) {
                // 다른 멤버 중 첫 번째에게 방장 양도
                GroupMember newLeader = otherMembers.get(0);
                newLeader.setRole(GroupRole.LEADER);
                groupMemberRepository.save(newLeader);
            }
        }

        groupMemberRepository.delete(member);

        // 남은 멤버가 없으면 그룹 삭제
        if (groupMemberRepository.findByGroup(group).isEmpty()) {
            groupRepository.delete(group);
        }
    }

    /**
     * 그룹 삭제 (방장만)
     */
    @Transactional
    public void deleteGroup(java.util.UUID groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 방장 권한 확인
        validateLeader(group, user);

        groupRepository.delete(group);
    }

    /**
     * 멤버 초대
     */
    @Transactional
    public void inviteMember(java.util.UUID groupId, User inviter, java.util.UUID inviteeId, 
                             com.lukehemmin.dodietapi.repository.UserRepository userRepository) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 초대자가 멤버인지 확인
        if (groupMemberRepository.findByGroupAndUser(group, inviter).isEmpty()) {
            throw new RuntimeException("Not a member of this group");
        }

        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이미 멤버인지 확인
        if (groupMemberRepository.findByGroupAndUser(group, invitee).isPresent()) {
            throw new RuntimeException("User is already a member");
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(invitee);
        member.setRole(GroupRole.MEMBER);

        groupMemberRepository.save(member);
    }

    /**
     * 멤버 강퇴 (방장만)
     */
    @Transactional
    public void kickMember(java.util.UUID groupId, User leader, java.util.UUID memberId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 방장 권한 확인
        validateLeader(group, leader);

        GroupMember memberToKick = groupMemberRepository.findByGroup(group).stream()
                .filter(m -> m.getUser().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found in this group"));

        // 자기 자신은 강퇴 불가
        if (memberToKick.getUser().getId().equals(leader.getId())) {
            throw new RuntimeException("Cannot kick yourself");
        }

        groupMemberRepository.delete(memberToKick);
    }

    /**
     * 방장 권한 확인
     */
    private void validateLeader(Group group, User user) {
        GroupMember member = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new RuntimeException("Not a member of this group"));

        if (member.getRole() != GroupRole.LEADER) {
            throw new RuntimeException("Only the leader can perform this action");
        }
    }

    /**
     * 사용자의 역할 확인
     */
    public String getUserRole(java.util.UUID groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return groupMemberRepository.findByGroupAndUser(group, user)
                .map(m -> m.getRole().name())
                .orElse(null);
    }
}
