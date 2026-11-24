package com.alekhya.resume_service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@ConditionalOnProperty(
        name="app.resume.storage.type",
        havingValue = "local",
        matchIfMissing = true
)
@Component
public class LocalFileStorage implements FileStorage {

    private final Path rootDirectory;

    public LocalFileStorage(
            @Value("${app.resume.storage.local-dir:./uploads/resumes}")
            String storageDirectory) throws IOException {

        this.rootDirectory = Path.of(storageDirectory)
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(this.rootDirectory);
    }
    @Override
    public String store(InputStream inputStream, String storageKey)
            throws IOException {
        Path target = resolveSafePath(storageKey);
        Files.createDirectories(target.getParent());
        Files.copy(
                inputStream,
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
        return storageKey;
    }
    @Override
    public InputStream load(String storageKey) throws IOException {
        Path target = resolveSafePath(storageKey);

        if (!Files.isRegularFile(target)) {
            throw new IOException("Resume file not found");
        }

        return Files.newInputStream(target);
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Path target = resolveSafePath(storageKey);
        Files.deleteIfExists(target);
    }

    private Path resolveSafePath(String storageKey) throws IOException {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IOException("Storage key cannot be empty");
        }

        Path target = rootDirectory.resolve(storageKey)
                .toAbsolutePath()
                .normalize();

        if (!target.startsWith(rootDirectory)) {
            throw new IOException("Invalid storage key");
        }

        return target;
    }
}
