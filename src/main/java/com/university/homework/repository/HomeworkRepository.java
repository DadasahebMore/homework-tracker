package com.university.homework.repository;

import com.university.homework.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for Homework entity
 */
@Repository
public interface HomeworkRepository extends JpaRepository<Homework, String> {

    List<Homework> findByAuthorIdAndStatusOrderByCreatedAtDesc(
            String authorId,
            Homework.HomeworkStatus status
    );

    List<Homework> findByVisibilityAndStatusOrderByCreatedAtDesc(
            Homework.Visibility visibility,
            Homework.HomeworkStatus status
    );

    @Query("SELECT h FROM Homework h WHERE h.createdAt BETWEEN :from AND :to ORDER BY h.createdAt DESC")
    List<Homework> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT h FROM Homework h WHERE h.visibility = 'PUBLIC' AND h.status = 'PUBLISHED' " +
            "ORDER BY h.viewCount DESC LIMIT 10")
    List<Homework> findTopPublicHomework();

    long countByAuthorIdAndStatus(String authorId, Homework.HomeworkStatus status);
}