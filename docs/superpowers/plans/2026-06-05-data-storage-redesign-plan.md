# Data Storage Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move cover images from filesystem to database, add shuhai/ backup directory for EPUB and cover files.

**Architecture:** Database is primary storage (books.epub_data + new books.cover_data), shuhai/ directory is backup-only. Cover served via new `/api/books/{id}/cover` endpoint instead of static file paths.

**Tech Stack:** Java Spring Boot 3.4 + JPA, Vue 3 (reader-app + admin-app), MySQL

---

### Task 1: Create shuhai directory structure

**Files:**
- Create: `shuhai/epub/README.txt`
- Create: `shuhai/covers/README.txt`
- Create: `shuhai/.gitignore`
- Modify: `.gitignore`

- [ ] **Step 1: Create directory and files**

```bash
mkdir -p shuhai/epub shuhai/covers
```

- [ ] **Step 2: Write README files**

`shuhai/epub/README.txt`:
```
EPUB电子书备份副本
- 以bookId命名（如 1.epub）
- 来源：books.epub_data
- 主存储为MySQL数据库，此目录仅作备份
```

`shuhai/covers/README.txt`:
```
图书封面备份副本
- 以bookId命名（如 1.jpg）
- 来源：books.cover_data
- 主存储为MySQL数据库，此目录仅作备份
```

`shuhai/.gitignore`:
```
*
!.gitignore
!README.txt
```

- [ ] **Step 3: Update root .gitignore**

Replace `uploads/` with `shuhai/` in root `.gitignore`:

```
uploads/ → shuhai/
```

- [ ] **Step 4: Commit**

```bash
git add shuhai/ .gitignore
git commit -m "chore: add shuhai backup directory structure"
```

---

### Task 2: Add coverData to Book entity

**Files:**
- Modify: `library-server/src/main/java/com/library/entity/Book.java`

- [ ] **Step 1: Add coverData field and getter/setter**

After the `description` field (line 30-31), add:

```java
@Lob
@Column(columnDefinition = "MEDIUMBLOB")
private byte[] coverData;
```

Add getter/setter (after `setDescription`, before `getStatus`):

```java
public byte[] getCoverData() { return coverData; }
public void setCoverData(byte[] coverData) { this.coverData = coverData; }
```

- [ ] **Step 2: Update Builder**

In the `Builder` class, add field:

```java
private byte[] coverData;
```

Add builder method:

```java
public Builder coverData(byte[] coverData) { this.coverData = coverData; return this; }
```

Update `build()` method, pass `coverData` to the `Book` constructor. Add `coverData` param to the all-args constructor and update it.

- [ ] **Step 3: Update the all-args constructor**

```java
public Book(Long id, String title, String author, String isbn, Long categoryId,
            String coverUrl, String description, byte[] epubData, byte[] coverData,
            Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    // ... existing assignments ...
    this.coverData = coverData;
}
```

Update `Builder.build()` to pass `coverData` as the 9th parameter.

- [ ] **Step 4: Build and verify compilation**

```bash
cd library-server && ./mvnw compile -q
```

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/entity/Book.java
git commit -m "feat: add coverData MEDIUMBLOB field to Book entity"
```

---

### Task 3: Update DTOs (BookResponse, BookRequest)

**Files:**
- Modify: `library-server/src/main/java/com/library/dto/BookResponse.java`
- Modify: `library-server/src/main/java/com/library/dto/BookRequest.java`

- [ ] **Step 1: Replace coverUrl with hasCover in BookResponse**

Replace the `coverUrl` field and all its usages:

```java
private boolean hasCover;

// In all-args constructor: replace String coverUrl with boolean hasCover
// In getter/setter:
public boolean isHasCover() { return hasCover; }
public void setHasCover(boolean hasCover) { this.hasCover = hasCover; }

// In Builder:
private boolean hasCover;
public Builder hasCover(boolean hasCover) { this.hasCover = hasCover; return this; }
// In build(): pass hasCover instead of coverUrl
```

- [ ] **Step 2: Remove coverUrl from BookRequest**

Remove the `coverUrl` field, getter, and setter.

- [ ] **Step 3: Build and verify compilation**

```bash
cd library-server && ./mvnw compile -q
```

Expected: compilation errors in BookService.java (references to coverUrl/toResponse). Ignore for now.

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/dto/
git commit -m "refactor: replace coverUrl with hasCover in DTOs, remove coverUrl from BookRequest"
```

