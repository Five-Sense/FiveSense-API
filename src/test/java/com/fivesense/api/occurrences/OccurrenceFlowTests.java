package com.fivesense.api.occurrences;

import com.fivesense.api.TestTemplates;
import com.fivesense.api.materials.app.MaterialService;
import com.fivesense.api.occurrences.app.OccurrenceImageReader;
import com.fivesense.api.occurrences.app.OccurrenceMapper;
import com.fivesense.api.occurrences.app.OccurrenceService;
import com.fivesense.api.occurrences.controller.OccurrenceController;
import com.fivesense.api.occurrences.domain.Occurrence;
import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.occurrences.infra.OccurrenceRepository;
import com.fivesense.api.problems.app.ProblemService;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.error.ApiExceptionHandler;
import com.fivesense.api.shared.infra.EmailService;
import com.fivesense.api.users.app.UserService;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer flow for registering an occurrence with images: real controller, service, image reader,
 * e-mail service and Thymeleaf template; only the database-backed collaborators and the SMTP sender are mocked.
 * (The PostgreSQL/Testcontainers variant needs a running Docker daemon.)
 */
class OccurrenceFlowTests {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 4, 5, 6};

    private final UUID problemId = UUID.randomUUID(), materialId = UUID.randomUUID(), userId = UUID.randomUUID();
    private JavaMailSender sender;
    private UserService users;
    private MockMvc mvc;

    @BeforeEach void setup() {
        sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenAnswer(inv -> new JavaMailSenderImpl().createMimeMessage());
        var email = new EmailService(sender, TestTemplates.engine(), "noreply@example.com");
        var problems = mock(ProblemService.class);
        when(problems.requireActive(problemId)).thenReturn(new ProblemService.NotificationDetails(problemId, "Vassoura", "quality@example.com", "Recebemos o aviso"));
        var materials = mock(MaterialService.class);
        when(materials.requireActive(materialId)).thenReturn("Vassouras");
        users = mock(UserService.class);
        when(users.requireActive(userId)).thenReturn("Ana");
        when(users.notificationRecipients()).thenReturn(List.of("manager@example.com"));
        var occurrences = mock(OccurrenceRepository.class);
        when(occurrences.save(any(Occurrence.class))).thenAnswer(inv -> inv.getArgument(0));
        var mapper = mock(OccurrenceMapper.class);
        when(mapper.toResponse(any())).thenReturn(new OccurrenceDtos.Response(UUID.randomUUID(), problemId, materialId, userId, 2, Instant.now()));
        var service = new OccurrenceService(occurrences, mapper, problems, materials, users, email, new OccurrenceImageReader(DataSize.ofKilobytes(10)));
        mvc = MockMvcBuilders.standaloneSetup(new OccurrenceController(service)).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private static String base64(byte[] bytes) { return Base64.getEncoder().encodeToString(bytes); }

    private String json(int quantity, UUID reporter, String images) {
        return "{\"problemId\":\"" + problemId + "\",\"materialId\":\"" + materialId + "\",\"affectedQuantity\":" + quantity
                + ",\"reportedByUserId\":\"" + reporter + "\"" + (images == null ? "" : ",\"images\":" + images) + "}";
    }

    private static String jsonImages(int count) {
        return Stream.generate(() -> "{\"name\":\"foto.png\",\"data\":\"" + base64(PNG) + "\"}").limit(count).collect(Collectors.joining(",", "[", "]"));
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder form(int quantity, String reporter) {
        return multipart("/api/v1/occurrences").param("problemId", problemId.toString()).param("materialId", materialId.toString())
                .param("affectedQuantity", String.valueOf(quantity)).param("reportedByUserId", reporter);
    }

    private List<MimeMessage> sentMessages(int expected) {
        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender, times(expected)).send(captor.capture());
        return captor.getAllValues();
    }

    private static String html(MimeMessage mime) throws Exception {
        mime.saveChanges();
        for (BodyPart part : leafParts((Multipart) mime.getContent())) if (part.isMimeType("text/html")) return (String) part.getContent();
        throw new AssertionError("no text/html part");
    }

    private static long inlineImages(MimeMessage mime) throws Exception {
        mime.saveChanges();
        return leafParts((Multipart) mime.getContent()).stream().filter(p -> {
            try { return p.isMimeType("image/*") && "inline".equals(p.getDisposition()); } catch (Exception ex) { throw new IllegalStateException(ex); }
        }).count();
    }

    private static List<BodyPart> leafParts(Multipart multipart) throws Exception {
        List<BodyPart> parts = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.getContent() instanceof Multipart nested) parts.addAll(leafParts(nested)); else parts.add(part);
        }
        return parts;
    }

    @Test void jsonOccurrenceWithBase64ImagesSendsFormattedEmailsWithInlineImages() throws Exception {
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, userId, jsonImages(2))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.affectedQuantity").value(2));

        var messages = sentMessages(2);
        for (MimeMessage mime : messages) {
            assertThat(inlineImages(mime)).isEqualTo(2);
            assertThat(html(mime)).contains("Vassoura").contains("src=\"cid:image1\"").contains("src=\"cid:image2\"");
        }
        assertThat(messages.stream().map(m -> { try { return m.getAllRecipients()[0].toString(); } catch (Exception ex) { throw new IllegalStateException(ex); } }))
                .containsExactly("manager@example.com", "quality@example.com");
        assertThat(html(messages.get(0))).contains("Vassouras").contains("Ana").contains("Quantidade afetada");
        assertThat(html(messages.get(1))).contains("Recebemos o aviso");
    }

    @Test void multipartOccurrenceAcceptsBinaryFilesAndBase64TextTogether() throws Exception {
        mvc.perform(form(2, userId.toString())
                        .file(new MockMultipartFile("images", "foto.png", "image/png", PNG))
                        .file(new MockMultipartFile("images", "foto.jpg", "image/jpeg", JPEG))
                        .param("images", "data:image/png;base64," + base64(PNG)))
                .andExpect(status().isCreated());

        for (MimeMessage mime : sentMessages(2)) {
            assertThat(inlineImages(mime)).isEqualTo(3);
            assertThat(html(mime)).contains("cid:image3");
        }
    }

    @Test void occurrenceWithoutImagesStillSendsTheFormattedEmails() throws Exception {
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, userId, null))).andExpect(status().isCreated());
        mvc.perform(form(2, userId.toString())).andExpect(status().isCreated());

        for (MimeMessage mime : sentMessages(4)) {
            assertThat(inlineImages(mime)).isZero();
            assertThat(html(mime)).doesNotContain("<img");
        }
    }

    @Test void malformedRequestShapeIsStillRejected() throws Exception {
        // Request-shape problems (missing data, too many images) remain 400: they are malformed requests,
        // validated before the service runs.
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, userId, "[{\"name\":\"sem-dados\"}]")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, userId, jsonImages(OccurrenceDtos.MAX_IMAGES + 1))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(sender);
    }

    @Test void badImageContentDoesNotBlockTheOccurrence_itIsSavedAndEmailedWithoutThatImage() throws Exception {
        // The image is only used to enrich the e-mail. A bad image (not a real image, invalid base64, empty)
        // must NOT prevent the occurrence from being saved: it is created and the e-mails are sent without it.
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON)
                        .content(json(2, userId, "[{\"name\":\"x.txt\",\"data\":\"" + base64("not an image".getBytes()) + "\"}]")))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, userId, "[{\"data\":\"***\"}]")))
                .andExpect(status().isCreated());
        mvc.perform(form(2, userId.toString()).file(new MockMultipartFile("images", "vazio.png", "image/png", new byte[0])))
                .andExpect(status().isCreated());

        // Three occurrences created, each sending to the manager and the problem contact: 6 e-mails, all without images.
        for (MimeMessage mime : sentMessages(6)) assertThat(inlineImages(mime)).isZero();
    }

    @Test void invalidFieldsAreRejectedWithBadRequest() throws Exception {
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(0, userId, null))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(form(0, userId.toString())).andExpect(status().isBadRequest());
        mvc.perform(form(2, "not-a-uuid")).andExpect(status().isBadRequest());
        verifyNoInteractions(sender);
    }

    @Test void unknownReportingUserIsRejectedInsteadOfFailingOnTheForeignKey() throws Exception {
        UUID unknown = UUID.randomUUID();
        when(users.requireActive(unknown)).thenThrow(ApiException.badRequest("Reporting user is unavailable"));
        mvc.perform(post("/api/v1/occurrences").contentType(MediaType.APPLICATION_JSON).content(json(2, unknown, null))).andExpect(status().isBadRequest());
        verifyNoInteractions(sender);
    }
}
