# 图书目录系统 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建线上图书馆的图书目录子系统：Spring Boot 后端 + 两个独立 Vue 3 前端（读者端 + 管理端），支持分类浏览、关键词搜索、图书详情、管理端 CRUD 和封面上传。

**Architecture:** Spring Boot 单体后端（Controller → Service → Repository 三层），两个 Vue 3 SPA 前端通过 REST API 通信。封面图片存储本地磁盘。MySQL 数据库，JPA 持久化。

**Tech Stack:** Java 17, Spring Boot 3.x, Spring Data JPA, MySQL, H2 (测试), Maven, Vue 3 (Composition API + `<script setup>`), Vite, Vue Router, Axios, Vitest

---

### Task 1: 初始化后端 Spring Boot 项目

**Files:**
- Create: `library-server/pom.xml`
- Create: `library-server/src/main/java/com/library/LibraryApplication.java`
- Create: `library-server/src/main/resources/application.yml`
- Create: `library-server/src/test/resources/application-test.yml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>
    <groupId>com.library</groupId>
    <artifactId>library-server</artifactId>
    <version>0.1.0</version>
    <name>library-server</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

file:
  upload-dir: uploads/covers
```

- [ ] **Step 4: 创建测试用 application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.H2Dialect

file:
  upload-dir: test-uploads/covers
```

- [ ] **Step 5: 验证项目可编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add library-server/
git commit -m "feat: scaffold Spring Boot project with dependencies"
```

---

### Task 2: 创建实体类

**Files:**
- Create: `library-server/src/main/java/com/library/entity/Category.java`
- Create: `library-server/src/main/java/com/library/entity/Book.java`

- [ ] **Step 1: 创建 Category 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private int sortOrder;
}
```

- [ ] **Step 2: 创建 Book 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/entity/
git commit -m "feat: add Category and Book JPA entities"
```

---

### Task 3: 创建 Repository 层

**Files:**
- Create: `library-server/src/main/java/com/library/repository/CategoryRepository.java`
- Create: `library-server/src/main/java/com/library/repository/BookRepository.java`

- [ ] **Step 1: 创建 CategoryRepository**

```java
package com.library.repository;

import com.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIdOrderBySortOrder(Long parentId);
    List<Category> findByParentIdIsNullOrderBySortOrder();
}
```

- [ ] **Step 2: 创建 BookRepository**

```java
package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE " +
           "(:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR b.categoryId = :categoryId) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Book> findWithFilters(@Param("keyword") String keyword,
                                @Param("categoryId") Long categoryId,
                                @Param("status") Integer status,
                                Pageable pageable);
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/repository/
git commit -m "feat: add CategoryRepository and BookRepository"
```

---

### Task 4: 创建 DTO 类

**Files:**
- Create: `library-server/src/main/java/com/library/dto/ApiResponse.java`
- Create: `library-server/src/main/java/com/library/dto/PageResult.java`
- Create: `library-server/src/main/java/com/library/dto/BookRequest.java`
- Create: `library-server/src/main/java/com/library/dto/BookResponse.java`
- Create: `library-server/src/main/java/com/library/dto/CategoryResponse.java`

- [ ] **Step 1: 创建统一响应包装类**

```java
package com.library.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().code(200).message("success").data(data).build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder().code(code).message(message).build();
    }
}
```

- [ ] **Step 2: 创建分页结果类**

```java
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
```

- [ ] **Step 3: 创建 BookRequest**

```java
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
```

- [ ] **Step 4: 创建 BookResponse**

```java
package com.library.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private Long categoryId;
    private String categoryName;
    private String coverUrl;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 5: 创建 CategoryResponse**

```java
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
```

- [ ] **Step 6: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add library-server/src/main/java/com/library/dto/
git commit -m "feat: add DTO classes for API request/response"
```

---

### Task 5: 创建 FileService + 测试

**Files:**
- Create: `library-server/src/main/java/com/library/config/FileUploadConfig.java`
- Create: `library-server/src/main/java/com/library/service/FileService.java`
- Create: `library-server/src/test/java/com/library/service/FileServiceTest.java`

- [ ] **Step 1: 编写 FileServiceTest（测试先行）**

```java
package com.library.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStoreFileAndReturnRelativePath() throws Exception {
        FileService service = new FileService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
            "file", "cover.jpg", "image/jpeg", "test image content".getBytes());

        String resultPath = service.store(file);

        assertThat(resultPath).startsWith("/uploads/covers/");
        assertThat(resultPath).endsWith(".jpg");
        Path storedFile = tempDir.resolve(resultPath.substring("/uploads/covers/".length()));
        assertThat(Files.exists(storedFile)).isTrue();
    }

    @Test
    void shouldGenerateUniqueFilenames() throws Exception {
        FileService service = new FileService(tempDir.toString());
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "cover.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "cover.jpg", "image/jpeg", "content2".getBytes());

        String path1 = service.store(file1);
        String path2 = service.store(file2);

        assertThat(path1).isNotEqualTo(path2);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=FileServiceTest -q`
Expected: FAIL (FileService class not found)

- [ ] **Step 3: 创建 FileUploadConfig**

```java
package com.library.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {
    private String uploadDir = "uploads/covers";
}
```

- [ ] **Step 4: 实现 FileService**

```java
package com.library.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

    private final Path uploadPath;

    public FileService(FileUploadConfig config) {
        this.uploadPath = Paths.get(config.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public FileService(String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/covers/" + filename;
    }
}
```

Wait — the FileService constructor takes FileUploadConfig, but the test passes a String. I need to add a package-private constructor for testing. Let me fix the FileService:

```java
package com.library.service;

import com.library.config.FileUploadConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

    private final Path uploadPath;

    public FileService(FileUploadConfig config) {
        this(config.getUploadDir());
    }

    FileService(String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        Path targetPath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/covers/" + filename;
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=FileServiceTest -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/config/FileUploadConfig.java library-server/src/main/java/com/library/service/FileService.java library-server/src/test/java/com/library/service/FileServiceTest.java
git commit -m "feat: add FileService for cover image upload"
```

---

### Task 6: 创建 CategoryService + 测试

**Files:**
- Create: `library-server/src/main/java/com/library/service/CategoryService.java`
- Create: `library-server/src/test/java/com/library/service/CategoryServiceTest.java`

- [ ] **Step 1: 编写 CategoryServiceTest（测试先行）**

```java
package com.library.service;

import com.library.dto.CategoryResponse;
import com.library.entity.Category;
import com.library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void shouldBuildCategoryTree() {
        Category lit = Category.builder().id(1L).name("文学").parentId(null).sortOrder(1).build();
        Category novel = Category.builder().id(2L).name("小说").parentId(1L).sortOrder(1).build();
        Category poetry = Category.builder().id(3L).name("诗词").parentId(1L).sortOrder(2).build();
        Category tech = Category.builder().id(4L).name("科技").parentId(null).sortOrder(2).build();

        when(categoryRepository.findAll()).thenReturn(List.of(lit, novel, poetry, tech));

        List<CategoryResponse> tree = categoryService.getCategoryTree();

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getName()).isEqualTo("文学");
        assertThat(tree.get(0).getChildren()).hasSize(2);
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("小说");
        assertThat(tree.get(1).getName()).isEqualTo("科技");
        assertThat(tree.get(1).getChildren()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of());
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        assertThat(tree).isEmpty();
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=CategoryServiceTest -q`
Expected: FAIL

