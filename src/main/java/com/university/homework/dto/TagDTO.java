package com.university.homework.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Tag response DTO")
public class TagDTO {

    @Schema(description = "Tag identifier")
    private Long id;

    @Schema(description = "Tag name")
    private String name;

    @Schema(description = "Tag description")
    private String description;

    @Schema(description = "Number of homework items with this tag")
    private Long usageCount;
}
