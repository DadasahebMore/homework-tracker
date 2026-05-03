package com.university.homework.controller;

import com.university.homework.dto.SearchRequestDTO;
import com.university.homework.dto.SearchResponseDTO;
import com.university.homework.dto.TagDTO;
import com.university.homework.service.SearchService;
import com.university.homework.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST controller for homework search API */
@Slf4j
@RestController
@RequestMapping("/api/v1/homework")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Homework Search", description = "APIs for searching and retrieving homework")
public class SearchController {

  private final SearchService searchService;
  private final TagService tagService;

  public SearchController(SearchService searchService, TagService tagService) {
    this.searchService = searchService;
    this.tagService = tagService;
  }

  /** Search homework with advanced filtering */
  @GetMapping("/search")
  @Operation(
      summary = "Search homework",
      description = "Search homework by title, author, tags, attachments, and other criteria")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<SearchResponseDTO> search(@Valid @ModelAttribute SearchRequestDTO request) {

    log.info("Search request received: {}", request);

    try {
      SearchResponseDTO response = searchService.search(request);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Search failed", e);
      SearchResponseDTO errorResponse =
          SearchResponseDTO.builder().success(false).results(List.of()).build();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /** Get all available tags for filtering */
  @GetMapping("/tags")
  @Operation(
      summary = "Get all tags",
      description = "Retrieve all available tags for homework classification")
  @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
  public ResponseEntity<List<TagDTO>> getTags() {
    log.debug("Fetching all tags");

    List<TagDTO> tags = tagService.getAllActiveTags();
    return ResponseEntity.ok(tags);
  }

  /** Health check endpoint */
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Check if search service is running")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "homework-search-service"));
  }
}
