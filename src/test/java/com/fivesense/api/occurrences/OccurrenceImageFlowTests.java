package com.fivesense.api.occurrences;

import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.controller.OccurrenceController;
import com.fivesense.api.occurrences.app.OccurrenceMapper;
import com.fivesense.api.occurrences.app.OccurrenceService;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.occurrences.infra.TemporaryImageStore;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.error.ApiExceptionHandler;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.app.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the image upload and occurrence endpoints. They use the real controller, service
 * and temporary store (in a temp directory); only the database-backed collaborators and SMTP are mocked.
 */
class OccurrenceImageFlowTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 4, 5, 6};

    private Path dir;
    private EmailService email;
    private ProblemService problems;
    private MockMvc mvc;

    @BeforeEach void setup() throws IOException {
        dir = Files.createTempDirectory("fivesense-image-flow-test");
        var store = new TemporaryImageStore(dir.toString(), DataSize.ofKilobytes(10));
        email = mock(EmailService.class);
        problems = mock(ProblemService.class);
        var occurrences = mock(OccurrenceRepository.class);
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv -> inv.getArgument(0));
        var mapper = mock(OccurrenceMapper.class);
        when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 2, Instant.now()));
        var users = mock(UserService.class);
        when(users.notificationRecipients()).thenReturn(List.of("manager@example.com"));
        var service = new OccurrenceService(occurrences, mapper, problems, mock(MaterialService.class), users, email, store);
        mvc = MockMvcBuilders.standaloneSetup(new OccurrenceController(service)).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @AfterEach void cleanup() throws IOException {
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    private long storedFileCount() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> !p.getFileName().toString().endsWith(".name")).count();
        }
    }

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test void multipartUploadAcceptsSeveralImageFilesInOneRequest() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images")
                        .file(new MockMultipartFile("images", "primeira.png", "image/png", PNG))
                        .file(new MockMultipartFile("images", "segunda.jpg", "image/jpeg", JPEG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageIds.length()").value(2))
                .andExpect(jsonPath("$.imageId").exists());
        assertThat(storedFileCount()).isEqualTo(2);
    }

    @Test void multipartUploadStillAcceptsTheLegacySingleImageField() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images").file(new MockMultipartFile("image", "foto.png", "image/png", PNG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageIds.length()").value(1))
                .andExpect(jsonPath("$.imageId").value(org.hamcrest.Matchers.notNullValue()));
    }

    @Test void multipartUploadAcceptsImagesSentAsBase64Text() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images")
                        .param("images", "data:image/png;base64," + base64(PNG))
                        .param("images", base64(JPEG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageIds.length()").value(2));
        assertThat(storedFileCount()).isEqualTo(2);
    }

    @Test void multipartUploadAcceptsFilesAndTextTogether() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images")
                        .file(new MockMultipartFile("images", "foto.png", "image/png", PNG))
                        .param("image", base64(JPEG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageIds.length()").value(2));
    }

    @Test void jsonUploadAcceptsSeveralBase64Images() throws Exception {
        var body = JsonMapper.builder().build().writeValueAsString(new OccurrenceDtos.ImageBatchRequest(List.of(
                new OccurrenceDtos.ImageContent("a.png", base64(PNG)), new OccurrenceDtos.ImageContent(null, base64(JPEG)))));
        mvc.perform(post("/api/v1/occurrences/images").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageIds.length()").value(2));
        assertThat(storedFileCount()).isEqualTo(2);
    }

    @Test void uploadWithoutAnyImageIsRejected() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images").param("other", "x")).andExpect(status().isBadRequest());
    }

    @Test void uploadOfAnEmptyFileIsRejected() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images").file(new MockMultipartFile("images", "vazio.png", "image/png", new byte[0])))
                .andExpect(status().isBadRequest());
        assertThat(storedFileCount()).isZero();
    }

    @Test void uploadAboveTheMaximumNumberOfImagesIsRejected() throws Exception {
        var images = Stream.generate(() -> new OccurrenceDtos.ImageContent("a.png", base64(PNG))).limit(OccurrenceDtos.MAX_IMAGES + 1).toList();
        var body = JsonMapper.builder().build().writeValueAsString(new OccurrenceDtos.ImageBatchRequest(images));
        mvc.perform(post("/api/v1/occurrences/images").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        assertThat(storedFileCount()).isZero();
    }

    @Test void invalidBase64LeavesNothingStored() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images").param("images", base64(PNG)).param("images", "***not base64***"))
                .andExpect(status().isBadRequest());
        assertThat(storedFileCount()).isZero();
    }

    @Test void oversizedImageIsRejectedWithPayloadTooLarge() throws Exception {
        mvc.perform(multipart("/api/v1/occurrences/images").file(new MockMultipartFile("images", "grande.png", "image/png", new byte[10 * 1024 + 1])))
                .andExpect(status().isContentTooLarge());
        assertThat(storedFileCount()).isZero();
    }

    @Test void occurrenceWithSeveralUploadedImagesSendsThemAsAttachmentsAndCleansTheTempFiles() throws Exception {
        var upload = mvc.perform(multipart("/api/v1/occurrences/images")
                        .file(new MockMultipartFile("images", "primeira", "application/octet-stream", PNG))
                        .file(new MockMultipartFile("images", "segunda.jpg", "image/jpeg", JPEG)))
                .andExpect(status().isCreated()).andReturn();
        var ids = JsonMapper.builder().build().readTree(upload.getResponse().getContentAsString()).get("imageIds");
        String imageIds = List.of(ids.get(0).asString(), ids.get(1).asString()).stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(","));
        UUID problemId = UUID.randomUUID();
        when(problems.requireActive(problemId)).thenReturn(new ProblemService.NotificationDetails(problemId, "P", "quality@example.com", "Received"));

        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"problemId\":\"" + problemId + "\",\"materialId\":\"" + UUID.randomUUID() + "\",\"affectedQuantity\":2,\"reportedByUserId\":\""
                                + UUID.randomUUID() + "\",\"imageIds\":[" + imageIds + "]}"))
                .andExpect(status().isCreated());

        var attachments = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(email).sendWithAttachments(eq("manager@example.com"), anyString(), anyString(), attachments.capture());
        verify(email).sendWithAttachments(eq("quality@example.com"), anyString(), eq("Received"), anyList());
        verify(email, never()).send(anyString(), anyString(), anyString());
        assertThat(attachments.getValue()).extracting("name").containsExactly("primeira.png", "segunda.jpg");
        assertThat(storedFileCount()).isZero();
    }

    @Test void occurrenceWithMoreImageIdsThanAllowedIsRejected() throws Exception {
        String ids = Stream.generate(() -> "\"" + UUID.randomUUID() + "\"").limit(OccurrenceDtos.MAX_IMAGES + 1).collect(Collectors.joining(","));
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"problemId\":\"" + UUID.randomUUID() + "\",\"materialId\":\"" + UUID.randomUUID() + "\",\"affectedQuantity\":2,\"reportedByUserId\":\""
                                + UUID.randomUUID() + "\",\"imageIds\":[" + ids + "]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(email);
    }
}
