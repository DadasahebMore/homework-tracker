package com.university.homework.repository;

import com.university.homework.entity.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA Repository for Tag entity */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String name);

  List<Tag> findByActiveTrue();

  List<Tag> findByActiveTrueOrderByUsageCountDesc();
}
