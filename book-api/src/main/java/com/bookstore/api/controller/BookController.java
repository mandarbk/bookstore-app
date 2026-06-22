package com.bookstore.api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.api.domain.LineItem;
import com.bookstore.api.dto.BookDto;
import com.bookstore.api.entity.Book;
import com.bookstore.api.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@Slf4j
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService, ObjectMapper sessionSerializer) {
        this.bookService = bookService;
    }

    @GetMapping
    // @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<List<BookDto>> getAllBooks() {
        log.info("GET /api/books - Fetching all books");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<BookDto> getBookById(@PathVariable String id) {
        log.info("GET /api/books/{} - Fetching book by id", id);
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<BookDto> getBookByIsbn(@PathVariable String isbn) {
        log.info("GET /api/books/isbn/{} - Fetching book by isbn", isbn);
        return bookService.getBookByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/author/{author}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<List<BookDto>> getBooksByAuthor(@PathVariable String author) {
        log.info("GET /api/books/author/{} - Fetching books by author", author);
        return ResponseEntity.ok(bookService.getBooksByAuthor(author));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam String title) {
        log.info("GET /api/books/search - Searching books by title: {}", title);
        return ResponseEntity.ok(bookService.searchBooksByTitle(title));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_write:books')")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        log.info("POST /api/books - Creating new book: {}", book.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:books')")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book bookUpdates) {
        log.info("PUT /api/books/{} - Updating book", id);
        try {
            return ResponseEntity.ok(bookService.updateBook(id, bookUpdates));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:books')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{} - Deleting book", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/inventory")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:books')")
    public ResponseEntity<Book> updateInventory(@PathVariable Long id, @RequestParam Integer quantity) {
        log.info("PUT /api/books/{}/inventory - Updating inventory by quantity: {}", id, quantity);
        try {
            return ResponseEntity.ok(bookService.updateInventory(id, quantity));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:books')")
    public ResponseEntity<Long> countAllBooks() {
        log.info("GET /api/books/count - Counting all books");
        return ResponseEntity.ok(bookService.getTotalBooksCount());
    }

    @PostMapping("/{id}/purchase")
    // @PreAuthorize("hasAnyAuthority('SCOPE_write:books')")
    public void addToCart(@RequestBody LineItem purchaseItem, Principal jwtTokenPrincipal) {
        System.out.println("Principal is ... " + jwtTokenPrincipal);

        JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtTokenPrincipal;
        Jwt jwt = (Jwt)authToken.getPrincipal();
        String userId = jwt.getSubject();

        //String userid = ((JwtAuthenticationToken) jwtTokenPrincipal).getToken().getSubject();

        log.info("Purchasing book with id: {} and quantity: {} and for user ", purchaseItem.getBookId(), purchaseItem.getQuantity(), userId);

        BookDto bookDetailsDto = bookService.getBookById(purchaseItem.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + purchaseItem.getBookId()));

        LineItem bookToPurchase = LineItem.builder()
                .bookId(purchaseItem.getBookId())
                .price(bookDetailsDto.getPrice())
                .quantity(purchaseItem.getQuantity())
                .build();

        bookService.purchaseBook(userId, bookToPurchase);
    }

    @PostMapping("/checkout")
    public void checkoutCart(Principal jwtTokenPrincipal) {
        JwtAuthenticationToken authToken = (JwtAuthenticationToken) jwtTokenPrincipal;
        Jwt jwt = (Jwt)authToken.getPrincipal();
        String userId = jwt.getSubject();

        log.info("Jwt Claims {}" , jwt.getClaims());
        log.info("Jwt Headers {}" , jwt.getHeaders());

        log.info("PLacing order for {} " , userId);

        bookService.checkoutCartForUser(userId);
    }

}
