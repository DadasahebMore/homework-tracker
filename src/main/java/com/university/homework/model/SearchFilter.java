package com.university.homework.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Filter criteria for homework search */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchFilter {

  private String title;
  private String description;
  private String author;
  private List<String> tags;
  private String attachmentName;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private Long fileSizeMin;
  private Long fileSizeMax;
  private List<String> createdByRole;
  private String sortBy; // "relevance", "date_desc", "size_asc"

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
