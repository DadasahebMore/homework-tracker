package com.university.homework.controller;

import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.dto.SearchResponseDTO;
import com.university.homework.dto.TagDTO;
import com.university.homework.service.SearchService;
import com.university.homework.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private SearchController searchController;

    private SearchRequestDTO request;

    @BeforeEach
    void setUp() {
        request = SearchRequestDTO.builder()
                .title("Linear Algebra")
                .page(1)
                .pageSize(20)
                .sortBy("relevance")
                .build();
    }

    @Test
    void searchReturnsOkWhenServiceReturnsResults() {
        SearchResponseDTO response = SearchResponseDTO.builder()
                .success(true)
                .total(2)
                .results(List.of(
                        SearchResponseDTO.HomeworkResultDTO.builder().id("h1").title("A").build(),
                        SearchResponseDTO.HomeworkResultDTO.builder().id("h2").title("B").build()
                ))
                .build();

        when(searchService.search(request)).thenReturn(response);

        ResponseEntity<SearchResponseDTO> entity = searchController.search(request);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(response, entity.getBody());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().isSuccess());
        assertEquals(2, entity.getBody().getTotal());
    }

    @Test
    void searchReturnsInternalServerErrorWhenServiceThrowsException() {
        when(searchService.search(any(SearchRequestDTO.class))).thenThrow(new RuntimeException("boom"));

        ResponseEntity<SearchResponseDTO> entity = searchController.search(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertFalse(entity.getBody().isSuccess());
        assertNotNull(entity.getBody().getResults());
        assertTrue(entity.getBody().getResults().isEmpty());
    }

    @Test
    void getTagsReturnsOkWithTagListFromService() {
        List<TagDTO> tags = List.of(
                TagDTO.builder().id(1L).name("math").description("Math tag").usageCount(10L).build(),
                TagDTO.builder().id(2L).name("physics").description("Physics tag").usageCount(5L).build()
        );

        when(tagService.getAllActiveTags()).thenReturn(tags);

        ResponseEntity<List<TagDTO>> entity = searchController.getTags();

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(tags, entity.getBody());
        assertNotNull(entity.getBody());
        assertEquals(2, entity.getBody().size());
    }

    @Test
    void getTagsReturnsOkWithEmptyList() {
        when(tagService.getAllActiveTags()).thenReturn(List.of());

        ResponseEntity<List<TagDTO>> entity = searchController.getTags();

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().isEmpty());
    }

    @Test
    void healthReturnsUpStatusAndServiceName() {
        ResponseEntity<Map<String, String>> entity = searchController.health();

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertNotNull(entity.getBody());
        assertEquals("UP", entity.getBody().get("status"));
        assertEquals("homework-search-service", entity.getBody().get("service"));
    }
}

