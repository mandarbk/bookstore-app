package com.bookstore.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto implements Serializable {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private double price;
    private Integer inventory;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
