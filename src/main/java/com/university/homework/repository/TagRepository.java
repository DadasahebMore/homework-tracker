package com.university.homework.repository;

import org.springframework.stereotype.Repository;

import com.university.homework.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Tag entity
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByActiveTrue();

    List<Tag> findByActiveTrueOrderByUsageCountDesc();
}
