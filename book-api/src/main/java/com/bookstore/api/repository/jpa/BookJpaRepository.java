package com.bookstore.api.repository.jpa;

import com.bookstore.api.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