---

### Task 4: Add getCoverData + backup logic to BookService

**Files:**
- Modify: `library-server/src/main/java/com/library/service/BookService.java`

- [ ] **Step 1: Add backup helper method**

Add to the end of BookService class:

```java
private void backupToFile(byte[] data, String subdir, String filename) {
    try {
        java.nio.file.Path dir = java.nio.file.Paths.get("shuhai", subdir);
        java.nio.file.Files.createDirectories(dir);
        java.nio.file.Files.write(dir.resolve(filename), data,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
    } catch (java.io.IOException e) {
        // 备份失败不影响主流程
    }
}
```

- [ ] **Step 2: Add getCoverData method**

```java
public byte[] getCoverData(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("图书不存在: " + id));
    if (book.getCoverData() == null) {
        throw new jakarta.persistence.EntityNotFoundException("该图书无封面");
    }
    return book.getCoverData();
}
```

- [ ] **Step 3: Update toResponse to use hasCover**

In the `toResponse` method, replace:
```java
.coverUrl(book.getCoverUrl())
```
with:
```java
.hasCover(book.getCoverData() != null)
```

- [ ] **Step 4: Add format extension helper**

```java
private String imageExtension(String contentType) {
    if (contentType != null && contentType.contains("png")) return ".png";
    return ".jpg";
}
```

- [ ] **Step 5: Build and verify compilation**

```bash
cd library-server && ./mvnw compile -q
```

