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
