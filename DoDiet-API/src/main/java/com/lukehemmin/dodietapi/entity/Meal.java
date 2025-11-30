package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meals", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, date")
})
@Getter @Setter
@NoArgsConstructor
public class Meal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String foodItem;
    
    @Column(nullable = false)
    private Double servingSize;
    
    @Column(nullable = false)
    private Double kcal;
    
    @Column(nullable = false)
    private Double carbs;
    
    @Column(nullable = false)
    private Double protein;
    
    @Column(nullable = false)
    private Double fat;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealTime mealTime; // BREAKFAST, LUNCH, DINNER, SNACK, LATE_NIGHT
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
