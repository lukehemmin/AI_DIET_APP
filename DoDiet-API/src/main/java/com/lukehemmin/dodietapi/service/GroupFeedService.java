package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.AddCommentRequest;
import com.lukehemmin.dodietapi.dto.request.ShareMealRequest;
import com.lukehemmin.dodietapi.dto.response.FeedCommentResponse;
import com.lukehemmin.dodietapi.dto.response.GroupFeedResponse;
import com.lukehemmin.dodietapi.entity.*;
import com.lukehemmin.dodietapi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupFeedService {

    private final GroupFeedRepository feedRepository;
    private final FeedCommentRepository commentRepository;
    private final FeedReactionRepository reactionRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final MealRepository mealRepository;

    /**
     * 그룹 피드 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GroupFeedResponse> getGroupFeeds(UUID groupId, User user, int page, int size) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 그룹 멤버인지 확인
        validateMembership(group, user);

        Pageable pageable = PageRequest.of(page, size);
        Page<GroupFeed> feeds = feedRepository.findByGroupOrderByCreatedAtDesc(group, pageable);

        return feeds.stream()
                .map(feed -> {
                    List<FeedReaction> allReactions = reactionRepository.findByFeed(feed);
                    List<FeedReaction> userReactions = reactionRepository.findByFeedAndUser(feed, user);
                    int commentCount = (int) commentRepository.countByFeed(feed);
                    return GroupFeedResponse.from(feed, allReactions, userReactions, commentCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 식단 그룹에 공유
     */
    @Transactional
    public List<GroupFeedResponse> shareMealToGroups(User user, ShareMealRequest request) {
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new RuntimeException("Meal not found"));

        // 본인 식단인지 확인
        if (!meal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cannot share other user's meal");
        }

        List<GroupFeedResponse> createdFeeds = new ArrayList<>();

        for (UUID groupId : request.getGroupIds()) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

            // 그룹 멤버인지 확인
            validateMembership(group, user);

            // 이미 공유되었는지 확인
            if (feedRepository.findByGroupAndMeal(group, meal).isPresent()) {
                log.info("Meal {} already shared to group {}", meal.getId(), groupId);
                continue;
            }

            // 피드 생성
            GroupFeed feed = new GroupFeed();
            feed.setGroup(group);
            feed.setUser(user);
            feed.setFeedType(FeedType.MEAL_SHARE);
            feed.setMeal(meal);
            feed.setContent(request.getMessage());
            feed.setImageUrl(meal.getImageUrl());

            feedRepository.save(feed);

            createdFeeds.add(GroupFeedResponse.from(feed, List.of(), List.of(), 0));

            log.info("Meal {} shared to group {} by user {}", meal.getId(), groupId, user.getId());
        }

        return createdFeeds;
    }

    /**
     * 챌린지 달성 피드 생성 (시스템 자동 생성)
     */
    @Transactional
    public GroupFeed createChallengeAchievedFeed(Group group, User user, String message) {
        GroupFeed feed = new GroupFeed();
        feed.setGroup(group);
        feed.setUser(user);
        feed.setFeedType(FeedType.CHALLENGE_ACHIEVED);
        feed.setContent(message);

        return feedRepository.save(feed);
    }

    /**
     * 마일스톤 피드 생성 (시스템 자동 생성)
     */
    @Transactional
    public GroupFeed createMilestoneFeed(Group group, User systemUser, String message) {
        GroupFeed feed = new GroupFeed();
        feed.setGroup(group);
        feed.setUser(systemUser);
        feed.setFeedType(FeedType.MILESTONE);
        feed.setContent(message);

        return feedRepository.save(feed);
    }

    /**
     * 새 멤버 참가 피드 생성
     */
    @Transactional
    public GroupFeed createMemberJoinedFeed(Group group, User newMember) {
        GroupFeed feed = new GroupFeed();
        feed.setGroup(group);
        feed.setUser(newMember);
        feed.setFeedType(FeedType.MEMBER_JOINED);
        feed.setContent(newMember.getName() + "님이 그룹에 참가했습니다!");

        return feedRepository.save(feed);
    }

    /**
     * 댓글 추가
     */
    @Transactional
    public FeedCommentResponse addComment(UUID feedId, User user, AddCommentRequest request) {
        GroupFeed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // 그룹 멤버인지 확인
        validateMembership(feed.getGroup(), user);

        FeedComment comment = new FeedComment();
        comment.setFeed(feed);
        comment.setUser(user);
        comment.setContent(request.getContent());

        commentRepository.save(comment);

        return FeedCommentResponse.from(comment);
    }

    /**
     * 피드 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FeedCommentResponse> getComments(UUID feedId, User user) {
        GroupFeed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // 그룹 멤버인지 확인
        validateMembership(feed.getGroup(), user);

        return commentRepository.findByFeedOrderByCreatedAtAsc(feed).stream()
                .map(FeedCommentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(UUID commentId, User user) {
        FeedComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 본인 댓글인지 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cannot delete other user's comment");
        }

        commentRepository.delete(comment);
    }

    /**
     * 반응 토글 (추가/제거)
     */
    @Transactional
    public boolean toggleReaction(UUID feedId, User user, ReactionType reactionType) {
        GroupFeed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // 그룹 멤버인지 확인
        validateMembership(feed.getGroup(), user);

        // 기존 반응 확인
        var existingReaction = reactionRepository.findByFeedAndUserAndReactionType(feed, user, reactionType);

        if (existingReaction.isPresent()) {
            // 이미 있으면 제거
            reactionRepository.delete(existingReaction.get());
            return false;
        } else {
            // 없으면 추가
            FeedReaction reaction = new FeedReaction();
            reaction.setFeed(feed);
            reaction.setUser(user);
            reaction.setReactionType(reactionType);
            reactionRepository.save(reaction);
            return true;
        }
    }

    /**
     * 피드 삭제 (작성자만)
     */
    @Transactional
    public void deleteFeed(UUID feedId, User user) {
        GroupFeed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new RuntimeException("Feed not found"));

        // 본인 피드인지 확인
        if (!feed.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Cannot delete other user's feed");
        }

        feedRepository.delete(feed);
    }

    /**
     * 그룹 멤버십 검증
     */
    private void validateMembership(Group group, User user) {
        boolean isMember = memberRepository.findByGroupAndUser(group, user).isPresent();
        if (!isMember) {
            throw new RuntimeException("User is not a member of this group");
        }
    }
}
