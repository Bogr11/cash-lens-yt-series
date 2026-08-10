package com.bornik.cashlens.inbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Receipts live on disk, and the inbound message keeps only the path.
 * Not base64 in a text column — it would bloat every row and drown the logs.
 * Not bytea — that row is read on every duplicate check.
 */
@Slf4j
@Component
class ReceiptStorage {

    private final Path directory;

    ReceiptStorage(@Value("${cashlens.upload-dir}") String directory) {
        this.directory = Path.of(directory);
    }

    String store(byte[] bytes, String originalFilename) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(UUID.randomUUID() + extensionOf(originalFilename));
            Files.write(file, bytes);
            log.info("Stored receipt. File={}, bytes={}", file, bytes.length);
            return file.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store receipt " + originalFilename, e);
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return ".jpg";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? ".jpg" : originalFilename.substring(dot).toLowerCase();
    }

}
