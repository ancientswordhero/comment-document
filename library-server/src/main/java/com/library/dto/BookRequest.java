package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "书名不能为空")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100)
    private String author;

    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20)
    private String isbn;

    private Long categoryId;
    private String coverUrl;
    private String description;
    private Integer status;
}
