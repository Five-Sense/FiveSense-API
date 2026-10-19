package com.fivesense.api.occurrences.app;

import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.shared.error.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OccurrenceImageReaderTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 4, 5, 6};
    private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 1};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 1};

    private final OccurrenceImageReader reader = new OccurrenceImageReader(DataSize.ofKilobytes(1));

    private static String base64(byte[] bytes) { return Base64.getEncoder().encodeToString(bytes); }

    private static void assertStatus(Throwable thrown, HttpStatus status) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(status));
    }

    @Test void noImagesIsValidAndReturnsAnEmptyList() {
        assertThat(reader.read(null, null, null)).isEmpty();
        assertThat(reader.read(List.of(), List.of(), List.of())).isEmpty();
    }

    @Test void readsBinaryFilesBase64TextAndJsonItemsInOrderWithIncrementalContentIds() {
        var images = reader.read(
                List.of(new MockMultipartFile("images", "foto.png", "image/png", PNG)),
                List.of("data:image/jpeg;base64," + base64(JPEG)),
                List.of(new OccurrenceDtos.ImageContent("anim.gif", base64(GIF)), new OccurrenceDtos.ImageContent(null, base64(WEBP))));

        assertThat(images).extracting("contentId").containsExactly("image1", "image2", "image3", "image4");
        assertThat(images).extracting("contentType").containsExactly("image/png", "image/jpeg", "image/gif", "image/webp");
        assertThat(images).extracting("name").containsExactly("foto.png", "imagem.jpg", "anim.gif", "imagem.webp");
        assertThat(images.get(0).content()).isEqualTo(PNG);
        assertThat(images.get(1).content()).isEqualTo(JPEG);
    }

    @Test void acceptsUrlSafeBase64WithLineBreaksAndMissingPadding() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xFB, (byte) 0xFF, (byte) 0xFE, 1};
        String urlSafe = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        assertThat(urlSafe).containsAnyOf("-", "_");
        var images = reader.read(null, List.of(urlSafe.substring(0, 4) + "\r\n " + urlSafe.substring(4)), null);
        assertThat(images.get(0).content()).isEqualTo(bytes);
    }

    @Test void detectsTheRealTypeAndFixesAWrongOrMissingExtension() {
        var images = reader.read(List.of(
                new MockMultipartFile("images", "foto.jpg", "image/jpeg", PNG),
                new MockMultipartFile("images", "foto.JPEG", "image/jpeg", JPEG),
                new MockMultipartFile("images", "semextensao", "application/octet-stream", GIF)), null, null);
        assertThat(images).extracting("name").containsExactly("foto.png", "foto.JPEG", "semextensao.gif");
    }

    @Test void stripsDirectoriesAndUnsafeCharactersFromTheName() {
        var images = reader.read(List.of(new MockMultipartFile("images", "..\\..\\etc/pass:wd\r\n.png", "image/png", PNG)), null, null);
        assertThat(images.get(0).name()).isEqualTo("pass_wd__.png");
    }

    @Test void rejectsContentThatIsNotAnImage() {
        assertThatThrownBy(() -> reader.read(null, List.of(base64("just text".getBytes())), null))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST)).hasMessageContaining("Unsupported image type");
    }

    @Test void rejectsEmptyFilesAndEmptyBase64() {
        assertThatThrownBy(() -> reader.read(List.of(new MockMultipartFile("images", "v.png", "image/png", new byte[0])), null, null))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reader.read(null, List.of(""), null)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reader.read(null, List.of("data:image/png;base64,"), null)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
    }

    @Test void rejectsInvalidBase64AndMalformedDataUri() {
        assertThatThrownBy(() -> reader.read(null, List.of("***not base64***"), null)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reader.read(null, List.of("data:image/png;base64"), null)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> reader.read(null, java.util.Arrays.asList((String) null), null)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
    }

    @Test void rejectsImagesAboveTheMaximumSizeWithoutDecodingHugePayloads() {
        byte[] big = new byte[1025];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        assertThatThrownBy(() -> reader.read(List.of(new MockMultipartFile("images", "g.png", "image/png", big)), null, null))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONTENT_TOO_LARGE));
        assertThatThrownBy(() -> reader.read(null, List.of(base64(big)), null)).satisfies(ex -> assertStatus(ex, HttpStatus.CONTENT_TOO_LARGE));
        assertThatThrownBy(() -> reader.read(null, List.of("A".repeat(100_000)), null)).satisfies(ex -> assertStatus(ex, HttpStatus.CONTENT_TOO_LARGE));
    }

    @Test void limitsTheTotalNumberOfImagesAcrossAllSources() {
        var items = Stream.generate(() -> new OccurrenceDtos.ImageContent("a.png", base64(PNG))).limit(OccurrenceDtos.MAX_IMAGES).toList();
        assertThat(reader.read(null, null, items)).hasSize(OccurrenceDtos.MAX_IMAGES);
        assertThatThrownBy(() -> reader.read(null, List.of(base64(PNG)), items)).satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));
    }
}
