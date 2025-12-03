package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.GroupCreateRequest;
import com.lukehemmin.dodietapi.dto.response.GroupResponse;
import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupMember;
import com.lukehemmin.dodietapi.entity.GroupRole;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.GroupMemberRepository;
import com.lukehemmin.dodietapi.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(User user, GroupCreateRequest request) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setChallenge(request.getChallenge());
        
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
}
