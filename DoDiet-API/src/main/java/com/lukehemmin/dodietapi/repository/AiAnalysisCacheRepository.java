package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.AiAnalysisCache;
import com.lukehemmin.dodietapi.entity.AiAnalysisType;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisCacheRepository extends JpaRepository<AiAnalysisCache, UUID> {
    
    Optional<AiAnalysisCache> findByUserAndAnalysisType(User user, AiAnalysisType analysisType);
    
    void deleteByUserAndAnalysisType(User user, AiAnalysisType analysisType);
}
