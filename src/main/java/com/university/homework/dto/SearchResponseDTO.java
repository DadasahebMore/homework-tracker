package com.university.homework.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for search response */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Homework search response")
public class SearchResponseDTO {

  @Schema(description = "Request success flag")
  private boolean success;

  @Schema(description = "Total number of results matching search criteria")
  private long total;

  @Schema(description = "Current page number (1-based)")
  private int page;

  @Schema(description = "Results per page")
  private int perPage;

  @Schema(description = "Total number of pages")
  private int totalPages;

  @Schema(description = "List of homework results")
  private List<HomeworkResultDTO> results;

  @Schema(description = "Aggregated facets for filtering")
  private Map<String, List<FacetEntryDTO>> facets;

  @Schema(description = "Query execution time in milliseconds")
  private long executionTimeMs;

  @Schema(description = "Response timestamp")
  private LocalDateTime timestamp;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class HomeworkResultDTO {
    private String id;
    private String title;
    private String description;
    private AuthorDTO author;
    private List<String> tags;
    private List<AttachmentDTO> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long viewCount;
    private double rating;
    private float relevanceScore;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AuthorDTO {
    private String id;
    private String name;
    private String role;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class AttachmentDTO {
    private String id;
    private String name;
    private long size;
    private String mimeType;
    private String fileType;
    private String downloadUrl;
    private String streamUrl;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class FacetEntryDTO {
    private String name;
    private long count;
    private String filterUrl;
  }
}
