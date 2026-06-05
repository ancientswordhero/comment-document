package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.dto.PageResult;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock BookRepository bookRepository;
    @Mock CategoryRepository categoryRepository;
    @InjectMocks BookService bookService;

    @Test
    void shouldReturnPagedBooksForReader() {
        Book book = Book.builder().id(1L).title("红楼梦").author("曹雪芹")
            .isbn("978-7-02").categoryId(2L).status(1).build();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findWithFilters(isNull(), isNull(), eq(1), any(Pageable.class)))
            .thenReturn(page);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(
            Category.builder().id(2L).name("小说").build()));

        PageResult<BookResponse> result = bookService.getBooks(null, null, 1, 20);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("红楼梦");
        assertThat(result.getRecords().get(0).getCategoryName()).isEqualTo("小说");
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldReturnBookById() {
        Book book = Book.builder().id(1L).title("三体").author("刘慈欣")
            .isbn("978-7-53").categoryId(3L).status(1)
            .description("<p>科幻经典</p>").build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(
            Category.builder().id(3L).name("科技").build()));

        BookResponse result = bookService.getBookById(1L);

        assertThat(result.getTitle()).isEqualTo("三体");
        assertThat(result.getCategoryName()).isEqualTo("科技");
        assertThat(result.getDescription()).isEqualTo("<p>科幻经典</p>");
    }

    @Test
    void shouldCreateBook() {
        BookRequest req = new BookRequest();
        req.setTitle("新书"); req.setAuthor("作者"); req.setIsbn("978-0-00");
        req.setCategoryId(1L); req.setDescription("简介");
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BookResponse result = bookService.createBook(null, null, req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("新书");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldUpdateBook() {
        Book existing = Book.builder().id(1L).title("旧名").author("旧作者")
            .isbn("978-0").build();
        BookRequest req = new BookRequest();
        req.setTitle("新名"); req.setAuthor("新作者"); req.setIsbn("978-1");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse result = bookService.updateBook(1L, null, null, req);

        assertThat(result.getTitle()).isEqualTo("新名");
        assertThat(result.getAuthor()).isEqualTo("新作者");
    }

    @Test
    void shouldDeleteBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        bookService.deleteBook(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void shouldToggleStatus() {
        Book book = Book.builder().id(1L).status(1).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse result = bookService.toggleStatus(1L);

        assertThat(result.getStatus()).isEqualTo(0);
    }
}
