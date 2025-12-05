package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt encrypted
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // MALE, FEMALE
    
    @Column(nullable = true)
    private LocalDate birthDate; // Changed from Age to BirthDate for more precise identification

    @Column(nullable = false)
    private Integer age; // Can be derived or kept for simplicity
    
    @Column(nullable = false)
    private Double height;
    
    @Column(nullable = false)
    private Double weight;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel activityLevel; // SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
    
    @ElementCollection
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "badge_id")
    private Set<String> unlockedBadges = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterIntake> waterIntakes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMemberships = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
