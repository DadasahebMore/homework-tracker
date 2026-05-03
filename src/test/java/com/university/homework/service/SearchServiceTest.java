package com.university.homework.service;

import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.dto.SearchResponseDTO;
import com.university.homework.entity.Homework;
import com.university.homework.entity.Tag;
import com.university.homework.exception.SearchException;
import com.university.homework.model.HomeworkDocument;
import com.university.homework.repository.HomeworkRepository;
import com.university.homework.repository.HomeworkSearchRepository;
import com.university.homework.util.SearchQueryBuilder;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private RestHighLevelClient elasticsearchClient;

    @Mock
    private HomeworkSearchRepository homeworkSearchRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private SearchQueryBuilder queryBuilder;

    @InjectMocks
    private SearchService searchService;

    private SearchRequestDTO searchRequest;

    @BeforeEach
    void setUp() {
        searchRequest = SearchRequestDTO.builder()
                .title("Linear Algebra")
                .sortBy("relevance")
                .page(1)
                .pageSize(20)
                .build();
    }

    @Test
    void searchReturnsResultsAndFacetsWhenElasticsearchRespondsSuccessfully() throws Exception {
        SearchResponse response = mockSearchResponseWithTwoHitsAndFacets();
        when(queryBuilder.buildSearchSource(any(SearchRequestDTO.class))).thenReturn(new SearchSourceBuilder());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(response);

        SearchResponseDTO result = searchService.search(searchRequest);

        assertTrue(result.isSuccess());
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getPage());
        assertEquals(20, result.getPerPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getResults().size());
        assertEquals("h1", result.getResults().get(0).getId());
        assertEquals("Teacher A", result.getResults().get(0).getAuthor().getName());
        assertNotNull(result.getFacets());
        assertTrue(result.getFacets().containsKey("tags"));
        assertTrue(result.getFacets().containsKey("authors"));
        assertNotNull(result.getTimestamp());
        assertEquals(20, searchRequest.getPageSize());
    }

    @Test
    void searchAppliesDefaultPaginationAndSortWhenRequestHasInvalidValues() throws Exception {
        SearchRequestDTO request = SearchRequestDTO.builder().page(0).pageSize(null).sortBy(null).build();
        SearchResponse response = mockSearchResponseWithTotalAndNoHits(0L);

        when(queryBuilder.buildSearchSource(any(SearchRequestDTO.class))).thenReturn(new SearchSourceBuilder());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(response);

        SearchResponseDTO result = searchService.search(request);

        assertEquals(1, request.getPage());
        assertEquals(20, request.getPageSize());
        assertEquals("relevance", request.getSortBy());
        assertEquals(1, result.getPage());
        assertEquals(20, result.getPerPage());
        assertEquals(0, result.getTotal());
    }

    @Test
    void searchCapsPageSizeAtHundredWhenRequestExceedsMaximum() throws Exception {
        SearchRequestDTO request = SearchRequestDTO.builder().page(1).pageSize(1000).sortBy("relevance").build();
        SearchResponse response = mockSearchResponseWithTotalAndNoHits(0L);

        when(queryBuilder.buildSearchSource(any(SearchRequestDTO.class))).thenReturn(new SearchSourceBuilder());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(response);

        SearchResponseDTO result = searchService.search(request);

        assertEquals(100, request.getPageSize());
        assertEquals(100, result.getPerPage());
    }

    @Test
    void searchThrowsSearchExceptionWhenElasticsearchClientThrowsIOException() throws Exception {
        when(queryBuilder.buildSearchSource(any(SearchRequestDTO.class))).thenReturn(new SearchSourceBuilder());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT)))
                .thenThrow(new IOException("connection down"));

        SearchException exception = assertThrows(SearchException.class, () -> searchService.search(searchRequest));

        assertTrue(exception.getMessage().contains("Failed to search homework"));
        assertTrue(exception.getMessage().contains("connection down"));
    }

    @Test
    void searchThrowsSearchExceptionWhenSearchHitCannotBeConverted() throws Exception {
        SearchResponse response = mockSearchResponseWithInvalidHitPayload();

        when(queryBuilder.buildSearchSource(any(SearchRequestDTO.class))).thenReturn(new SearchSourceBuilder());
        when(elasticsearchClient.search(any(SearchRequest.class), eq(RequestOptions.DEFAULT))).thenReturn(response);

        SearchException exception = assertThrows(SearchException.class, () -> searchService.search(searchRequest));

        assertEquals("Failed to convert search result", exception.getMessage());
    }

    @Test
    void indexHomeworkSavesConvertedDocumentWhenInputIsValid() {
        Homework homework = Homework.builder()
                .id("hw-1")
                .title("Linear Algebra Notes")
                .description("Week 1")
                .authorId("u-1")
                .authorName("Teacher A")
                .authorRole("TEACHER")
                .status(Homework.HomeworkStatus.PUBLISHED)
                .visibility(Homework.Visibility.PUBLIC)
                .tags(Set.of(
                        Tag.builder().id(1L).name("math").build(),
                        Tag.builder().id(2L).name("algebra").build()))
                .createdAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 2, 10, 0))
                .viewCount(12L)
                .rating(4.5)
                .build();

        searchService.indexHomework(homework);

        ArgumentCaptor<HomeworkDocument> documentCaptor = ArgumentCaptor.forClass(HomeworkDocument.class);
        verify(homeworkSearchRepository).save(documentCaptor.capture());
        HomeworkDocument document = documentCaptor.getValue();

        assertEquals("hw-1", document.getId());
        assertEquals("Linear Algebra Notes", document.getTitle());
        assertEquals("PUBLISHED", document.getStatus());
        assertEquals("PUBLIC", document.getVisibility());
        assertTrue(document.getTags().contains("math"));
        assertTrue(document.getTags().contains("algebra"));
    }

    @Test
    void indexHomeworkDoesNotPropagateExceptionWhenRepositorySaveFails() {
        Homework homework = Homework.builder()
                .id("hw-2")
                .title("Physics")
                .authorId("u-2")
                .authorName("Teacher B")
                .authorRole("TEACHER")
                .status(Homework.HomeworkStatus.PUBLISHED)
                .visibility(Homework.Visibility.PUBLIC)
                .tags(Set.of(Tag.builder().name("physics").build()))
                .build();

        when(homeworkSearchRepository.save(any(HomeworkDocument.class)))
                .thenThrow(new RuntimeException("save failed"));

        assertDoesNotThrow(() -> searchService.indexHomework(homework));
    }

    @Test
    void deleteHomeworkRemovesDocumentById() {
        searchService.deleteHomework("hw-3");

        verify(homeworkSearchRepository).deleteById("hw-3");
    }

    @Test
    void deleteHomeworkDoesNotPropagateExceptionWhenRepositoryDeleteFails() {
        doThrow(new RuntimeException("delete failed")).when(homeworkSearchRepository).deleteById("hw-4");

        assertDoesNotThrow(() -> searchService.deleteHomework("hw-4"));
    }

    private SearchResponse mockSearchResponseWithTwoHitsAndFacets() {
        SearchResponse response = mock(SearchResponse.class);
        SearchHits hits = mock(SearchHits.class);
        SearchHit hit1 = mock(SearchHit.class);
        SearchHit hit2 = mock(SearchHit.class);
        Aggregations aggregations = mock(Aggregations.class);
        Terms tagsFacet = mock(Terms.class);
        Terms authorsFacet = mock(Terms.class);
        Terms.Bucket tagsBucket = mock(Terms.Bucket.class);
        Terms.Bucket authorsBucket = mock(Terms.Bucket.class);

        when(response.getHits()).thenReturn(hits);
        when(hits.getTotalHits()).thenReturn(new TotalHits(2L, TotalHits.Relation.EQUAL_TO));
        when(hits.getHits()).thenReturn(new SearchHit[] {hit1, hit2});

        when(hit1.getId()).thenReturn("h1");
        when(hit1.getScore()).thenReturn(1.2f);
        when(hit1.getSourceAsMap()).thenReturn(Map.of(
                "title", "Linear Algebra",
                "description", "Matrices",
                "authorId", "u-1",
                "authorName", "Teacher A",
                "authorRole", "TEACHER",
                "tags", List.of("math"),
                "viewCount", 15L,
                "rating", 4.8
        ));

        when(hit2.getId()).thenReturn("h2");
        when(hit2.getScore()).thenReturn(0.9f);
        when(hit2.getSourceAsMap()).thenReturn(Map.of(
                "title", "Calculus",
                "description", "Limits",
                "authorId", "u-2",
                "authorName", "Teacher B",
                "authorRole", "TEACHER",
                "tags", List.of("math", "calculus"),
                "viewCount", 22L,
                "rating", 4.2
        ));

        when(response.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("tags_facet")).thenReturn(tagsFacet);
        when(aggregations.get("authors_facet")).thenReturn(authorsFacet);

        when(tagsBucket.getKeyAsString()).thenReturn("math");
        when(tagsBucket.getDocCount()).thenReturn(5L);
        when(tagsFacet.getBuckets()).thenReturn((List) List.of(tagsBucket));

        when(authorsBucket.getKeyAsString()).thenReturn("Teacher A");
        when(authorsBucket.getDocCount()).thenReturn(2L);
        when(authorsFacet.getBuckets()).thenReturn((List) List.of(authorsBucket));

        return response;
    }

    private SearchResponse mockSearchResponseWithTotalAndNoHits(long total) {
        SearchResponse response = mock(SearchResponse.class);
        SearchHits hits = mock(SearchHits.class);

        when(response.getHits()).thenReturn(hits);
        when(hits.getTotalHits()).thenReturn(new TotalHits(total, TotalHits.Relation.EQUAL_TO));
        when(hits.getHits()).thenReturn(new SearchHit[0]);
        when(response.getAggregations()).thenReturn(null);

        return response;
    }

    private SearchResponse mockSearchResponseWithInvalidHitPayload() {
        SearchResponse response = mock(SearchResponse.class);
        SearchHits hits = mock(SearchHits.class);
        SearchHit invalidHit = mock(SearchHit.class);

        when(response.getHits()).thenReturn(hits);
        when(hits.getTotalHits()).thenReturn(new TotalHits(1L, TotalHits.Relation.EQUAL_TO));
        when(hits.getHits()).thenReturn(new SearchHit[] {invalidHit});
        //when(response.getAggregations()).thenReturn(null);

        when(invalidHit.getId()).thenReturn("bad-hit");
        when(invalidHit.getSourceAsMap()).thenReturn(Map.of(
                "title", "Broken",
                "description", "Invalid payload",
                "authorId", "u-9",
                "authorName", "Teacher Z",
                "authorRole", "TEACHER",
                "tags", List.of("math")
        ));

        return response;
    }
}