package com.university.homework.util;

import static com.university.homework.util.SearchConstants.CREATED_AT;

import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.entity.Homework;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/** Utility class for building Elasticsearch queries */
@Slf4j
@Component
public class SearchQueryBuilder {

  /** Generate cache key from search request */
  public static String generateCacheKey(SearchRequestDTO request) {
    return request.hashCode() + "";
  }

    private static void filterQueryByTag(SearchRequestDTO request, BoolQueryBuilder boolQuery) {
        if (!ObjectUtils.isEmpty(request.getTags() )) {
          for (String tag : request.getTags()) {
            boolQuery.filter(QueryBuilders.termQuery("tags.keyword", tag));
          }
        }
    }

    private static void filterQueryByAttachmentName(SearchRequestDTO request, BoolQueryBuilder boolQuery) {
        if (!ObjectUtils.isEmpty(request.getAttachmentName())) {
          boolQuery.filter(
              QueryBuilders.nestedQuery(
                  "attachments",
                  QueryBuilders.matchQuery("attachments.name", request.getAttachmentName()),
                  ScoreMode.None));
        }
    }

  /** Build SearchSourceBuilder from search request */
  public SearchSourceBuilder buildSearchSource(SearchRequestDTO request) {
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    // Build bool query
    sourceBuilder.query(this.buildBoolQuery(request));

    // Pagination
    int from = (request.getPage() - 1) * request.getPageSize();
    sourceBuilder.from(from);
    sourceBuilder.size(request.getPageSize());

    // Sorting
    applySorting(sourceBuilder, request);

    // Aggregations
    sourceBuilder.aggregation(AggregationBuilders.terms("tags_facet").field("tags.keyword").size(50).order(BucketOrder.count(false)));
    sourceBuilder.aggregation(AggregationBuilders.terms("authors_facet").field("authorName.keyword").size(20).order(BucketOrder.count(false)));

    return sourceBuilder;
  }

  /** Build bool query from search filters */
  private BoolQueryBuilder buildBoolQuery(SearchRequestDTO request) {
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    // MUST clauses - text search
    if (!ObjectUtils.isEmpty(request.getTitle())) {
      boolQuery.must(
          QueryBuilders.matchQuery("title", request.getTitle()).boost(2.0f).fuzziness(Fuzziness.AUTO).operator(Operator.AND));
    }

    if (!ObjectUtils.isEmpty(request.getDescription())) {
      boolQuery.must(QueryBuilders.matchQuery("description", request.getDescription()));
    }

    // FILTER clauses - exact matching
    if (!ObjectUtils.isEmpty(request.getAuthor())) {
      boolQuery.filter(QueryBuilders.termQuery("authorName.keyword", request.getAuthor()));
    }

    // Tags filter - all must match (AND logic)
      filterQueryByTag(request, boolQuery);

      // Date range filter
    if (!ObjectUtils.isEmpty(request.getDateTo())) {
      boolQuery.filter(buildDateRangeQuery(request));
    }

    // File size range filter
    if (!ObjectUtils.isEmpty(request.getFileSizeMin() ) ) {
      boolQuery.filter(buildFileSizeRangeQuery(request));
    }

    // Attachment name filter using nested query
      filterQueryByAttachmentName(request, boolQuery);

      // Role filter
    if (!ObjectUtils.isEmpty(request.getCreatedByRole() )) {
      boolQuery.filter(QueryBuilders.termsQuery("authorRole", request.getCreatedByRole()));
    }

    // Always filter for public visibility and published status
    boolQuery.filter(QueryBuilders.termQuery("visibility", Homework.Visibility.PUBLIC.name()));
    boolQuery.filter(QueryBuilders.termQuery("status", Homework.HomeworkStatus.PUBLISHED.name()));

    return boolQuery;
  }

    /** Build date range query */
  private org.elasticsearch.index.query.RangeQueryBuilder buildDateRangeQuery(
      SearchRequestDTO request) {
    org.elasticsearch.index.query.RangeQueryBuilder rangeQuery =
        QueryBuilders.rangeQuery(CREATED_AT);

    if (request.getDateFrom() != null) {
      rangeQuery.gte(request.getDateFrom().atStartOfDay());
    }
    if (request.getDateTo() != null) {
      rangeQuery.lte(request.getDateTo().atTime(23, 59, 59));
    }

    return rangeQuery;
  }

  /** Build file size range query */
  private org.elasticsearch.index.query.RangeQueryBuilder buildFileSizeRangeQuery(
      SearchRequestDTO request) {
    org.elasticsearch.index.query.RangeQueryBuilder rangeQuery =
        QueryBuilders.rangeQuery("fileSizeTotal");

    if (request.getFileSizeMin() != null) {
      rangeQuery.gte(request.getFileSizeMin());
    }
    if (request.getFileSizeMax() != null) {
      rangeQuery.lte(request.getFileSizeMax());
    }

    return rangeQuery;
  }

  /** Apply sorting to search results */
  private void applySorting(SearchSourceBuilder sourceBuilder, SearchRequestDTO request) {
    String sortBy = request.getSortBy();
    if ("date_desc".equals(sortBy)) {
      sourceBuilder.sort(SortBuilders.fieldSort(CREATED_AT).order(SortOrder.DESC));
    } else if ("size_asc".equals(sortBy)) {
      sourceBuilder.sort(SortBuilders.fieldSort("fileSizeTotal").order(SortOrder.ASC));
    } else { // Default: relevance
      sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
      sourceBuilder.sort(SortBuilders.fieldSort(CREATED_AT).order(SortOrder.DESC));
    }
  }
}
