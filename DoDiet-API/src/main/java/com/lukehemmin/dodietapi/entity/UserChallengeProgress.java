package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_challenge_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"}))
@Getter @Setter
@NoArgsConstructor
public class UserChallengeProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;
    
    @Column(nullable = false)
    private Integer current = 0; // Current progress count
    
    @Column(nullable = false)
    private Double progress = 0.0; // 0.0 ~ 1.0
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status = ChallengeStatus.IN_PROGRESS;
    
    @Column
    private LocalDateTime completedAt; // Completion time
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Update progress calculation
    public void updateProgress() {
        if (challenge != null && challenge.getGoal() > 0) {
            this.progress = Math.min(1.0, (double) current / challenge.getGoal());
            if (this.progress >= 1.0 && this.status != ChallengeStatus.COMPLETED) {
                this.status = ChallengeStatus.COMPLETED;
                this.completedAt = LocalDateTime.now();
            }
        }
    }
}