Expected: still compilation errors in createBook/updateBook and in AdminBookController. These will be fixed next.

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/service/BookService.java
git commit -m "feat: add getCoverData, backupToFile helper, update toResponse hasCover"
```

---

### Task 5: Modify createBook/updateBook for cover + backup

**Files:**
- Modify: `library-server/src/main/java/com/library/service/BookService.java`

- [ ] **Step 1: Change method signatures to accept cover MultipartFile**

Change `createBook` signature:
```java
public BookResponse createBook(MultipartFile epubFile, MultipartFile coverFile, BookRequest req) {
```

Change `updateBook` signature:
```java
public BookResponse updateBook(Long id, MultipartFile epubFile, MultipartFile coverFile, BookRequest req) {
```

Rename all internal `file` references to `epubFile` in both methods.

- [ ] **Step 2: Add cover handling in createBook**

After the EPUB extraction block and before `book = bookRepository.save(book)`, add:

```java
if (coverFile != null && !coverFile.isEmpty()) {
    try {
        book.setCoverData(coverFile.getBytes());
    } catch (Exception e) {
        throw new RuntimeException("封面文件读取失败", e);
    }
}
```

After `book = bookRepository.save(book)`, add backup calls:

```java
if (book.getEpubData() != null) {
    backupToFile(book.getEpubData(), "epub", book.getId() + ".epub");
}
if (book.getCoverData() != null) {
    String ext = imageExtension(coverFile != null ? coverFile.getContentType() : null);
    backupToFile(book.getCoverData(), "covers", book.getId() + ext);
}
```

- [ ] **Step 3: Add cover handling in updateBook**

After the EPUB file update block and before `book = bookRepository.save(book)`, add:

```java
if (coverFile != null && !coverFile.isEmpty()) {
    try {
        book.setCoverData(coverFile.getBytes());
    } catch (Exception e) {
        throw new RuntimeException("封面文件读取失败", e);
    }
}
```

After `book = bookRepository.save(book)`, add backup calls:

```java
if (book.getEpubData() != null) {
    backupToFile(book.getEpubData(), "epub", book.getId() + ".epub");
}
if (book.getCoverData() != null) {
    String ext = imageExtension(coverFile != null ? coverFile.getContentType() : null);
    backupToFile(book.getCoverData(), "covers", book.getId() + ext);
}
```

- [ ] **Step 4: Build and verify compilation**

```bash
cd library-server && ./mvnw compile -q
```

Expected: still errors in AdminBookController.java (calling old signatures). No other errors.

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/service/BookService.java
git commit -m "feat: add cover file param to createBook/updateBook, add shuhai backup on save"
```

---

### Task 6: Add GET /api/books/{id}/cover to BookController

**Files:**
- Modify: `library-server/src/main/java/com/library/controller/BookController.java`

- [ ] **Step 1: Write the failing test**

Create/modify `library-server/src/test/java/com/library/controller/BookControllerTest.java`. Add test:

```java
@Test
void getCover_shouldReturnCoverImage() throws Exception {
    byte[] coverBytes = "fake-image-data".getBytes();
    when(bookService.getCoverData(1L)).thenReturn(coverBytes);

    mockMvc.perform(get("/api/books/1/cover"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG))
        .andExpect(content().bytes(coverBytes));
}

@Test
void getCover_shouldReturn404_whenNoCover() throws Exception {
    when(bookService.getCoverData(1L))
        .thenThrow(new EntityNotFoundException("该图书无封面"));

    mockMvc.perform(get("/api/books/1/cover"))
        .andExpect(status().isNotFound());
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd library-server && ./mvnw test -Dtest=BookControllerTest#getCover_shouldReturnCoverImage -pl . -q
```

Expected: FAIL (404, no mapping)

- [ ] **Step 3: Add the endpoint**

```java
@GetMapping("/books/{id}/cover")
public ResponseEntity<byte[]> getCover(@PathVariable Long id) {
    byte[] data = bookService.getCoverData(id);
    return ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
            .body(data);
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd library-server && ./mvnw test -Dtest=BookControllerTest -q
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/BookController.java
git add library-server/src/test/java/com/library/controller/BookControllerTest.java
git commit -m "feat: add GET /api/books/{id}/cover endpoint"
```

---

### Task 7: Refactor AdminBookController

**Files:**
- Modify: `library-server/src/main/java/com/library/controller/AdminBookController.java`

- [ ] **Step 1: Remove FileService dependency, add cover param to createBook**

Change constructor and createBook endpoint:

```java
@RestController
@RequestMapping("/api/admin")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/books")
    public ApiResponse<BookResponse> createBook(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam("isbn") String isbn,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "description", required = false) String description) {
        BookRequest req = new BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        if (categoryId != null) req.setCategoryId(categoryId);
        if (description != null) req.setDescription(description);
        return ApiResponse.success(bookService.createBook(file, cover, req));
    }
```

Remove `coverUrl` param from both createBook and updateBook.

- [ ] **Step 2: Update updateBook similarly**

```java
@PutMapping("/books/{id}")
public ApiResponse<BookResponse> updateBook(
        @PathVariable Long id,
        @RequestPart(value = "file", required = false) MultipartFile file,
        @RequestPart(value = "cover", required = false) MultipartFile cover,
        @RequestParam("title") String title,
        @RequestParam("author") String author,
        @RequestParam("isbn") String isbn,
        @RequestParam(value = "categoryId", required = false) Long categoryId,
        @RequestParam(value = "description", required = false) String description) {
    BookRequest req = new BookRequest();
    req.setTitle(title);
    req.setAuthor(author);
    req.setIsbn(isbn);
    if (categoryId != null) req.setCategoryId(categoryId);
    if (description != null) req.setDescription(description);
    return ApiResponse.success(bookService.updateBook(id, file, cover, req));
}
```

- [ ] **Step 3: Remove /upload/cover endpoint and unused imports**

Delete the `uploadCover` method entirely. Remove `FileService` import, `MultipartFile` import stays. Remove `IOException` import. Remove `fileService` field.

- [ ] **Step 4: Update AdminBookControllerTest**

Fix test to pass cover as null or as MockMultipartFile. Update any `uploadCover` test references — delete those tests.

- [ ] **Step 5: Build and run tests**

```bash
cd library-server && ./mvnw test -Dtest=AdminBookControllerTest -q
```

Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add library-server/src/main/java/com/library/controller/AdminBookController.java
git add library-server/src/test/java/com/library/controller/AdminBookControllerTest.java
git commit -m "refactor: AdminBookController accepts cover file, removes uploadCover endpoint"
```

---

### Task 8: Cleanup — delete FileService, FileUploadConfig, update WebConfig

**Files:**
- Delete: `library-server/src/main/java/com/library/service/FileService.java`
- Delete: `library-server/src/main/java/com/library/config/FileUploadConfig.java`
- Delete: `library-server/src/test/java/com/library/service/FileServiceTest.java`
- Modify: `library-server/src/main/java/com/library/config/WebConfig.java`

- [ ] **Step 1: Delete files**

```bash
rm library-server/src/main/java/com/library/service/FileService.java
rm library-server/src/main/java/com/library/config/FileUploadConfig.java
rm library-server/src/test/java/com/library/service/FileServiceTest.java
```

- [ ] **Step 2: Update WebConfig — remove FileUploadConfig dependency and addResourceHandlers**

Remove `FileUploadConfig` import, `Paths` import. Remove constructor injection. Remove `addResourceHandlers` override. Keep `addCorsMappings`, `characterEncodingFilter`, `stringHttpMessageConverter`.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:5175",
                           "http://localhost:5176", "http://localhost:5177", "http://localhost:5178")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

- [ ] **Step 3: Build and run all tests**

```bash
cd library-server && ./mvnw test -q
```

Expected: all tests PASS

- [ ] **Step 4: Commit**

```bash
git add library-server/
git commit -m "refactor: remove FileService/FileUploadConfig, simplify WebConfig"
```

---

### Task 9: Data migration runner

**Files:**
- Create: `library-server/src/main/java/com/library/config/DataMigrationRunner.java`

- [ ] **Step 1: Write the migration runner**

```java
package com.library.config;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component
public class DataMigrationRunner implements ApplicationRunner {

    private final BookRepository bookRepository;

    public DataMigrationRunner(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Path marker = Paths.get("shuhai", ".migration_done");
        if (Files.exists(marker)) return;

        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            if (book.getEpubData() != null && book.getEpubData().length > 0) {
                try {
                    Path dir = Paths.get("shuhai", "epub");
                    Files.createDirectories(dir);
                    Files.write(dir.resolve(book.getId() + ".epub"), book.getEpubData(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    // skip failed backup
                }
            }

            String coverUrl = book.getCoverUrl();
            if (coverUrl != null && !coverUrl.isBlank()) {
                try {
                    Path srcPath = Paths.get(coverUrl.substring(1));
                    if (Files.exists(srcPath)) {
                        byte[] data = Files.readAllBytes(srcPath);
                        book.setCoverData(data);
                        bookRepository.save(book);

                        Path dir = Paths.get("shuhai", "covers");
                        Files.createDirectories(dir);
                        String ext = coverUrl.contains(".")
                            ? coverUrl.substring(coverUrl.lastIndexOf("."))
                            : ".jpg";
                        Files.write(dir.resolve(book.getId() + ext), data,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (IOException e) {
                    // skip failed migration
                }
            }
        }

        try {
            Files.createDirectories(Paths.get("shuhai"));
            Files.createFile(marker);
        } catch (IOException e) {
            // skip marker creation
        }
    }
}
```

- [ ] **Step 2: Build and verify compilation**

```bash
cd library-server && ./mvnw compile -q
```

Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/config/DataMigrationRunner.java
git commit -m "feat: add DataMigrationRunner to export epub/covers to shuhai/"
```

---

### Task 10: Update reader-app (BookCard, BookDetail)

**Files:**
- Modify: `reader-app/src/views/BookDetail.vue`
- Modify: `reader-app/src/components/BookCard.vue`

- [ ] **Step 1: Update BookCard.vue**

Change line 4:
```html
<img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
```
To:
```html
<img v-if="book.hasCover" :src="`/api/books/${book.id}/cover`" :alt="book.title" />
```

- [ ] **Step 2: Update BookDetail.vue**

Change line 13:
```html
<img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
```
To:
```html
<img v-if="book.hasCover" :src="`/api/books/${book.id}/cover`" :alt="book.title" />
```

- [ ] **Step 3: Build to verify compilation**

```bash
cd reader-app && npx vite build --emptyOutDir 2>&1 | tail -5
```

Expected: built successfully

- [ ] **Step 4: Commit**

```bash
git add reader-app/src/components/BookCard.vue reader-app/src/views/BookDetail.vue
git commit -m "refactor: reader-app uses hasCover + /api/books/{id}/cover endpoint"
```

---

### Task 11: Update admin-app (BookTable, BookForm, book.js)

**Files:**
- Modify: `admin-app/src/views/BookTable.vue`
- Modify: `admin-app/src/views/BookForm.vue`
- Modify: `admin-app/src/api/book.js`

- [ ] **Step 1: Update BookTable.vue**

Change line 29:
```html
<img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
```
To:
```html
<img v-if="book.hasCover" :src="`/api/books/${book.id}/cover`" :alt="book.title" />
```

- [ ] **Step 2: Update BookForm.vue — cover upload flow**

**a)** Add `coverFile` and `coverPreviewUrl` refs (replace `previewUrl`):
```javascript
const coverFile = ref(null)
const coverPreviewUrl = ref(null)
```

**b)** Change `onFileChange` (remove uploadCover API call, add JPEG/PNG validation):
```javascript
function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    alert('封面仅支持 JPEG 或 PNG 格式')
    return
  }
  coverFile.value = file
  coverPreviewUrl.value = URL.createObjectURL(file)
}
```

Also update the file input `accept` attribute from `image/*` to `.jpg,.jpeg,.png`:

**c)** Update template — change `previewUrl` to `coverPreviewUrl`:
```html
<img v-if="coverPreviewUrl" :src="coverPreviewUrl" class="preview-img" />
```

**d)** Update `onMounted` — remove `b.coverUrl` handling:
```javascript
// Remove these lines:
// form.coverUrl = b.coverUrl || ''
// if (b.coverUrl) previewUrl.value = b.coverUrl
```

**e)** Update `save()` — append cover file instead of coverUrl:
```javascript
// Remove: if (form.coverUrl) fd.append('coverUrl', form.coverUrl)
// Add:
if (coverFile.value) fd.append('cover', coverFile.value)
```

**f)** Remove `uploadCover` import:
```javascript
import { getBookById, createBook, updateBook, getCategories } from '../api/book'
```

