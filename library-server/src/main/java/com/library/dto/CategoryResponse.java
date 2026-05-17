package com.library.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private int sortOrder;
    private List<CategoryResponse> children;
}
