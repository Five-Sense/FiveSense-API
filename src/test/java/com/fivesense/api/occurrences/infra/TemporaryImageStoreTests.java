package com.fivesense.api.occurrences.infra;

import com.fivesense.api.shared.error.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporaryImageStoreTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    private Path dir;
    private TemporaryImageStore store;

    @BeforeEach void setup() throws IOException {
        dir = Files.createTempDirectory("fivesense-store-test");
        store = new TemporaryImageStore(dir.toString(), DataSize.ofKilobytes(1));
        store.ensureDirectory();
    }

    @AfterEach void cleanup() throws IOException {
        if (Files.exists(dir)) {
            try (var paths = Files.walk(dir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    private UUID storeBytes(byte[] bytes, String name) {
        return store.store(new ByteArrayInputStream(bytes), name);
    }

    @Test void storesLocatesAndDeletesTemporaryImage() {
        UUID id = storeBytes("bytes".getBytes(StandardCharsets.UTF_8), "evidencia.png");

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
        UUID id = storeBytes("x".getBytes(StandardCharsets.UTF_8), "   ");
        assertThat(store.locate(id)).isPresent();
        assertThat(store.locate(id).get().originalName()).isEqualTo("imagem");
    }

    @Test void addsExtensionOfTheRealImageTypeWhenNameHasNone() {
        assertThat(store.locate(storeBytes(PNG, null)).get().originalName()).isEqualTo("imagem.png");
        assertThat(store.locate(storeBytes(JPEG, "foto")).get().originalName()).isEqualTo("foto.jpg");
    }

    @Test void replacesExtensionThatContradictsTheRealImageType() {
        assertThat(store.locate(storeBytes(PNG, "foto.jpg")).get().originalName()).isEqualTo("foto.png");
    }

    @Test void keepsEquivalentJpegExtension() {
        assertThat(store.locate(storeBytes(JPEG, "foto.JPEG")).get().originalName()).isEqualTo("foto.JPEG");
    }

    @Test void stripsDirectoriesAndUnsafeCharactersFromName() {
        assertThat(store.locate(storeBytes(PNG, "..\\..\\etc/pass:wd\r\n.png")).get().originalName()).isEqualTo("pass_wd__.png");
    }

    @Test void rejectsEmptyContent() {
        assertThatThrownBy(() -> storeBytes(new byte[0], "a.png"))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test void rejectsContentAboveTheMaximumSizeWithoutLeavingFiles() throws IOException {
        assertThatThrownBy(() -> storeBytes(new byte[1025], "a.png"))
                .isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE));
        try (var files = Files.list(dir)) {
            assertThat(files).isEmpty();
        }
        assertThat(store.maxBytes()).isEqualTo(1024);
    }
}
