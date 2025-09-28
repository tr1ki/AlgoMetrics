package com.university.algorithms.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvWriterTest {
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("metrics", ".csv");
        Files.deleteIfExists(tempFile); // ensure fresh
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void writesHeaderOnceAndAppendsRows() throws IOException {
        try (CsvWriter w = new CsvWriter(tempFile)) {
            w.write(new Metrics("algoA", 10, 1, 5, 2, 3, 1));
            w.write(new Metrics("algoB", 20, 2, 6, 3, 4, 2));
        }
        String content = Files.readString(tempFile);
        String[] lines = content.trim().split("\r?\n");
        assertEquals(3, lines.length);
        assertEquals(Metrics.csvHeader(), lines[0]);
        assertTrue(lines[1].contains("algoA"));
        assertTrue(lines[2].contains("algoB"));
    }
}


