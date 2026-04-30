package com.university.homework.util;

import com.university.homework.dto.SearchRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

/**
 * Utility class for building Elasticsearch queries
 */
@Slf4j
@Component
public class SearchQueryBuilder {

    /**
     * Generate cache key from search request
     */
    public static String generateCacheKey(SearchRequestDTO request) {
        return request.hashCode() + "";
    }

    /**
     * Build SearchSourceBuilder from search request
     */
    public SearchSourceBuilder buildSearchSource(SearchRequestDTO request) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // Build bool query
        BoolQueryBuilder boolQuery = buildBoolQuery(request);
        sourceBuilder.query(boolQuery);

        // Pagination
        int from = (request.getPage() - 1) * request.getPageSize();
        sourceBuilder.from(from);
        sourceBuilder.size(request.getPageSize());

        // Sorting
        applySorting(sourceBuilder, request);

        // Aggregations
        sourceBuilder.aggregation(
                AggregationBuilders.terms("tags_facet")
                        .field("tags.keyword")
                        .size(50)
        );
        sourceBuilder.aggregation(
                AggregationBuilders.terms("authors_facet")
                        .field("authorName.keyword")
                        .size(20)
        );

        return sourceBuilder;
    }

    /**
     * Build bool query from search filters
     */
    private BoolQueryBuilder buildBoolQuery(SearchRequestDTO request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // MUST clauses - text search
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("title", request.getTitle())
                    .boost(2.0f)
                    .fuzziness("AUTO"));
        }

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("description", request.getDescription()));
        }

        // FILTER clauses - exact matching
        if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("authorName.keyword", request.getAuthor()));
        }

        // Tags filter - all must match (AND logic)
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                boolQuery.filter(QueryBuilders.termQuery("tags.keyword", tag));
            }
        }

        // Date range filter
        if (request.getDateFrom() != null || request.getDateTo() != null) {
            boolQuery.filter(buildDateRangeQuery(request));
        }

        // File size range filter
        if (request.getFileSizeMin() != null || request.getFileSizeMax() != null) {
            boolQuery.filter(buildFileSizeRangeQuery(request));
        }

        // Attachment name filter using nested query
        if (request.getAttachmentName() != null && !request.getAttachmentName().isEmpty()) {
            boolQuery.filter(QueryBuilders.nestedQuery(
                    "attachments",
                    QueryBuilders.matchQuery("attachments.name", request.getAttachmentName()),
                    ScoreMode.None
            ));
        }

        // Role filter
        if (request.getCreatedByRole() != null && !request.getCreatedByRole().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("authorRole", request.getCreatedByRole()));
        }

        // Always filter for public visibility and published status
        boolQuery.filter(QueryBuilders.termQuery("visibility", "PUBLIC"));
        boolQuery.filter(QueryBuilders.termQuery("status", "PUBLISHED"));

        return boolQuery;
    }

    /**
     * Build date range query
     */
    private org.elasticsearch.index.query.RangeQueryBuilder buildDateRangeQuery(SearchRequestDTO request) {
        org.elasticsearch.index.query.RangeQueryBuilder rangeQuery =
                QueryBuilders.rangeQuery("createdAt");

        if (request.getDateFrom() != null) {
            rangeQuery.gte(request.getDateFrom().atStartOfDay());
        }
        if (request.getDateTo() != null) {
            rangeQuery.lte(request.getDateTo().atTime(23, 59, 59));
        }

        return rangeQuery;
    }

    /**
     * Build file size range query
     */
    private org.elasticsearch.index.query.RangeQueryBuilder buildFileSizeRangeQuery(SearchRequestDTO request) {
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

    /**
     * Apply sorting to search results
     */
    private void applySorting(SearchSourceBuilder sourceBuilder, SearchRequestDTO request) {
        String sortBy = request.getSortBy();

        if ("date_desc".equals(sortBy)) {
            sourceBuilder.sort(SortBuilders.fieldSort("createdAt")
                    .order(SortOrder.DESC));
        } else if ("size_asc".equals(sortBy)) {
            sourceBuilder.sort(SortBuilders.fieldSort("fileSizeTotal")
                    .order(SortOrder.ASC));
        } else { // Default: relevance
            sourceBuilder.sort(SortBuilders.scoreSort()
                    .order(SortOrder.DESC));
            sourceBuilder.sort(SortBuilders.fieldSort("createdAt")
                    .order(SortOrder.DESC));
        }
    }
}