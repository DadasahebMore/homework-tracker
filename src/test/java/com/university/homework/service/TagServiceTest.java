package com.university.homework.service;

import com.university.homework.dto.TagDTO;
import com.university.homework.entity.Tag;
import com.university.homework.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getAllActiveTagsReturnsListOfDTOsOrderedByUsageCount() {
        Tag tag1 = Tag.builder().id(1L).name("math").description("Mathematics").usageCount(50L).active(true).build();
        Tag tag2 = Tag.builder().id(2L).name("algebra").description("Algebra topics").usageCount(100L).active(true).build();
        Tag tag3 = Tag.builder().id(3L).name("geometry").description("Geometry topics").usageCount(30L).active(true).build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag2, tag1, tag3));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(3, result.size());
        assertEquals("algebra", result.get(0).getName());
        assertEquals(100L, result.get(0).getUsageCount());
        assertEquals("math", result.get(1).getName());
        assertEquals(50L, result.get(1).getUsageCount());
        assertEquals("geometry", result.get(2).getName());
        assertEquals(30L, result.get(2).getUsageCount());
    }

    @Test
    void getAllActiveTagsReturnsEmptyListWhenNoActiveTags() {
        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of());

        List<TagDTO> result = tagService.getAllActiveTags();

        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    void getAllActiveTagsConvertsAllTagFieldsToDTO() {
        Tag tag = Tag.builder()
                .id(5L)
                .name("physics")
                .description("Physics and mechanics")
                .usageCount(75L)
                .active(true)
                .build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(1, result.size());
        TagDTO dto = result.get(0);
        assertEquals(5L, dto.getId());
        assertEquals("physics", dto.getName());
        assertEquals("Physics and mechanics", dto.getDescription());
        assertEquals(75L, dto.getUsageCount());
    }

    @Test
    void getAllActiveTagsSingleTagWithZeroUsageCount() {
        Tag tag = Tag.builder().id(10L).name("unused").description("Not used yet").usageCount(0L).active(true).build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(1, result.size());
        assertEquals("unused", result.get(0).getName());
        assertEquals(0L, result.get(0).getUsageCount());
    }

    @Test
    void getAllActiveTagsHandlesLargeUsageCountValues() {
        Tag tag1 = Tag.builder().id(1L).name("popular").description("Very popular").usageCount(999999L).active(true).build();
        Tag tag2 = Tag.builder().id(2L).name("less_popular").description("Less used").usageCount(100000L).active(true).build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag1, tag2));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(2, result.size());
        assertEquals(999999L, result.get(0).getUsageCount());
        assertEquals(100000L, result.get(1).getUsageCount());
    }

    @Test
    void getAllActiveTagsPreservesTagNameWithSpecialCharacters() {
        Tag tag = Tag.builder().id(1L).name("c++").description("C++ Programming").usageCount(45L).active(true).build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(1, result.size());
        assertEquals("c++", result.get(0).getName());
    }

    @Test
    void getAllActiveTagsPreservesNullDescription() {
        Tag tag = Tag.builder().id(1L).name("nodesc").description(null).usageCount(10L).active(true).build();

        when(tagRepository.findByActiveTrueOrderByUsageCountDesc()).thenReturn(List.of(tag));

        List<TagDTO> result = tagService.getAllActiveTags();

        assertEquals(1, result.size());
        assertNull(result.get(0).getDescription());
    }
}


