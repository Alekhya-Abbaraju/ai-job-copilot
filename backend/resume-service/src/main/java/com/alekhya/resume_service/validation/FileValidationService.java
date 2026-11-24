package com.alekhya.resume_service.validation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileValidationService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "docx");
    public void validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a non-empty resume file."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Resume file must not exceed 5 MB."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()
                || filename.contains("/")
                || filename.contains("\\")
                || filename.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(
                    "Invalid filename."
            );
        }

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex == filename.length() - 1) {
            throw new IllegalArgumentException(
                    "Only PDF and DOCX files are allowed."
            );
        }

        String extension = filename.substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF and DOCX files are allowed."
            );
        }
        byte[] content = file.getBytes();
        if ("pdf".equals(extension)) {
            validatePdf(content);
        } else {
            validateDocx(content);
        }
    }

    private void validatePdf(byte[] content) {

        byte[] signature = {
                '%', 'P', 'D', 'F', '-'
        };

        if (content.length < signature.length) {
            throw new IllegalArgumentException(
                    "The uploaded file is not a valid PDF."
            );
        }

        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) {
                throw new IllegalArgumentException(
                        "The uploaded file is not a valid PDF."
                );
            }
        }
    }

    private void validateDocx(byte[] content) throws IOException {

        if (content.length < 4
                || content[0] != 'P'
                || content[1] != 'K') {
            throw new IllegalArgumentException(
                    "The uploaded file is not a valid DOCX document."
            );
        }

        boolean hasContentTypes = false;
        boolean hasDocumentXml = false;

        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(content))) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();

                if ("[Content_Types].xml".equals(name)) {
                    hasContentTypes = true;
                }

                if ("word/document.xml".equals(name)) {
                    hasDocumentXml = true;
                }

                zip.closeEntry();

                if (hasContentTypes && hasDocumentXml) {
                    return;
                }
            }
        }

        throw new IllegalArgumentException(
                "The uploaded file is not a valid DOCX document."
        );
    }
}