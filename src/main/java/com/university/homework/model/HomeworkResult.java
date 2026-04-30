package com.university.homework.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Homework result from search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HomeworkResult {

    private String id;
    private String title;
    private String description;
    private AuthorInfo author;
    private List<String> tags;
    private List<AttachmentInfo> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private double rating;
    private float score; // Elasticsearch score

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private String id;
        private String name;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String id;
        private String name;
        private long size;
        private String mimeType;
        private String url;
    }
}