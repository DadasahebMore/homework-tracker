package com.university.homework.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for search request */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Homework search request")
public class SearchRequestDTO {

  @Schema(description = "Homework title (partial match)", example = "Linear Algebra")
  @Size(max = 255, message = "Title must not exceed 255 characters")
  private String title;

  @Schema(description = "Description text to search", example = "Matrix operations")
  private String description;

  @Schema(description = "Author name (exact match)", example = "Dr. Smith")
  private String author;

  @Schema(description = "List of tags (AND logic)", example = "[\"math\", \"algebra\"]")
  private List<String> tags;

  @Schema(description = "Attachment file name (partial match)", example = "solution")
  private String attachmentName;

  @Schema(description = "Start date for created date range", example = "2026-04-01")
  @PastOrPresent(message = "Date from must not be in the future")
  private LocalDate dateFrom;

  @Schema(description = "End date for created date range", example = "2026-04-30")
  @PastOrPresent(message = "Date to must not be in the future")
  private LocalDate dateTo;

  @Schema(description = "Minimum file size in bytes", example = "0")
  @PositiveOrZero(message = "File size min must be >= 0")
  private Long fileSizeMin;

  @Schema(description = "Maximum file size in bytes", example = "1073741824")
  @Positive(message = "File size max must be > 0")
  private Long fileSizeMax;

  @Schema(description = "Filter by creator role", example = "[\"TEACHER\", \"STUDENT\"]")
  private List<String> createdByRole;

  @Schema(
      description = "Sort order: relevance, date_desc, size_asc",
      example = "relevance",
      defaultValue = "relevance")
  @Pattern(
      regexp = "relevance|date_desc|size_asc",
      message = "Sort must be one of: relevance, date_desc, size_asc")
  private String sortBy;

  @Schema(description = "Page number (1-based)", example = "1", defaultValue = "1")
  @Positive(message = "Page must be >= 1")
  private Integer page;

  @Schema(description = "Results per page", example = "20", defaultValue = "20")
  @Min(value = 1, message = "Page size must be >= 1")
  @Max(value = 100, message = "Page size must be <= 100")
  private Integer pageSize;
}
