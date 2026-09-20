package com.softdreams.activityhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {

    @NotBlank
    @Size(min = 2, max = 50)
    String code;

    @NotBlank
    @Size(min = 2, max = 255)
    String name;

    String description;

    @Builder.Default
    boolean active = true;
}
