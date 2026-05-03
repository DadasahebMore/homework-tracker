package com.university.homework.repository;

import com.university.homework.model.HomeworkDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/** Elasticsearch repository for HomeworkDocument */
@Repository
public interface HomeworkSearchRepository
    extends ElasticsearchRepository<HomeworkDocument, String> {}
