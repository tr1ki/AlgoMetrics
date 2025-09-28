package com.university.algorithms.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class CsvWriter implements AutoCloseable {
    private final Path path;
    private final BufferedWriter writer;
    private boolean headerWritten;

    public CsvWriter(Path path) {
        this.path = path;
        try {
            Files.createDirectories(path.getParent());
            this.writer = Files.newBufferedWriter(
                    path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to open CSV writer for " + path, e);
        }
    }

    public synchronized void write(Metrics m) {
        try {
            if (!headerWritten && isEmptyFile()) {
                writer.write(Metrics.csvHeader());
                writer.newLine();
                headerWritten = true;
            }
            writer.write(m.toCsvRow());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV row", e);
        }
    }

    private boolean isEmptyFile() throws IOException {
        return !Files.exists(path) || Files.size(path) == 0L;
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            // swallow to keep AutoCloseable simple
        }
    }
}


