package com.university.homework.service;

import com.university.homework.dto.TagDTO;
import com.university.homework.entity.Tag;
import com.university.homework.repository.TagRepository;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for tag management and retrieval
 */
@Slf4j
@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Timed(value = "homework.tags.retrieval", description = "Time to retrieve tags")
    @Cacheable(value = "tags", unless = "#result.isEmpty()")
    public List<TagDTO> getAllActiveTags() {
        log.debug("Fetching all active tags");

        return tagRepository.findByActiveTrueOrderByUsageCountDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TagDTO convertToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .usageCount(tag.getUsageCount())
                .build();
    }
}