package com.university.homework.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends org.springframework.data.jpa.repository.JpaRepository<com.university.homework.entity.Tag, Long> {

    java.util.List<com.university.homework.entity.Tag> findByActiveTrueOrderByUsageCountDesc();
}
