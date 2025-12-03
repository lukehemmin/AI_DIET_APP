package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenges")
@Getter @Setter
@NoArgsConstructor
public class Challenge {
    
    @Id
    private String id; // e.g., "morning_meal", "protein_goal"
    
    @Column(nullable = false)
    private String title; // e.g., "아침 식사 챙기기"
    
    @Column(nullable = false, length = 500)
    private String description; // e.g., "3일 연속 아침 식사를 기록하세요"
    
    @Column(nullable = false)
    private String icon; // Lucide Icon Name: "Sunrise", "Target" etc.
    
    @Column(nullable = false)
    private Integer goal; // Goal count (e.g., 3, 7)
    
    @Column(nullable = false)
    private String goalUnit; // "days", "times", "kcal"
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
