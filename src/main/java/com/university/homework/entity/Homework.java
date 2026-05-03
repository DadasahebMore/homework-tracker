package com.university.homework.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Homework entity for database persistence */
@Entity
@Table(
    name = "homework",
    indexes = {
      @Index(name = "idx_user_id", columnList = "user_id"),
      @Index(name = "idx_created_at", columnList = "created_at"),
      @Index(name = "idx_status", columnList = "status"),
      @Index(name = "idx_visibility", columnList = "visibility")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Homework {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 100)
  private String authorId;

  @Column(nullable = false, length = 255)
  private String authorName;

  @Column(nullable = false, length = 50)
  private String authorRole; // STUDENT, TEACHER, ADMIN

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private HomeworkStatus status; // DRAFT, PUBLISHED, ARCHIVED

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private Visibility visibility; // PUBLIC, PRIVATE, RESTRICTED

  @OneToMany(mappedBy = "homework", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Attachment> attachments = new HashSet<>();

  @ManyToMany(cascade = CascadeType.PERSIST)
  @JoinTable(
      name = "homework_tags",
      joinColumns = @JoinColumn(name = "homework_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Column(nullable = false)
  private Long viewCount = 0L;

  @Column private Double rating = 0.0;

  public enum HomeworkStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
  }

  public enum Visibility {
    PUBLIC,
    PRIVATE,
    RESTRICTED
  }
}
