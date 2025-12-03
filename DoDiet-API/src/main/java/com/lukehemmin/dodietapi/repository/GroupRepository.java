package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
}