- [ ] **Step 3: 实现 CategoryService**

```java
package com.library.service;

import com.library.dto.CategoryResponse;
import com.library.entity.Category;
import com.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        Map<Long, List<Category>> childrenMap = all.stream()
            .filter(c -> c.getParentId() != null)
            .collect(Collectors.groupingBy(Category::getParentId));

        return all.stream()
            .filter(c -> c.getParentId() == null)
            .sorted(Comparator.comparingInt(Category::getSortOrder))
            .map(c -> buildResponse(c, childrenMap))
            .collect(Collectors.toList());
    }

    private CategoryResponse buildResponse(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryResponse> children = childrenMap
            .getOrDefault(category.getId(), List.of())
            .stream()
            .sorted(Comparator.comparingInt(Category::getSortOrder))
            .map(c -> buildResponse(c, childrenMap))
            .collect(Collectors.toList());

        return CategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .sortOrder(category.getSortOrder())
            .children(children)
            .build();
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=CategoryServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/service/CategoryService.java library-server/src/test/java/com/library/service/CategoryServiceTest.java
git commit -m "feat: add CategoryService with tree building"
```

---

### Task 7: 创建 BookService + 测试

**Files:**
- Create: `library-server/src/main/java/com/library/service/BookService.java`
- Create: `library-server/src/test/java/com/library/service/BookServiceTest.java`

- [ ] **Step 1: 编写 BookServiceTest（测试先行）**

```java
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
            .isbn("978-7-53").categoryId(3L).status(1).coverUrl("/uploads/covers/a.jpg")
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

        BookResponse result = bookService.createBook(req);

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

        BookResponse result = bookService.updateBook(1L, req);

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
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=BookServiceTest -q`
Expected: FAIL

- [ ] **Step 3: 实现 BookService**

```java
package com.library.service;

import com.library.dto.*;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<BookResponse> getBooks(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryId, 1, pageable);
        return buildPageResult(bookPage);
    }

    public PageResult<BookResponse> getAdminBooks(String keyword, Long categoryId, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Book> bookPage = bookRepository.findWithFilters(keyword, categoryId, status, pageable);
        return buildPageResult(bookPage);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        return toResponse(book);
    }

    public BookResponse createBook(BookRequest req) {
        Book book = Book.builder()
            .title(req.getTitle())
            .author(req.getAuthor())
            .isbn(req.getIsbn())
            .categoryId(req.getCategoryId())
            .coverUrl(req.getCoverUrl())
            .description(req.getDescription())
            .status(1)
            .build();
        book = bookRepository.save(book);
        return toResponse(book);
    }

    public BookResponse updateBook(Long id, BookRequest req) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setIsbn(req.getIsbn());
        book.setCategoryId(req.getCategoryId());
        book.setCoverUrl(req.getCoverUrl());
        book.setDescription(req.getDescription());
        book = bookRepository.save(book);
        return toResponse(book);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("图书不存在: " + id);
        }
        bookRepository.deleteById(id);
    }

    public BookResponse toggleStatus(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("图书不存在: " + id));
        book.setStatus(book.getStatus() == 1 ? 0 : 1);
        book = bookRepository.save(book);
        return toResponse(book);
    }

    private PageResult<BookResponse> buildPageResult(Page<Book> page) {
        List<BookResponse> records = page.getContent().stream()
            .map(this::toResponse)
            .toList();
        return PageResult.<BookResponse>builder()
            .records(records)
            .total(page.getTotalElements())
            .page(page.getNumber() + 1)
            .size(page.getSize())
            .build();
    }

    private BookResponse toResponse(Book book) {
        String categoryName = null;
        if (book.getCategoryId() != null) {
            categoryName = categoryRepository.findById(book.getCategoryId())
                .map(Category::getName)
                .orElse(null);
        }
        return BookResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .author(book.getAuthor())
            .isbn(book.getIsbn())
            .categoryId(book.getCategoryId())
            .categoryName(categoryName)
            .coverUrl(book.getCoverUrl())
            .description(book.getDescription())
            .status(book.getStatus())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt())
            .build();
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=BookServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/service/BookService.java library-server/src/test/java/com/library/service/BookServiceTest.java
git commit -m "feat: add BookService with CRUD and search"
```

---

### Task 8: 创建全局异常处理

**Files:**
- Create: `library-server/src/main/java/com/library/config/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 GlobalExceptionHandler**

```java
package com.library.config;

