package com.bookstore.api.repository.jdbc;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.bookstore.api.dto.BookDto;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BookJdbcRepository {

    // Until we sort out db replication issues.
    // private final ReadFallbackTemplate fallbackTemplate;

    private final JdbcTemplate primaryJdbcTemplate;

    public BookJdbcRepository(JdbcTemplate primaryJdbcTemplate) {
        this.primaryJdbcTemplate = primaryJdbcTemplate;
    }

    public Optional<BookDto> findById(String id) {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books WHERE id = ?";
        try {
            BookDto book = primaryJdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BookDto.class), id);
            return Optional.ofNullable(book);
        } catch (Exception e) {
            log.error("Error finding book by id: {}", id, e);
            return Optional.empty();
        }
    }

    public List<BookDto> findAll() {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books";
        return primaryJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookDto.class));
    }

    public Optional<BookDto> findByIsbn(String isbn) {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books WHERE isbn = ?";
        try {
            BookDto book = primaryJdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BookDto.class), isbn);
            return Optional.ofNullable(book);
        } catch (Exception e) {
            log.error("Error finding book by isbn: {}", isbn, e);
            return Optional.empty();
        }
    }

    public List<BookDto> findByAuthor(String author) {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books WHERE author = ?";
        return primaryJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookDto.class), author);
    }

    public List<BookDto> findByTitle(String title) {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books WHERE title LIKE ?";
        return primaryJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookDto.class), "%" + title + "%");
    }

    public Long countAll() {
        String sql = "SELECT COUNT(*) FROM books";
        Long count = primaryJdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    public List<BookDto> findByInventoryGreaterThan(Integer minInventory) {
        String sql = "SELECT id, title, author, isbn, price, inventory, description, created_at, updated_at FROM books WHERE inventory > ?";
        return primaryJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BookDto.class), minInventory);
    }
}