**g)** Remove `coverUrl` from form reactive:
```javascript
const form = reactive({
  title: '', author: '', isbn: '', categoryId: null, description: '', content: ''
})
```

- [ ] **Step 3: Update admin-app book.js — remove uploadCover**

Remove the `uploadCover` function (lines 37-41):
```javascript
export function uploadCover(file) { ... }  // DELETE this
```

- [ ] **Step 4: Build to verify compilation**

```bash
cd admin-app && npx vite build --emptyOutDir 2>&1 | tail -5
```

Expected: built successfully

- [ ] **Step 5: Commit**

```bash
git add admin-app/src/views/BookTable.vue admin-app/src/views/BookForm.vue admin-app/src/api/book.js
git commit -m "refactor: admin-app uses hasCover + cover file in FormData, removes uploadCover API"
```

---

### Task 12: Final cleanup — remove coverUrl from entity + uploads/ directory

**Files:**
- Modify: `library-server/src/main/java/com/library/entity/Book.java`
- Modify: `library-server/src/main/java/com/library/repository/BookRepository.java`

- [ ] **Step 1: Remove coverUrl from Book entity**

Delete `coverUrl` field, getter, setter. Remove from all-args constructor and Builder. Remove from `BookRepository` query if referenced.

Change column name annotation is not needed since the field is gone — but the column still exists in DB. The `@Column(name = "cover_url")` is tied to the field; removing the field means Hibernate won't reference it. The DB column can be dropped manually later.

- [ ] **Step 2: Build and run all tests**

```bash
cd library-server && ./mvnw test -q
```

Expected: all tests PASS

- [ ] **Step 3: Clean up uploads directory**

If `uploads/covers/` exists, remove it:
```bash
rm -rf library-server/uploads/
```

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/entity/Book.java
git add library-server/src/main/java/com/library/repository/BookRepository.java
git commit -m "refactor: remove coverUrl field from Book entity, delete uploads/ directory"
```

---

### Verification

After all tasks complete, run the full test suite:

```bash
cd library-server && ./mvnw test -q
cd reader-app && npx vite build --emptyOutDir
cd admin-app && npx vite build --emptyOutDir
```

Start the application and verify:
1. Upload a new book with cover → cover appears in reader + admin
2. Existing books with migrated covers display correctly
3. `shuhai/epub/` has backup files
4. `shuhai/covers/` has backup files
5. `/api/books/{id}/cover` returns image for books with covers
6. `/api/books/{id}/cover` returns 404 for books without covers
