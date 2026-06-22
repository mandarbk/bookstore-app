package com.bookstore.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bookstore.api.domain.LineItem;
import com.bookstore.api.dto.BookDto;
import com.bookstore.api.entity.Book;

@Service
public interface BookService {

    Book updateInventory(Long id, Integer quantity);

    void deleteBook(Long id);

    Book updateBook(Long id, Book bookUpdates);

    Book createBook(Book book);

    List<BookDto> getAvailableBooks(Integer minInventory);

    Long getTotalBooksCount();

    List<BookDto> searchBooksByTitle(String title);

    List<BookDto> getBooksByAuthor(String author);

    Optional<BookDto> getBookByIsbn(String isbn);

    List<BookDto> getAllBooks();

    Optional<BookDto> getBookById(String id);

    void checkoutCartForUser(String userId);

    void purchaseBook(String userId, LineItem bookToPurchase);

}
