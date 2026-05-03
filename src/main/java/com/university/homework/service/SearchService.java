package com.university.homework.service;

import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.dto.SearchResponseDTO;
import com.university.homework.entity.Homework;
import com.university.homework.exception.SearchException;
import com.university.homework.model.HomeworkDocument;
import com.university.homework.repository.HomeworkRepository;
import com.university.homework.repository.HomeworkSearchRepository;
import com.university.homework.util.SearchQueryBuilder;
import io.micrometer.core.annotation.Timed;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** Service for homework search functionality */
@Slf4j
@Service
public class SearchService {

  private static final String HOMEWORK_INDEX = "homework";
  private static final int MAX_RESULTS = 1000;
  private final RestHighLevelClient elasticsearchClient;
  private final HomeworkSearchRepository homeworkSearchRepository;
  private final HomeworkRepository homeworkRepository;
  private final SearchQueryBuilder queryBuilder;

  public SearchService(
      RestHighLevelClient elasticsearchClient,
      HomeworkSearchRepository homeworkSearchRepository,
      HomeworkRepository homeworkRepository,
      SearchQueryBuilder queryBuilder) {
    this.elasticsearchClient = elasticsearchClient;
    this.homeworkSearchRepository = homeworkSearchRepository;
    this.homeworkRepository = homeworkRepository;
    this.queryBuilder = queryBuilder;
  }

