package com.university.homework.repository;


import com.university.homework.model.HomeworkDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeworkSearchRepository extends JpaRepository<HomeworkDocument, String> {
    // This repository can be used for custom search queries if needed in the future
}