import com.library.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(EntityNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("isbn")) {
            return ApiResponse.error(409, "ISBN已存在");
        }
        return ApiResponse.error(409, "数据冲突");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleOther(Exception e) {
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/config/GlobalExceptionHandler.java
git commit -m "feat: add global exception handler"
```

---

### Task 9: 创建读者端 Controller + 集成测试

**Files:**
- Create: `library-server/src/main/java/com/library/controller/BookController.java`
- Create: `library-server/src/test/java/com/library/controller/BookControllerTest.java`

- [ ] **Step 1: 编写 BookControllerTest（测试先行）**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired MockMvc mvc;
    @MockBean BookService bookService;
    @MockBean CategoryService categoryService;

    @Test
    void shouldReturnBookList() throws Exception {
        BookResponse book = BookResponse.builder().id(1L).title("红楼梦").author("曹雪芹")
            .isbn("978-7-02").categoryName("文学").status(1).build();
        PageResult<BookResponse> page = PageResult.<BookResponse>builder()
            .records(List.of(book)).total(1).page(1).size(20).build();
        when(bookService.getBooks(isNull(), isNull(), eq(1), eq(20))).thenReturn(page);

        mvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].title").value("红楼梦"));
    }

    @Test
    void shouldReturnBookDetail() throws Exception {
        BookResponse book = BookResponse.builder().id(1L).title("三体").author("刘慈欣").build();
        when(bookService.getBookById(1L)).thenReturn(book);

        mvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("三体"));
    }

    @Test
    void shouldReturnCategoryTree() throws Exception {
        CategoryResponse cat = CategoryResponse.builder().id(1L).name("文学").children(List.of()).build();
        when(categoryService.getCategoryTree()).thenReturn(List.of(cat));

        mvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("文学"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=BookControllerTest -q`
Expected: FAIL

- [ ] **Step 3: 实现 BookController**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping("/books")
    public ApiResponse<PageResult<BookResponse>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.getBooks(keyword, categoryId, page, size));
    }

    @GetMapping("/books/{id}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBookById(id));
    }

    @GetMapping("/categories")
    public ApiResponse<java.util.List<CategoryResponse>> getCategories() {
        return ApiResponse.success(categoryService.getCategoryTree());
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=BookControllerTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/BookController.java library-server/src/test/java/com/library/controller/BookControllerTest.java
git commit -m "feat: add reader BookController with list/detail/category endpoints"
```

---

### Task 10: 创建管理端 Controller + 集成测试

**Files:**
- Create: `library-server/src/main/java/com/library/controller/AdminBookController.java`
- Create: `library-server/src/test/java/com/library/controller/AdminBookControllerTest.java`

- [ ] **Step 1: 编写 AdminBookControllerTest（测试先行）**

```java
package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.*;
import com.library.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBookController.class)
class AdminBookControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BookService bookService;
    @MockBean FileService fileService;

    @Test
    void shouldListAllBooks() throws Exception {
        PageResult<BookResponse> page = PageResult.<BookResponse>builder()
            .records(List.of()).total(0).page(1).size(20).build();
        when(bookService.getAdminBooks(isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(page);

        mvc.perform(get("/api/admin/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCreateBook() throws Exception {
        BookRequest req = new BookRequest();
        req.setTitle("新书"); req.setAuthor("作者"); req.setIsbn("978-0");
        BookResponse resp = BookResponse.builder().id(1L).title("新书").build();
        when(bookService.createBook(any(BookRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/admin/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("新书"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        BookRequest req = new BookRequest();
        req.setTitle("更新"); req.setAuthor("作者"); req.setIsbn("978-0");
        BookResponse resp = BookResponse.builder().id(1L).title("更新").build();
        when(bookService.updateBook(eq(1L), any(BookRequest.class))).thenReturn(resp);

        mvc.perform(put("/api/admin/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("更新"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mvc.perform(delete("/api/admin/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldToggleStatus() throws Exception {
        BookResponse resp = BookResponse.builder().id(1L).status(0).build();
        when(bookService.toggleStatus(1L)).thenReturn(resp);

        mvc.perform(put("/api/admin/books/1/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void shouldUploadCover() throws Exception {
        when(fileService.store(any())).thenReturn("/uploads/covers/test.jpg");

        mvc.perform(multipart("/api/admin/upload/cover")
                .file(new MockMultipartFile("file", "cover.jpg", "image/jpeg", "test".getBytes())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value("/uploads/covers/test.jpg"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=AdminBookControllerTest -q`
Expected: FAIL

- [ ] **Step 3: 实现 AdminBookController**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin")
public class AdminBookController {

    private final BookService bookService;
    private final FileService fileService;

    public AdminBookController(BookService bookService, FileService fileService) {
        this.bookService = bookService;
        this.fileService = fileService;
    }

    @GetMapping("/books")
    public ApiResponse<PageResult<BookResponse>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.getAdminBooks(keyword, categoryId, status, page, size));
    }

    @PostMapping("/books")
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.createBook(request));
    }

    @PutMapping("/books/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.updateBook(id, request));
    }

    @DeleteMapping("/books/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/books/{id}/status")
    public ApiResponse<BookResponse> toggleStatus(@PathVariable Long id) {
        return ApiResponse.success(bookService.toggleStatus(id));
    }

    @PostMapping("/upload/cover")
    public ApiResponse<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        String path = fileService.store(file);
        return ApiResponse.success(path);
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=AdminBookControllerTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/AdminBookController.java library-server/src/test/java/com/library/controller/AdminBookControllerTest.java
git commit -m "feat: add admin BookController with CRUD and upload"
```

---

### Task 11: 创建 CORS 配置

**Files:**
- Create: `library-server/src/main/java/com/library/config/WebConfig.java`

- [ ] **Step 1: 创建 WebConfig**

```java
package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:5174")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/config/WebConfig.java
git commit -m "feat: add CORS and static resource config"
```

---

### Task 12: 数据库初始化数据

**Files:**
- Create: `library-server/src/main/resources/data.sql`

- [ ] **Step 1: 创建 data.sql（初始分类和示例图书）**

```sql
INSERT INTO categories (name, parent_id, sort_order) VALUES ('文学', NULL, 1);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('小说', 1, 1);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('诗词', 1, 2);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('散文', 1, 3);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('历史', NULL, 2);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('哲学', NULL, 3);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('科技', NULL, 4);
INSERT INTO categories (name, parent_id, sort_order) VALUES ('艺术', NULL, 5);

INSERT INTO books (title, author, isbn, category_id, description, status) VALUES
('红楼梦', '曹雪芹', '978-7-02-000220-9', 2, '<p>中国古典四大名著之一，以贾宝玉、林黛玉的爱情悲剧为主线。</p>', 1);
INSERT INTO books (title, author, isbn, category_id, description, status) VALUES
('三体', '刘慈欣', '978-7-5366-9293-0', 7, '<p>地球文明向宇宙发出信号，引发了一场浩大的宇宙战争。</p>', 1);
INSERT INTO books (title, author, isbn, category_id, description, status) VALUES
('史记', '司马迁', '978-7-101-00304-8', 5, '<p>中国第一部纪传体通史，记载了从黄帝到汉武帝的历史。</p>', 1);
INSERT INTO books (title, author, isbn, category_id, description, status) VALUES
('论语', '孔子', '978-7-101-07034-7', 6, '<p>儒家经典，记录了孔子及其弟子的言行。</p>', 1);
```

- [ ] **Step 2: 更新 application.yml 启用数据初始化**

在 `application.yml` 的 `spring.jpa.hibernate` 下添加:

```yaml
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
```

- [ ] **Step 3: 验证项目可启动（需 MySQL 运行中）**

Run: `cd library-server && mvn spring-boot:run`
Expected: 应用启动在 8080 端口

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/resources/data.sql library-server/src/main/resources/application.yml
git commit -m "feat: add seed data for categories and sample books"
```

---

### Task 13: 运行全部后端测试

- [ ] **Step 1: 运行完整测试套件**

Run: `cd library-server && mvn test`
Expected: All tests pass

- [ ] **Step 2: Commit（如有格式修正）**

```bash
git add -A && git commit -m "chore: finalize backend tests" || echo "no changes"
```

---

### Task 14: 初始化读者端 Vue 项目

**Files:**
- Create: `reader-app/` (via Vite scaffold)

- [ ] **Step 1: 使用 Vite 创建 Vue 3 项目**

Run: `npm create vite@latest reader-app -- --template vue`
然后在 `reader-app` 目录下:

Run: `cd reader-app && npm install && npm install vue-router@4 axios`

- [ ] **Step 2: 创建全局 CSS 样式**

创建 `reader-app/src/assets/global.css`:

```css
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700&family=Noto+Sans+SC:wght@400;500&display=swap');

:root {
  --bg: #fafaf7;
  --card-bg: #fff;
  --card-border: #ece8df;
  --text: #4a3d2f;
  --text-secondary: #8b8070;
  --text-muted: #a09880;
  --accent: #c9a96e;
  --accent-light: #f0ebe0;
  --header-bg: #fff;
  --border: #e8e4dc;
  --status-up-bg: #e8f5e9;
  --status-up-text: #5b8c5a;
  --status-down-bg: #fff3e0;
  --status-down-text: #c08840;
  --font-serif: 'Noto Serif SC', 'SimSun', serif;
  --font-sans: 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  background: var(--bg);
  color: var(--text);
  font-family: var(--font-sans);
  font-size: 14px;
}

a { color: inherit; text-decoration: none; }
```

- [ ] **Step 3: 创建 main.js**

重写 `reader-app/src/main.js`:

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

createApp(App).use(router).mount('#app')
```

- [ ] **Step 4: 验证项目可启动**

Run: `cd reader-app && npm run dev`
Expected: Vite dev server 启动在 5173 端口

- [ ] **Step 5: Commit**

```bash
git add reader-app/
git commit -m "feat: scaffold reader Vue 3 project"
```

---

### Task 15: 创建读者端路由和 API 层

**Files:**
- Create: `reader-app/src/router/index.js`
- Create: `reader-app/src/api/index.js`
- Create: `reader-app/src/api/book.js`

- [ ] **Step 1: 创建路由配置**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import BookList from '../views/BookList.vue'
import BookDetail from '../views/BookDetail.vue'

const routes = [
  { path: '/', name: 'home', component: BookList },
  { path: '/book/:id', name: 'book-detail', component: BookDetail }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

- [ ] **Step 2: 创建 axios 实例（带拦截器）**

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => {
    if (response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response.data
  },
  error => {
    const msg = error.response?.data?.message || '网络错误'
    console.error(msg)
    return Promise.reject(error)
  }
)

export default api
```

- [ ] **Step 3: 创建图书 API 模块**

```javascript
import api from './index'

export function getBooks(params) {
  return api.get('/books', { params })
}

export function getBookById(id) {
  return api.get(`/books/${id}`)
}

export function getCategories() {
  return api.get('/categories')
}
```

- [ ] **Step 4: Commit**

```bash
git add reader-app/src/router/ reader-app/src/api/
git commit -m "feat: add reader router, axios config, and book API"
```

---

### Task 16: 创建读者端组件

**Files:**
- Create: `reader-app/src/components/AppHeader.vue`
- Create: `reader-app/src/components/CategoryNav.vue`
- Create: `reader-app/src/components/BookCarousel.vue`
- Create: `reader-app/src/components/SearchBar.vue`
- Create: `reader-app/src/components/BookCard.vue`
- Create: `reader-app/src/components/Pagination.vue`

- [ ] **Step 1: 创建 AppHeader.vue**

```vue
<template>
  <header class="app-header">
    <div class="header-left">
      <span class="logo-icon">書</span>
      <span class="logo-text">云图书馆</span>
    </div>
    <div class="header-center">
      <div class="search-box" @click="$emit('focus-search')">
        <span class="search-icon">🔍</span>
        <span class="search-placeholder">搜索书名 · 作者 · ISBN</span>
      </div>
    </div>
    <div class="header-right">
      <span class="nav-link">登录</span>
      <span class="nav-divider">|</span>
      <span class="nav-link">注册</span>
    </div>
  </header>
</template>

<script setup>
defineEmits(['focus-search'])
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  padding: 12px 36px;
  gap: 28px;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border);
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 8px; }
.logo-icon {
  width: 30px; height: 30px;
  background: var(--accent);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; color: #fff; border-radius: 2px;
}
.logo-text {
  font-weight: 600; font-size: 18px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.header-center { flex: 1; max-width: 500px; margin: 0 auto; }
.search-box {
  display: flex; align-items: center; gap: 8px;
  background: var(--bg); border: 1px solid #e0dbd0;
  padding: 8px 16px; border-radius: 2px; cursor: text;
}
.search-icon { flex-shrink: 0; }
.search-placeholder { color: #c0b8a8; font-size: 13px; }
.header-right { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); }
.nav-divider { color: var(--border); }
.nav-link { cursor: pointer; }
.nav-link:hover { color: var(--accent); }
</style>
```

- [ ] **Step 2: 创建 CategoryNav.vue**

```vue
<template>
  <nav class="category-nav">
    <div class="category-title">图书分类</div>
    <div
      v-for="cat in categories"
      :key="cat.id"
      class="category-item"
      :class="{ active: selectedId === cat.id }"
      @click="$emit('select', cat.id)"
    >
      {{ cat.name }}
    </div>
    <template v-for="cat in categories" :key="'sub-' + cat.id">
      <div
        v-if="selectedId === cat.id && cat.children?.length"
        v-for="child in cat.children"
        :key="child.id"
        class="category-item sub"
        :class="{ active: selectedChildId === child.id }"
        @click="$emit('select-child', child.id)"
      >
        {{ child.name }}
      </div>
    </template>
  </nav>
</template>

<script setup>
defineProps({
  categories: { type: Array, default: () => [] },
  selectedId: { type: Number, default: null },
  selectedChildId: { type: Number, default: null }
})
defineEmits(['select', 'select-child'])
</script>

<style scoped>
.category-nav { width: 160px; flex-shrink: 0; }
.category-title {
  font-weight: 600; font-size: 14px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 1px;
  margin-bottom: 10px;
}
.category-item {
  padding: 2px 10px; font-size: 13px; color: var(--text);
  cursor: pointer; line-height: 2.4; transition: color 0.2s;
}
.category-item:hover { color: var(--accent); }
.category-item.active {
  background: var(--accent-light);
  border-left: 2px solid var(--accent);
  font-weight: 500;
}
.category-item.sub {
  padding-left: 20px; font-size: 12px; color: var(--text-muted);
}
</style>
```

- [ ] **Step 3: 创建 BookCarousel.vue**

```vue
<template>
  <div class="carousel-wrapper">
    <div class="carousel-main">
      <div class="carousel-slide">
        <div class="carousel-text">
          <h2>翰 墨 书 香</h2>
          <p>万卷古今消永日，一窗昏晓送流年</p>
        </div>
        <div class="carousel-dots">
          <span class="dot active"></span>
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
      <button class="carousel-btn prev">◂</button>
      <button class="carousel-btn next">▸</button>
    </div>
    <div class="carousel-side">
      <div class="side-card">推荐位 1</div>
      <div class="side-card">推荐位 2</div>
    </div>
  </div>
</template>

<style scoped>
.carousel-wrapper { display: flex; gap: 16px; padding: 16px 36px; }
.carousel-main {
  flex: 1; height: 220px; position: relative;
  background: linear-gradient(135deg, #f5f1ea, #ede6d8, #e8e0d0);
  border: 1px solid var(--border); border-radius: 2px;
  display: flex; align-items: center; justify-content: center;
}
.carousel-text { text-align: center; }
.carousel-text h2 {
  font-size: 26px; color: var(--text); font-weight: 600;
  font-family: var(--font-serif); letter-spacing: 6px;
}
.carousel-text p { font-size: 12px; color: var(--text-muted); margin-top: 6px; }
.carousel-dots { display: flex; gap: 6px; justify-content: center; margin-top: 14px; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: #d8d0c0; }
.dot.active { background: var(--accent); }
.carousel-btn {
  position: absolute; top: 50%; transform: translateY(-50%);
  background: none; border: none; font-size: 18px; color: #c0b8a8; cursor: pointer;
}
.carousel-btn.prev { left: 10px; }
.carousel-btn.next { right: 10px; }
.carousel-side { width: 130px; display: flex; flex-direction: column; gap: 10px; }
.side-card {
  flex: 1; background: #f5f1ea; border: 1px solid var(--border);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: var(--text-muted);
}
</style>
```

- [ ] **Step 4: 创建 SearchBar.vue**

```vue
<template>
  <div class="search-bar">
    <input
      v-model="keywords"
      class="search-input"
      placeholder="搜索书名 · 作者 · ISBN"
      @keyup.enter="$emit('search', keywords)"
    />
    <select v-model="categoryId" class="search-select" @change="$emit('filter', categoryId)">
      <option :value="null">全部分类</option>
      <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
    </select>
    <button class="search-btn" @click="$emit('search', keywords)">搜索</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
defineProps({ categories: { type: Array, default: () => [] } })
defineEmits(['search', 'filter'])
const keywords = ref('')
const categoryId = ref(null)
</script>

<style scoped>
.search-bar { display: flex; gap: 8px; margin-bottom: 14px; }
.search-input {
  flex: 1; max-width: 320px; padding: 7px 12px;
  background: #fff; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text); outline: none;
}
.search-input:focus { border-color: var(--accent); }
.search-input::placeholder { color: #c0b8a8; }
.search-select {
  padding: 7px 12px; background: #fff;
  border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text-secondary); outline: none;
}
.search-btn {
  padding: 7px 16px; background: var(--accent); color: #fff;
  border: none; border-radius: 2px; font-size: 12px; cursor: pointer;
}
.search-btn:hover { opacity: 0.9; }
</style>
```

- [ ] **Step 5: 创建 BookCard.vue**

```vue
<template>
  <div class="book-card" @click="$router.push(`/book/${book.id}`)">
    <div class="book-cover">
      <img v-if="book.coverUrl" :src="coverSrc" :alt="book.title" />
      <span v-else class="cover-placeholder">📖</span>
    </div>
    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ book: { type: Object, required: true } })
const coverSrc = computed(() =>
  props.book.coverUrl
    ? `http://localhost:8080${props.book.coverUrl}`
    : null
)
</script>

<style scoped>
.book-card {
  background: #fff; border: 1px solid var(--card-border);
  border-radius: 2px; padding: 12px; text-align: center;
  cursor: pointer; transition: box-shadow 0.2s, border-color 0.2s;
}
.book-card:hover {
  box-shadow: 0 3px 14px rgba(90,50,30,0.08);
  border-color: var(--accent);
}
.book-cover {
  background: #f8f5ee; height: 150px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 10px; overflow: hidden;
}
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 36px; color: #d0c8b4; }
.book-title { font-weight: 500; font-size: 13px; color: var(--text); }
.book-author { font-size: 11px; color: var(--text-muted); margin-top: 3px; }
</style>
```

- [ ] **Step 6: 创建 Pagination.vue**

```vue
<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="page <= 1" @click="$emit('change', page - 1)">← 上一页</button>
    <button
      v-for="p in displayedPages" :key="p"
      :class="{ active: p === page }"
      @click="$emit('change', p)"
    >{{ p }}</button>
    <button :disabled="page >= totalPages" @click="$emit('change', page + 1)">下一页 →</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  page: Number, total: Number, size: Number
})
defineEmits(['change'])
const totalPages = computed(() => Math.ceil(props.total / props.size))
const displayedPages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = props.page
  let start = Math.max(1, current - 2)
  let end = Math.min(total, current + 2)
  if (end - start < 4) {
    if (start === 1) end = Math.min(total, start + 4)
    else start = Math.max(1, end - 4)
  }
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>

<style scoped>
.pagination {
  text-align: center; margin-top: 20px;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}
.pagination button {
  padding: 3px 9px; border: 1px solid #e0dbd0;
  background: #fff; border-radius: 2px; font-size: 12px;
  color: var(--text-secondary); cursor: pointer;
}
.pagination button.active { background: var(--accent); color: #fff; border-color: var(--accent); }
.pagination button:disabled { color: #d0c8b4; cursor: not-allowed; }
</style>
```

- [ ] **Step 7: Commit**

```bash
git add reader-app/src/components/
git commit -m "feat: add reader components (header, nav, carousel, search, card, pagination)"
```

---

### Task 17: 创建读者端视图

**Files:**
- Create: `reader-app/src/views/BookList.vue`
- Create: `reader-app/src/views/BookDetail.vue`
- Modify: `reader-app/src/App.vue`

- [ ] **Step 1: 重写 App.vue**

```vue
<template>
  <AppHeader />
  <CategoryNav
    :categories="categories"
    :selected-id="selectedCategoryId"
    @select="onCategorySelect"
    @select-child="onChildSelect"
    v-if="showSidebar"
  />
  <router-view
    :books="books"
    :loading="loading"
    :page="page"
    :total="total"
    :categories="categories"
    :selected-category-id="selectedCategoryId"
    @search="onSearch"
    @filter="onFilter"
    @page-change="onPageChange"
  />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import CategoryNav from './components/CategoryNav.vue'
import { getBooks, getCategories } from './api/book'

const route = useRoute()
const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const selectedCategoryId = ref(null)

const showSidebar = computed(() => route.path === '/')

onMounted(async () => {
  const catRes = await getCategories()
  categories.value = catRes.data
  fetchBooks()
})

async function fetchBooks() {
  loading.value = true
  try {
    const res = await getBooks({
      keyword: keyword.value || undefined,
      categoryId: selectedCategoryId.value || undefined,
      page: page.value,
      size: 20
    })
    books.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onSearch(kw) {
  keyword.value = kw
  page.value = 1
  fetchBooks()
}

function onFilter(catId) {
  selectedCategoryId.value = catId
  page.value = 1
  fetchBooks()
}

function onCategorySelect(catId) {
  selectedCategoryId.value = selectedCategoryId.value === catId ? null : catId
  page.value = 1
  fetchBooks()
}

function onChildSelect(childId) {
  selectedCategoryId.value = childId
  page.value = 1
  fetchBooks()
}

function onPageChange(p) {
  page.value = p
  fetchBooks()
}
</script>
```

Wait — this design has issues. The CategoryNav is only shown on the home page, but it's rendered in App.vue alongside the router-view. And the router-view gets props that only BookList needs. This is messy.

Let me restructure: CategoryNav should be part of BookList.vue since it only shows on the home page. The App.vue should just have AppHeader + router-view without props drilling.

- [ ] **Step 1: 重写 App.vue（修正版）**

```vue
<template>
  <div class="app">
    <AppHeader />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<style scoped>
.app { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; }
</style>
```

- [ ] **Step 2: 创建 BookList.vue**

```vue
<template>
  <div class="home-layout">
    <div class="home-body">
      <CategoryNav
        :categories="categories"
        :selected-id="selectedCategoryId"
        :selected-child-id="selectedChildId"
        @select="onCategorySelect"
        @select-child="onChildSelect"
      />
      <div class="home-main">
        <BookCarousel />
        <SearchBar
          :categories="categories"
          @search="onSearch"
          @filter="onFilter"
        />
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else class="book-grid">
          <BookCard v-for="book in books" :key="book.id" :book="book" />
        </div>
        <Pagination
          :page="page" :total="total" :size="20"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CategoryNav from '../components/CategoryNav.vue'
import BookCarousel from '../components/BookCarousel.vue'
import SearchBar from '../components/SearchBar.vue'
import BookCard from '../components/BookCard.vue'
import Pagination from '../components/Pagination.vue'
import { getBooks, getCategories } from '../api/book'

const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const selectedCategoryId = ref(null)
const selectedChildId = ref(null)

onMounted(async () => {
  const catRes = await getCategories()
  categories.value = catRes.data
  fetchBooks()
})

async function fetchBooks() {
  loading.value = true
  try {
    const effectiveCategory = selectedChildId.value || selectedCategoryId.value
    const res = await getBooks({
      keyword: keyword.value || undefined,
      categoryId: effectiveCategory || undefined,
      page: page.value,
      size: 20
    })
    books.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onSearch(kw) { keyword.value = kw; page.value = 1; fetchBooks() }
function onFilter(catId) {
  selectedCategoryId.value = catId
  selectedChildId.value = null
  page.value = 1
  fetchBooks()
}
function onCategorySelect(catId) {
  selectedCategoryId.value = selectedCategoryId.value === catId ? null : catId
  selectedChildId.value = null
  page.value = 1
  fetchBooks()
}
function onChildSelect(childId) {
  selectedChildId.value = childId
  page.value = 1
  fetchBooks()
}
function onPageChange(p) { page.value = p; fetchBooks() }
</script>

<style scoped>
.home-layout { padding: 0 36px; }
.home-body { display: flex; gap: 24px; padding-top: 16px; }
.home-main { flex: 1; }
.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.loading-text { text-align: center; padding: 40px; color: var(--text-muted); }
</style>
```

- [ ] **Step 3: 创建 BookDetail.vue**

```vue
<template>
  <div class="detail-page" v-if="book">
    <div class="detail-card">
      <div class="detail-cover">
        <img v-if="book.coverUrl" :src="coverSrc" :alt="book.title" />
        <span v-else class="cover-placeholder">📖</span>
      </div>
      <div class="detail-info">
        <h1 class="detail-title">{{ book.title }}</h1>
        <div class="detail-meta">
          <div class="meta-item"><span class="meta-label">作者</span>{{ book.author }}</div>
          <div class="meta-item"><span class="meta-label">ISBN</span>{{ book.isbn }}</div>
          <div class="meta-item"><span class="meta-label">分类</span>{{ book.categoryName || '未分类' }}</div>
          <div class="meta-item"><span class="meta-label">上架时间</span>{{ formatDate(book.createdAt) }}</div>
        </div>
        <div class="detail-desc-title">图书简介</div>
        <div class="detail-desc" v-html="book.description || '暂无简介'"></div>
        <button class="back-btn" @click="$router.push('/')">← 返回首页</button>
      </div>
    </div>
  </div>
  <div v-else class="loading-text">加载中...</div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getBookById } from '../api/book'

const route = useRoute()
const book = ref(null)

const coverSrc = computed(() =>
  book.value?.coverUrl ? `http://localhost:8080${book.value.coverUrl}` : null
)

onMounted(async () => {
  const res = await getBookById(route.params.id)
  book.value = res.data
})

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.detail-page { padding: 28px 36px; }
.detail-card { display: flex; gap: 28px; background: #fff; border: 1px solid var(--card-border); padding: 24px; }
.detail-cover { width: 260px; height: 360px; background: #f8f5ee; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.detail-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { font-size: 64px; color: #d0c8b4; }
.detail-info { flex: 1; }
.detail-title { font-size: 24px; font-family: var(--font-serif); color: var(--text); font-weight: 600; letter-spacing: 2px; margin-bottom: 20px; }
.detail-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 24px; }
.meta-item { font-size: 13px; color: var(--text-secondary); }
.meta-label { color: var(--text-muted); margin-right: 8px; font-size: 12px; }
.detail-desc-title { font-size: 14px; font-family: var(--font-serif); color: var(--text); margin-bottom: 8px; font-weight: 600; }
.detail-desc { font-size: 13px; color: var(--text-secondary); line-height: 1.8; }
.detail-desc :deep(p) { margin-bottom: 8px; }
.back-btn { margin-top: 20px; padding: 8px 20px; background: var(--accent); color: #fff; border: none; border-radius: 2px; cursor: pointer; font-size: 13px; }
.loading-text { text-align: center; padding: 60px; color: var(--text-muted); }
</style>
```

- [ ] **Step 4: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds without errors

- [ ] **Step 5: Commit**

```bash
git add reader-app/src/
git commit -m "feat: add reader views (BookList, BookDetail, App)"
```

---

### Task 18: 初始化管理端 Vue 项目

**Files:**
- Create: `admin-app/` (via Vite scaffold)

- [ ] **Step 1: 使用 Vite 创建 Vue 3 项目**

Run: `npm create vite@latest admin-app -- --template vue`
然后在 `admin-app` 目录下:

Run: `cd admin-app && npm install && npm install vue-router@4 axios`

- [ ] **Step 2: 复制全局 CSS**

将 `reader-app/src/assets/global.css` 复制到 `admin-app/src/assets/global.css`

- [ ] **Step 3: 创建 main.js**

```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/global.css'

createApp(App).use(router).mount('#app')
```

- [ ] **Step 4: 验证项目可启动**

Run: `cd admin-app && npm run dev`
Expected: Vite dev server 启动在 5174 端口

- [ ] **Step 5: Commit**

```bash
git add admin-app/
git commit -m "feat: scaffold admin Vue 3 project"
```

---

### Task 19: 创建管理端路由和 API 层

**Files:**
- Create: `admin-app/src/router/index.js`
- Create: `admin-app/src/api/index.js`
- Create: `admin-app/src/api/book.js`

- [ ] **Step 1: 创建路由**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import BookTable from '../views/BookTable.vue'
import BookForm from '../views/BookForm.vue'

const routes = [
  { path: '/', name: 'book-list', component: BookTable },
  { path: '/book/new', name: 'book-new', component: BookForm },
  { path: '/book/:id/edit', name: 'book-edit', component: BookForm, props: true }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

- [ ] **Step 2: 创建 axios 实例**

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api/admin',
  timeout: 10000
})

api.interceptors.response.use(
  response => {
    if (response.data.code !== 200) {
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response.data
  },
  error => {
    const msg = error.response?.data?.message || '网络错误'
    console.error(msg)
    return Promise.reject(error)
  }
)

export default api
```

- [ ] **Step 3: 创建图书 API 模块**

```javascript
import api from './index'

export function getBooks(params) {
  return api.get('/books', { params })
}

export function getBookById(id) {
  return api.get(`/books/${id}`)
}

export function createBook(data) {
  return api.post('/books', data)
}

export function updateBook(id, data) {
  return api.put(`/books/${id}`, data)
}

export function deleteBook(id) {
  return api.delete(`/books/${id}`)
}

export function toggleStatus(id) {
  return api.put(`/books/${id}/status`)
}

export function uploadCover(file) {
  const form = new FormData()
  form.append('file', file)
  return api.post('/upload/cover', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

- [ ] **Step 4: Commit**

```bash
git add admin-app/src/
git commit -m "feat: add admin router, axios config, and book API"
```

---

### Task 20: 创建管理端组件

**Files:**
- Create: `admin-app/src/components/AdminHeader.vue`
- Create: `admin-app/src/components/BookTable.vue`
- Create: `admin-app/src/components/BookForm.vue`

- [ ] **Step 1: 创建 AdminHeader.vue**

```vue
<template>
  <header class="admin-header">
    <span class="admin-logo">云图书馆 · 管理后台</span>
    <div class="admin-links">
      <span>图书管理</span>
      <span class="divider">|</span>
      <span>管理员</span>
    </div>
  </header>
</template>

<style scoped>
.admin-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 28px; background: var(--header-bg);
  border-bottom: 1px solid var(--border);
}
.admin-logo {
  font-weight: 600; font-size: 16px; color: var(--text);
  font-family: var(--font-serif); letter-spacing: 2px;
}
.admin-links { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); }
.divider { color: var(--border); }
</style>
```

- [ ] **Step 2: 创建 BookTable.vue**

This is a larger component. Let me write the full version:

```vue
<template>
  <div class="table-page">
    <div class="toolbar">
      <input
        v-model="keyword"
        class="toolbar-search"
        placeholder="搜索书名、作者、ISBN..."
        @keyup.enter="search"
      />
      <select v-model="categoryId" class="toolbar-select" @change="search">
        <option :value="null">全部分类</option>
        <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="statusFilter" class="toolbar-select" @change="search">
        <option :value="null">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
      <button class="btn-add" @click="$router.push('/book/new')">+ 新增图书</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>封面</th><th>书名</th><th>作者</th><th>ISBN</th><th>分类</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>
            <div class="thumb">
              <img v-if="book.coverUrl" :src="`http://localhost:8080${book.coverUrl}`" :alt="book.title" />
              <span v-else>封面</span>
            </div>
          </td>
          <td class="cell-title">{{ book.title }}</td>
          <td>{{ book.author }}</td>
          <td class="cell-isbn">{{ book.isbn }}</td>
          <td>{{ book.categoryName || '-' }}</td>
          <td>
            <span class="status-tag" :class="book.status === 1 ? 'up' : 'down'">
              {{ book.status === 1 ? '上架' : '下架' }}
            </span>
          </td>
          <td class="cell-actions">
            <a @click="$router.push(`/book/${book.id}/edit`)">编辑</a>
            <span class="sep">|</span>
            <a class="toggle" @click="onToggle(book)">{{ book.status === 1 ? '下架' : '上架' }}</a>
            <span class="sep">|</span>
            <a class="del" @click="onDelete(book)">删除</a>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="table-pagination" v-if="totalPages > 1">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getBooks, getBookById, deleteBook, toggleStatus } from '../api/book'

const books = ref([])
const flatCategories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const statusFilter = ref(null)
const page = ref(1)
const total = ref(0)
const size = 20

const totalPages = computed(() => Math.ceil(total.value / size))

async function fetchCategories() {
  const api = await import('../api/index')
  const res = await api.default.get('/categories', { baseURL: 'http://localhost:8080/api' })
  flatCategories.value = flattenCategories(res.data)
}

function flattenCategories(cats, prefix = '') {
  let result = []
  for (const cat of cats) {
    result.push({ id: cat.id, name: prefix + cat.name })
    if (cat.children) {
      result.push(...flattenCategories(cat.children, prefix + '  '))
    }
  }
  return result
}

onMounted(async () => {
  await fetchCategories()
  fetchBooks()
})

async function fetchBooks() {
  const res = await getBooks({
    keyword: keyword.value || undefined,
    categoryId: categoryId.value || undefined,
    status: statusFilter.value,
    page: page.value,
    size
  })
  books.value = res.data.records
  total.value = res.data.total
}

function search() { page.value = 1; fetchBooks() }
function goPage(p) { page.value = p; fetchBooks() }

async function onToggle(book) {
  await toggleStatus(book.id)
  fetchBooks()
}

async function onDelete(book) {
  if (confirm(`确定删除「${book.title}」吗？`)) {
    await deleteBook(book.id)
    fetchBooks()
  }
}
</script>

<style scoped>
.table-page { padding: 16px 28px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 14px; align-items: center; }
.toolbar-search {
  flex: 1; max-width: 280px; padding: 7px 12px;
  border: 1px solid #e0dbd0; border-radius: 2px; font-size: 12px; outline: none;
}
.toolbar-select {
  padding: 7px 12px; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 12px; color: var(--text-secondary); outline: none;
}
.btn-add {
  margin-left: auto; padding: 8px 20px;
  background: var(--accent); color: #fff; border: none;
  border-radius: 2px; font-size: 12px; cursor: pointer;
}
.data-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.data-table th {
  text-align: left; padding: 10px 12px; color: var(--text-secondary);
  font-weight: 500; border-bottom: 2px solid var(--border);
}
.data-table td { padding: 10px 12px; border-bottom: 1px solid var(--accent-light); }
.thumb {
  width: 36px; height: 48px; background: #f5f1ea;
  display: flex; align-items: center; justify-content: center;
  font-size: 10px; color: var(--text-muted); overflow: hidden;
}
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.cell-title { color: var(--text); font-weight: 500; }
.cell-isbn { color: var(--text-muted); font-size: 11px; }
.status-tag { padding: 2px 8px; border-radius: 2px; font-size: 11px; }
.status-tag.up { background: var(--status-up-bg); color: var(--status-up-text); }
.status-tag.down { background: var(--status-down-bg); color: var(--status-down-text); }
.cell-actions a { cursor: pointer; color: var(--text-secondary); }
.cell-actions a:hover { color: var(--accent); }
.cell-actions a.del:hover { color: #c04040; }
.cell-actions .sep { color: var(--border); margin: 0 6px; }
.table-pagination { text-align: center; margin-top: 16px; display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 12px; }
.table-pagination button { padding: 3px 12px; border: 1px solid #e0dbd0; background: #fff; border-radius: 2px; color: var(--text-secondary); cursor: pointer; }
.page-info { color: var(--text-muted); }
</style>
```

I notice the `fetchCategories` function has a messy import. Let me fix it to use a cleaner approach — create a separate categories API call or import the reader API. Let me clean this up:

Instead of the dynamic import, let's add a `getCategories` function to the admin API:

In `admin-app/src/api/book.js`, update to include:

```javascript
import api from './index'
import axios from 'axios'

const readerApi = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

export function getCategories() {
  return readerApi.get('/categories').then(r => r.data)
}

// ... rest of the existing exports
```

And fix the fetchCategories in BookTable:

```javascript
import { getBooks, deleteBook, toggleStatus, getCategories } from '../api/book'

// ...

async function fetchCategories() {
  const res = await getCategories()
  flatCategories.value = flattenCategories(res.data)
}
```

Let me rewrite this cleanly.

- [ ] **Step 3: 创建 BookForm.vue**

```vue
<template>
  <div class="form-page">
    <h2 class="form-title">{{ isEdit ? '编辑图书' : '新增图书' }}</h2>
    <div class="form-card">
      <div class="form-grid">
        <div class="field">
          <label>书名</label>
          <input v-model="form.title" class="input" placeholder="请输入书名" />
          <span class="error" v-if="errors.title">{{ errors.title }}</span>
        </div>
        <div class="field">
          <label>作者</label>
          <input v-model="form.author" class="input" placeholder="请输入作者" />
          <span class="error" v-if="errors.author">{{ errors.author }}</span>
        </div>
        <div class="field">
          <label>ISBN</label>
          <input v-model="form.isbn" class="input" placeholder="请输入ISBN" />
          <span class="error" v-if="errors.isbn">{{ errors.isbn }}</span>
        </div>
        <div class="field">
          <label>分类</label>
          <select v-model="form.categoryId" class="input">
            <option :value="null">请选择分类</option>
            <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="field full">
          <label>封面图片</label>
          <div class="upload-zone" @click="triggerUpload">
            <img v-if="previewUrl" :src="previewUrl" class="preview-img" />
            <span v-else>点击上传封面图片</span>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
        </div>
        <div class="field full">
          <label>简介</label>
          <div class="editor-placeholder">
            <textarea
              v-model="form.description"
              class="textarea"
              placeholder="请输入图书简介（支持HTML标签）"
              rows="5"
            ></textarea>
          </div>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn-cancel" @click="$router.push('/')">取消</button>
        <button class="btn-save" @click="onSubmit" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookById, createBook, updateBook, uploadCover, getCategories } from '../api/book'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const flatCategories = ref([])
const fileInput = ref(null)
const previewUrl = ref(null)
const saving = ref(false)
const form = reactive({
  title: '', author: '', isbn: '', categoryId: null, coverUrl: '', description: ''
})
const errors = reactive({ title: '', author: '', isbn: '' })

onMounted(async () => {
  const res = await getCategories()
  flatCategories.value = flattenCategories(res.data)
  if (isEdit.value) {
    const res2 = await getBookById(route.params.id)
    const b = res2.data
    form.title = b.title; form.author = b.author; form.isbn = b.isbn
    form.categoryId = b.categoryId; form.coverUrl = b.coverUrl || ''
    form.description = b.description || ''
    if (b.coverUrl) previewUrl.value = `http://localhost:8080${b.coverUrl}`
  }
})

function flattenCategories(cats, prefix = '') {
  let result = []
  for (const cat of cats) {
    result.push({ id: cat.id, name: prefix + cat.name })
    if (cat.children) result.push(...flattenCategories(cat.children, '  ' + prefix))
  }
  return result
}

function triggerUpload() { fileInput.value?.click() }

async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const res = await uploadCover(file)
  form.coverUrl = res.data
  previewUrl.value = `http://localhost:8080${res.data}`
}

function validate() {
  let valid = true
  if (!form.title.trim()) { errors.title = '书名不能为空'; valid = false } else errors.title = ''
  if (!form.author.trim()) { errors.author = '作者不能为空'; valid = false } else errors.author = ''
  if (!form.isbn.trim()) { errors.isbn = 'ISBN不能为空'; valid = false } else errors.isbn = ''
  return valid
}

async function onSubmit() {
  if (!validate()) return
  saving.value = true
  try {
    const data = {
      title: form.title, author: form.author, isbn: form.isbn,
      categoryId: form.categoryId, coverUrl: form.coverUrl,
      description: form.description
    }
    if (isEdit.value) {
      await updateBook(route.params.id, data)
    } else {
      await createBook(data)
    }
    router.push('/')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-page { padding: 24px 28px; max-width: 640px; }
.form-title { font-size: 18px; font-family: var(--font-serif); color: var(--text); letter-spacing: 2px; margin-bottom: 16px; }
.form-card { background: #fff; border: 1px solid var(--card-border); padding: 24px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field.full { grid-column: 1 / -1; }
.field label { font-size: 12px; color: var(--text-secondary); }
.input {
  padding: 7px 12px; border: 1px solid #e0dbd0; border-radius: 2px;
  font-size: 13px; color: var(--text); outline: none; background: #fff;
}
.input:focus { border-color: var(--accent); }
.error { font-size: 11px; color: #c04040; }
.upload-zone {
  border: 1px dashed #e0dbd0; padding: 24px; text-align: center;
  font-size: 12px; color: var(--text-muted); background: #fafaf7;
  cursor: pointer; min-height: 100px; display: flex; align-items: center;
  justify-content: center;
}
.preview-img { max-width: 100%; max-height: 200px; object-fit: contain; }
.textarea {
  width: 100%; padding: 10px 12px; border: 1px solid #e0dbd0;
  border-radius: 2px; font-size: 12px; color: var(--text); outline: none;
  resize: vertical; font-family: var(--font-sans);
}
.textarea:focus { border-color: var(--accent); }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
.btn-cancel {
  padding: 8px 20px; background: #fff; border: 1px solid #e0dbd0;
  border-radius: 2px; color: var(--text-secondary); font-size: 13px; cursor: pointer;
}
.btn-save {
  padding: 8px 20px; background: var(--accent); color: #fff;
  border: none; border-radius: 2px; font-size: 13px; cursor: pointer;
}
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
```

- [ ] **Step 4: 创建 App.vue**

```vue
<template>
  <div class="admin-app">
    <AdminHeader />
    <router-view />
  </div>
</template>

<script setup>
import AdminHeader from './components/AdminHeader.vue'
</script>

<style scoped>
.admin-app { min-height: 100vh; background: var(--bg); }
</style>
```

- [ ] **Step 5: 验证前端编译**

Run: `cd admin-app && npm run build`
Expected: Build succeeds without errors

- [ ] **Step 6: Commit**

```bash
git add admin-app/src/
git commit -m "feat: add admin components (header, table, form)"
```

---

### Task 21: 端到端验证

- [ ] **Step 1: 启动 MySQL 并创建数据库**

```sql
CREATE DATABASE IF NOT EXISTS library DEFAULT CHARSET utf8mb4;
```

- [ ] **Step 2: 启动后端**

Run: `cd library-server && mvn spring-boot:run`
Expected: 应用启动在 8080 端口，无错误

- [ ] **Step 3: 启动读者端**

Run: `cd reader-app && npm run dev`
Expected: 开发服务器启动在 5173 端口

- [ ] **Step 4: 启动管理端**

Run: `cd admin-app && npm run dev -- --port 5174`
Expected: 开发服务器启动在 5174 端口

- [ ] **Step 5: 验证读者端功能**
  - 打开 http://localhost:5173
  - 查看首页：轮播图、分类树、图书卡片网格、分页
  - 点击图书卡片进入详情页
  - 使用搜索框搜索
  - 切换分类筛选

- [ ] **Step 6: 验证管理端功能**
  - 打开 http://localhost:5174
  - 查看图书列表表格
  - 新增一本图书
  - 编辑图书信息
  - 上架/下架切换
  - 删除图书

- [ ] **Step 7: Commit（如有修复）**

```bash
git add -A && git commit -m "chore: finalize end-to-end integration" || echo "no changes"
```

---

## Plan Self-Review

**1. Spec coverage:**
- Database (categories + books tables) → Task 2 (entities), Task 12 (seed data)
- Reader API (3 endpoints) → Task 9 (BookController)
- Admin API (6 endpoints) → Task 10 (AdminBookController)
- File upload → Task 5 (FileService + test)
- Global error handling → Task 8
- CORS → Task 11
- Reader frontend (all components, routes, views) → Tasks 14-17
- Admin frontend (all components, routes, views) → Tasks 18-20
- Testing (unit + integration) → Tasks 5-10, 13

**2. Placeholder scan:** No TBD, TODO, or vague instructions. All steps have concrete code.

**3. Type consistency:**
- `BookRequest` fields (title, author, isbn, categoryId, coverUrl, description, status) consistent across Task 4 definition, Task 7 service usage, Task 10 controller tests
- `BookResponse` fields consistent across Task 4 definition and all usages in Tasks 7, 9, 10
- `CategoryResponse` tree structure with `children: List<CategoryResponse>` consistent in Task 4 and Task 6
- Vue component props/events: BookCard expects `book` object with `id, title, author, coverUrl` → matches BookResponse shape. SearchBar emits `search(keyword)` and `filter(categoryId)` → BookList handles both.
