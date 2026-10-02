package com.fivesense.api.occurrences.infra;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemporaryImageStoreTests {
    private Path dir;
    private TemporaryImageStore store;

    @BeforeEach void setup() throws IOException {
        dir = Files.createTempDirectory("fivesense-store-test");
        store = new TemporaryImageStore(dir.toString());
        store.ensureDirectory();
    }

    @AfterEach void cleanup() throws IOException {
        if (Files.exists(dir)) {
            try (var paths = Files.walk(dir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test void storesLocatesAndDeletesTemporaryImage() {
        UUID id = store.store(new ByteArrayInputStream("bytes".getBytes(StandardCharsets.UTF_8)), "evidencia.png");

        var located = store.locate(id);
        assertThat(located).isPresent();
        assertThat(located.get().originalName()).isEqualTo("evidencia.png");
        assertThat(Files.exists(located.get().path())).isTrue();

        store.delete(id);
        assertThat(store.locate(id)).isEmpty();
    }

    @Test void locateReturnsEmptyForUnknownOrNullId() {
        assertThat(store.locate(UUID.randomUUID())).isEmpty();
        assertThat(store.locate(null)).isEmpty();
    }

    @Test void sanitizesBlankFilenameToDefault() {
        UUID id = store.store(new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)), "   ");
        assertThat(store.locate(id)).isPresent();
        assertThat(store.locate(id).get().originalName()).isEqualTo("imagem");
    }
}
