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
