package com.university.homework.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/** Attachment entity representing files associated with homework */
@Entity
@Table(
    name = "attachments",
    indexes = {
      @Index(name = "idx_homework_id", columnList = "homework_id"),
      @Index(name = "idx_file_key", columnList = "file_key")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "homework_id", nullable = false)
  private Homework homework;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 500)
  private String fileKey; // S3/Cloud storage key

  @Column(nullable = false)
  private Long fileSize; // in bytes

  @Column(nullable = false, length = 100)
  private String mimeType;

  @Column(length = 50)
  private String fileType; // DOCUMENT, IMAGE, VIDEO, AUDIO, OTHER

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column private Long downloadCount = 0L;
}
