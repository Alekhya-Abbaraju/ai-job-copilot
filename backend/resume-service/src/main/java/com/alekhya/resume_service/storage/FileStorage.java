package com.alekhya.resume_service.storage;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {
    String store(
            InputStream inputStream,
            String storageKey
    ) throws IOException;
    InputStream load(String storageKey) throws IOException;
    void delete(String storageKey) throws IOException;
}
