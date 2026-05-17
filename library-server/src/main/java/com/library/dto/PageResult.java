package com.library.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;
}
