package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Friendship;
import com.lukehemmin.dodietapi.entity.FriendshipStatus;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    
    boolean existsByUserAndFriend(User user, User friend);
    
    Optional<Friendship> findByUserAndFriend(User user, User friend);

    @Query("SELECT f FROM Friendship f WHERE (f.user = :user OR f.friend = :user) AND f.status = :status")
    List<Friendship> findAllFriends(@Param("user") User user, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE f.friend = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingRequests(@Param("user") User user);
}
