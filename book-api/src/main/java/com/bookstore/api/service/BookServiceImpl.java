package com.bookstore.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.api.domain.LineItem;
import com.bookstore.api.dto.BookDto;
import com.bookstore.api.entity.Book;
import com.bookstore.api.repository.jdbc.BookJdbcRepository;
import com.bookstore.api.repository.jpa.BookJpaRepository;
import com.bookstore.api.service.feign.CartServiceClient;
import com.bookstore.api.service.feign.OrderServiceClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookJdbcRepository bookJdbcRepository;
    private final BookJpaRepository bookJpaRepository;
    private final OrderServiceClient orderServiceClient;
    private final CartServiceClient cartServiceClient;

    public BookServiceImpl(BookJdbcRepository bookJdbcRepository, BookJpaRepository bookJpaRepository, OrderServiceClient orderServiceClient, CartServiceClient cartServiceClient) {
        this.bookJdbcRepository = bookJdbcRepository;
        this.bookJpaRepository = bookJpaRepository;
        this.orderServiceClient = orderServiceClient;
        this.cartServiceClient = cartServiceClient;
    }

    // READ operations - Using JDBC with fallback
    @Transactional(readOnly = true)
    @Override
    public Optional<BookDto> getBookById(String id) {
        log.debug("Fetching book by id: {}", id);
        return bookJdbcRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> getAllBooks() {
        log.debug("Fetching all books");
        return bookJdbcRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BookDto> getBookByIsbn(String isbn) {
        log.debug("Fetching book by isbn: {}", isbn);
        return bookJdbcRepository.findByIsbn(isbn);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> getBooksByAuthor(String author) {
        log.debug("Fetching books by author: {}", author);
        return bookJdbcRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> searchBooksByTitle(String title) {
        log.debug("Searching books by title: {}", title);
        return bookJdbcRepository.findByTitle(title);
    }

    @Transactional(readOnly = true)
    @Override
    public Long getTotalBooksCount() {
        return bookJdbcRepository.countAll();
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> getAvailableBooks(Integer minInventory) {
        log.debug("Fetching available books with inventory > {}", minInventory);
        return bookJdbcRepository.findByInventoryGreaterThan(minInventory);
    }

    // WRITE operations - Using JPA
    @Transactional
    @Override
    public Book createBook(Book book) {
        log.info("Creating new book: {}", book.getTitle());
        return bookJpaRepository.save(book);
    }

    @Transactional
    @Override
    public Book updateBook(Long id, Book bookUpdates) {
        log.info("Updating book with id: {}", id);
        Book book = bookJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        if (bookUpdates.getTitle() != null) book.setTitle(bookUpdates.getTitle());
        if (bookUpdates.getAuthor() != null) book.setAuthor(bookUpdates.getAuthor());
        if (bookUpdates.getIsbn() != null) book.setIsbn(bookUpdates.getIsbn());
        if (bookUpdates.getPrice() != null) book.setPrice(bookUpdates.getPrice());
        if (bookUpdates.getInventory() != null) book.setInventory(bookUpdates.getInventory());
        if (bookUpdates.getDescription() != null) book.setDescription(bookUpdates.getDescription());

        return bookJpaRepository.save(book);
    }

    @Transactional
    @Override
    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);
        bookJpaRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Book updateInventory(Long id, Integer quantity) {
        log.info("Updating inventory for book id: {}, quantity: {}", id, quantity);
        Book book = bookJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        book.setInventory(book.getInventory() + quantity);
        return bookJpaRepository.save(book);
    }

    @Transactional
    @Override
    public void purchaseBook(String userId, LineItem bookToPurchase) {
        /// Manage inventory and other purchase logic here
        log.info("Processing purchase for book id: {}, quantity: {}", bookToPurchase.getBookId(), bookToPurchase.getQuantity());
        cartServiceClient.addItem(userId, bookToPurchase);
    }

    @Override
    public void checkoutCartForUser(String userid) {
        // Call order service to place order and clear cart
        log.info("About to place order for {} ", userid);
        orderServiceClient.placeOrder(userid);
    }
}
