package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupMember;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByUser(User user);
    boolean existsByGroup_IdAndUser_Id(UUID groupId, UUID userId);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    List<GroupMember> findByGroup(Group group);
}
