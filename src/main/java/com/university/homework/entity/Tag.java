package com.university.homework.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tag entity for categorizing homework */
@Entity
@Table(
    name = "tags",
    uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  @Builder.Default
  private Long usageCount = 0L;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;
}