  @Timed(value = "homework.search.duration", description = "Time taken to search homework")
  @Cacheable(
      value = "homework-search",
      key =
          "T(java.lang.String).format('%s_%s_%d', "
              + "@com.university.homework.util.SearchQueryBuilder@generateCacheKey(#request), "
              + "#request.page, #request.pageSize)",
      unless = "#result.total == 0")
  public SearchResponseDTO search(SearchRequestDTO request) {
    log.info("Searching homework with filters: {}", request);

    long startTime = System.currentTimeMillis();

    try {
      // Set defaults
      if (request.getPage() == null || request.getPage() < 1) {
        request.setPage(1);
      }
      if (request.getPageSize() == null || request.getPageSize() < 1) {
        request.setPageSize(20);
      }
      if (request.getSortBy() == null) {
        request.setSortBy("relevance");
      }

      // Validate page size
      if (request.getPageSize() > 100) {
        request.setPageSize(100);
      }

      // Build Elasticsearch query
      SearchSourceBuilder sourceBuilder = queryBuilder.buildSearchSource(request);
      SearchRequest searchRequest = new SearchRequest(HOMEWORK_INDEX);
      searchRequest.source(sourceBuilder);

      // Execute search
      SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);

      // Process and return results
      SearchResponseDTO result = processSearchResponse(response, request, startTime);

      log.info(
          "Search completed in {} ms, found {} results",
          System.currentTimeMillis() - startTime,
          result.getTotal());

      return result;

    } catch (IOException e) {
      log.error("Error searching homework", e);
      throw new SearchException("Failed to search homework: " + e.getMessage(), e);
    }
  }

  private SearchResponseDTO processSearchResponse(
      SearchResponse response, SearchRequestDTO request, long startTime) {
    SearchResponseDTO.SearchResponseDTOBuilder builder =
        SearchResponseDTO.builder()
            .success(true)
            .total(response.getHits().getTotalHits().value)
            .page(request.getPage())
            .perPage(request.getPageSize())
            .executionTimeMs(System.currentTimeMillis() - startTime)
            .timestamp(LocalDateTime.now(ZoneId.of("UTC")));

    // Calculate total pages
    long totalPages =
        (response.getHits().getTotalHits().value + request.getPageSize() - 1)
            / request.getPageSize();
    builder.totalPages((int) totalPages);

    // Convert hits to DTOs
    List<SearchResponseDTO.HomeworkResultDTO> results =
        Arrays.stream(response.getHits().getHits())
            .map(this::convertSearchHit)
            .collect(Collectors.toList());
    builder.results(results);

    // Extract facets
    Map<String, List<SearchResponseDTO.FacetEntryDTO>> facets = extractFacets(response, request);
    builder.facets(facets);

    return builder.build();
  }

  private SearchResponseDTO.HomeworkResultDTO convertSearchHit(SearchHit hit) {
    try {
      // HomeworkDocument doc = new SearchResponseDTO.HomeworkResultDTO();
      Map<String, Object> source = hit.getSourceAsMap();

      return SearchResponseDTO.HomeworkResultDTO.builder()
          .id(hit.getId())
          .title((String) source.get("title"))
          .description((String) source.get("description"))
          .author(
              SearchResponseDTO.AuthorDTO.builder()
                  .id((String) source.get("authorId"))
                  .name((String) source.get("authorName"))
                  .role((String) source.get("authorRole"))
                  .build())
          .tags((List<String>) source.get("tags"))
          .createdAt(null) // Parse from source if needed
          .viewCount(((Number) source.get("viewCount")).longValue())
          .rating(((Number) source.get("rating")).doubleValue())
          .relevanceScore(hit.getScore())
          .build();

    } catch (Exception e) {
      log.error("Error converting search hit: {}", hit.getId(), e);
      throw new SearchException("Failed to convert search result", e);
    }
  }

  private Map<String, List<SearchResponseDTO.FacetEntryDTO>> extractFacets(
      SearchResponse response, SearchRequestDTO request) {
    Map<String, List<SearchResponseDTO.FacetEntryDTO>> facets = new HashMap<>();

    try {
      // Extract tags facet
      if (response.getAggregations() != null) {
        Terms tagsAgg = response.getAggregations().get("tags_facet");
        if (tagsAgg != null) {
          List<SearchResponseDTO.FacetEntryDTO> tagsList =
              tagsAgg.getBuckets().stream()
                  .map(
                      bucket ->
                          SearchResponseDTO.FacetEntryDTO.builder()
                              .name(bucket.getKeyAsString())
                              .count(bucket.getDocCount())
                              .build())
                  .collect(Collectors.toList());
          facets.put("tags", tagsList);
        }

        // Extract authors facet
        Terms authorsAgg = response.getAggregations().get("authors_facet");
        if (authorsAgg != null) {
          List<SearchResponseDTO.FacetEntryDTO> authorsList =
              authorsAgg.getBuckets().stream()
                  .map(
                      bucket ->
                          SearchResponseDTO.FacetEntryDTO.builder()
                              .name(bucket.getKeyAsString())
                              .count(bucket.getDocCount())
                              .build())
                  .collect(Collectors.toList());
          facets.put("authors", authorsList);
        }
      }
    } catch (Exception e) {
      log.warn("Error extracting facets", e);
    }

    return facets;
  }

  /** Index a homework document in Elasticsearch */
  public void indexHomework(Homework homework) {
    try {
      HomeworkDocument document = convertHomeworkToDocument(homework);
      homeworkSearchRepository.save(document);
      log.info("Indexed homework: {}", homework.getId());
    } catch (Exception e) {
      log.error("Error indexing homework: {}", homework.getId(), e);
    }
  }

  /** Delete homework document from Elasticsearch */
  public void deleteHomework(String homeworkId) {
    try {
      homeworkSearchRepository.deleteById(homeworkId);
      log.info("Deleted homework from index: {}", homeworkId);
    } catch (Exception e) {
      log.error("Error deleting homework from index: {}", homeworkId, e);
    }
  }

  private HomeworkDocument convertHomeworkToDocument(Homework homework) {
    return HomeworkDocument.builder()
        .id(homework.getId())
        .title(homework.getTitle())
        .description(homework.getDescription())
        .authorId(homework.getAuthorId())
        .authorName(homework.getAuthorName())
        .authorRole(homework.getAuthorRole())
        .status(homework.getStatus().name())
        .visibility(homework.getVisibility().name())
        .tags(homework.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toList()))
        .createdAt(homework.getCreatedAt())
        .updatedAt(homework.getUpdatedAt())
        .viewCount(homework.getViewCount())
        .rating(homework.getRating())
        .build();
  }
}
