package com.fivesense.api.occurrences.infra;

import com.fivesense.api.shared.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores occurrence images in temporary files only. The image bytes are never written to the
 * database: they live on disk until the occurrence e-mail is sent, and are deleted right after.
 * The caller receives an opaque image id used to retrieve and discard the file later.
 */
@Component
public class TemporaryImageStore {
    private static final Logger log=LoggerFactory.getLogger(TemporaryImageStore.class);
    private static final String DEFAULT_NAME="imagem";
    private final Path directory;

    public TemporaryImageStore(@Value("${occurrences.temp-image-dir:${java.io.tmpdir}/fivesense-occurrence-images}") String directory){
        this.directory=Path.of(directory);
    }

    @PostConstruct
    void ensureDirectory(){
        try { Files.createDirectories(directory); }
        catch (IOException ex){ throw new IllegalStateException("Unable to create temporary image directory",ex); }
    }

    /** Persists the uploaded bytes to a temporary file and returns an opaque id. */
    public UUID store(InputStream content,String originalFilename){
        UUID imageId=UUID.randomUUID();
        try {
            Files.copy(content,pathFor(imageId),StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(namePathFor(imageId),sanitizeName(originalFilename),StandardCharsets.UTF_8);
        } catch (IOException ex){ delete(imageId); throw ApiException.badRequest("Unable to store the uploaded image"); }
        return imageId;
    }

    /** Returns the stored temporary image (file path and original name) when it still exists. */
    public Optional<StoredImage> locate(UUID imageId){
        if(imageId==null)return Optional.empty();
        Path target=pathFor(imageId);
        if(!Files.exists(target))return Optional.empty();
        return Optional.of(new StoredImage(target,readName(imageId)));
    }

    /** Deletes the temporary image and its metadata; silent when already gone. */
    public void delete(UUID imageId){
        if(imageId==null)return;
        try { Files.deleteIfExists(pathFor(imageId)); Files.deleteIfExists(namePathFor(imageId)); }
        catch (IOException ex){ log.warn("Unable to delete temporary occurrence image"); }
    }

    private String readName(UUID imageId){
        try { return Files.exists(namePathFor(imageId))?Files.readString(namePathFor(imageId),StandardCharsets.UTF_8):DEFAULT_NAME; }
        catch (IOException ex){ return DEFAULT_NAME; }
    }

    private static String sanitizeName(String name){
        if(name==null||name.isBlank())return DEFAULT_NAME;
        String cleaned=Path.of(name).getFileName().toString().trim();
        return cleaned.isBlank()?DEFAULT_NAME:cleaned;
    }

    private Path pathFor(UUID imageId){ return directory.resolve(imageId.toString()); }
    private Path namePathFor(UUID imageId){ return directory.resolve(imageId.toString()+".name"); }

    public record StoredImage(Path path,String originalName){}
}
