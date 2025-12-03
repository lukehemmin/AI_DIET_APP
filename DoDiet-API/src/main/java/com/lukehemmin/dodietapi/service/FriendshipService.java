package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.FriendResponse;
import com.lukehemmin.dodietapi.entity.Friendship;
import com.lukehemmin.dodietapi.entity.FriendshipStatus;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.FriendshipRepository;
import com.lukehemmin.dodietapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendFriendRequest(User fromUser, String toEmail) {
        User toUser = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + toEmail));

        if (fromUser.getId().equals(toUser.getId())) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        // Check if friendship already exists (in either direction)
        if (friendshipRepository.existsByUserAndFriend(fromUser, toUser) || 
            friendshipRepository.existsByUserAndFriend(toUser, fromUser)) {
            throw new IllegalArgumentException("Friendship or request already exists");
        }

        Friendship friendship = new Friendship();
        friendship.setUser(fromUser);
        friendship.setFriend(toUser);
        friendship.setStatus(FriendshipStatus.PENDING);

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptFriendRequest(User user, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (!friendship.getFriend().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to accept this request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Friendship is not in pending state");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public List<FriendResponse> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findAllFriends(user, FriendshipStatus.ACCEPTED);
        
        return friendships.stream()
                .map(f -> {
                    User friend = f.getUser().getId().equals(user.getId()) ? f.getFriend() : f.getUser();
                    return FriendResponse.builder()
                            .id(f.getId())
                            .userId(friend.getId())
                            .email(friend.getEmail())
                            .name(friend.getName())
                            .status(f.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<FriendResponse> getPendingRequests(User user) {
        List<Friendship> requests = friendshipRepository.findPendingRequests(user);
        
        return requests.stream()
                .map(f -> FriendResponse.builder()
                        .id(f.getId())
                        .userId(f.getUser().getId())
                        .email(f.getUser().getEmail())
                        .name(f.getUser().getName())
                        .status(f.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
