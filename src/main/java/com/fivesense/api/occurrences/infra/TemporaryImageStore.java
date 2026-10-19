package com.fivesense.api.occurrences.infra;

import com.fivesense.api.shared.error.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final long maxBytes;

    public TemporaryImageStore(@Value("${occurrences.temp-image-dir:${java.io.tmpdir}/fivesense-occurrence-images}") String directory,
                               @Value("${spring.servlet.multipart.max-file-size:5MB}") DataSize maxSize){
        this.directory=Path.of(directory);
        this.maxBytes=maxSize.toBytes();
    }

    @PostConstruct
    void ensureDirectory(){
        try { Files.createDirectories(directory); }
        catch (IOException ex){ throw new IllegalStateException("Unable to create temporary image directory",ex); }
    }

    /** Maximum size, in bytes, accepted for a single image. */
    public long maxBytes(){ return maxBytes; }

    /**
     * Persists the uploaded bytes to a temporary file and returns an opaque id. The stored name gets
     * the extension that matches the real image type (detected from the content), so the e-mail
     * attachment is delivered as an image even when the client sent no or a wrong file name.
     */
    public UUID store(InputStream content,String originalFilename){
        byte[] bytes;
        try { bytes=content.readNBytes((int)Math.min(maxBytes+1,Integer.MAX_VALUE-8)); }
        catch (IOException ex){ throw ApiException.badRequest("Unable to read the uploaded image"); }
        if(bytes.length==0)throw ApiException.badRequest("Image content is empty");
        if(bytes.length>maxBytes)throw new ApiException(HttpStatus.CONTENT_TOO_LARGE,"Image exceeds the maximum size of "+DataSize.ofBytes(maxBytes).toMegabytes()+"MB");
        UUID imageId=UUID.randomUUID();
        try {
            Files.write(pathFor(imageId),bytes);
            Files.writeString(namePathFor(imageId),normalizeName(originalFilename,bytes),StandardCharsets.UTF_8);
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

    private static String normalizeName(String name,byte[] content){
        String clean=sanitizeName(name);
        String detected=detectExtension(content);
        if(detected==null)return clean;
        int dot=clean.lastIndexOf('.');
        String current=dot>0?clean.substring(dot+1).toLowerCase():"";
        if(current.equals(detected)||(detected.equals("jpg")&&current.equals("jpeg")))return clean;
        return (dot>0?clean.substring(0,dot):clean)+"."+detected;
    }

    /** Keeps only the file name (no directories) and replaces characters unsafe for file names and mail headers. */
    private static String sanitizeName(String name){
        if(name==null||name.isBlank())return DEFAULT_NAME;
        String cleaned=name.substring(Math.max(name.lastIndexOf('/'),name.lastIndexOf('\\'))+1).replaceAll("[^\\p{L}\\p{N}._ -]","_").trim();
        return cleaned.isBlank()||cleaned.chars().allMatch(c->c=='.'||c=='_')?DEFAULT_NAME:cleaned;
    }

    private static String detectExtension(byte[] b){
        if(startsWith(b,0x89,'P','N','G'))return "png";
        if(startsWith(b,0xFF,0xD8,0xFF))return "jpg";
        if(startsWith(b,'G','I','F','8'))return "gif";
        if(b.length>=12&&startsWith(b,'R','I','F','F')&&b[8]=='W'&&b[9]=='E'&&b[10]=='B'&&b[11]=='P')return "webp";
        return null;
    }

    private static boolean startsWith(byte[] content,int... signature){
        if(content.length<signature.length)return false;
        for(int i=0;i<signature.length;i++)if((content[i]&0xFF)!=signature[i])return false;
        return true;
    }

    private Path pathFor(UUID imageId){ return directory.resolve(imageId.toString()); }
    private Path namePathFor(UUID imageId){ return directory.resolve(imageId.toString()+".name"); }

    public record StoredImage(Path path,String originalName){}
}
