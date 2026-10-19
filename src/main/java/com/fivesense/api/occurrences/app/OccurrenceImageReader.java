package com.fivesense.api.occurrences.app;

import com.fivesense.api.occurrences.dto.OccurrenceDtos;
import com.fivesense.api.shared.error.ApiException;
import com.fivesense.api.shared.infra.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Turns the images sent with an occurrence (multipart files, base64 text or JSON items) into in-memory
 * images ready to be embedded in the notification e-mail. Nothing is written to disk or to the database.
 * Every image is validated: not empty, within the size limit, and a real PNG, JPEG, GIF or WebP
 * (detected from the content, not from the file name or declared type).
 */
@Component
public class OccurrenceImageReader {
    private static final String DEFAULT_NAME="imagem";
    private final long maxBytes;

    public OccurrenceImageReader(@Value("${spring.servlet.multipart.max-file-size:5MB}") DataSize maxSize){this.maxBytes=maxSize.toBytes();}

    /** Reads all sources in order (files, base64 texts, JSON items); {@code null} lists mean "none". */
    public List<EmailService.InlineImage> read(List<MultipartFile> files,List<String> encoded,List<OccurrenceDtos.ImageContent> items){
        int total=size(files)+size(encoded)+size(items);
        if(total>OccurrenceDtos.MAX_IMAGES)throw ApiException.badRequest("At most "+OccurrenceDtos.MAX_IMAGES+" images are allowed");
        List<EmailService.InlineImage> images=new ArrayList<>(total);
        if(files!=null)for(MultipartFile file:files)images.add(build(images.size(),file.getOriginalFilename(),bytesOf(file)));
        if(encoded!=null)for(String text:encoded)images.add(build(images.size(),null,decode(text)));
        if(items!=null)for(OccurrenceDtos.ImageContent item:items)images.add(build(images.size(),item.name(),decode(item.data())));
        return List.copyOf(images);
    }

    private static int size(List<?> values){return values==null?0:values.size();}

    private byte[] bytesOf(MultipartFile file){
        if(file.getSize()>maxBytes)throw tooLarge();
        try { return file.getBytes(); }
        catch (IOException ex){ throw ApiException.badRequest("Unable to read the uploaded image"); }
    }

    /** Accepts plain base64 (standard or URL-safe) or a {@code data:...;base64,} URI; whitespace and line breaks are ignored. */
    private byte[] decode(String data){
        String payload=data==null?"":data.strip();
        if(payload.startsWith("data:")){
            int comma=payload.indexOf(',');
            if(comma<0)throw ApiException.badRequest("Image content is not valid base64");
            payload=payload.substring(comma+1);
        }
        payload=payload.replaceAll("\\s","");
        if(payload.length()>maxBytes/3*4+8)throw tooLarge();
        try { return (payload.indexOf('-')>=0||payload.indexOf('_')>=0?Base64.getUrlDecoder():Base64.getDecoder()).decode(payload); }
        catch (IllegalArgumentException ex){ throw ApiException.badRequest("Image content is not valid base64"); }
    }

    private EmailService.InlineImage build(int index,String name,byte[] content){
        if(content.length==0)throw ApiException.badRequest("Image content is empty");
        if(content.length>maxBytes)throw tooLarge();
        ImageType type=ImageType.detect(content);
        if(type==null)throw ApiException.badRequest("Unsupported image type; use PNG, JPEG, GIF or WebP");
        return new EmailService.InlineImage("image"+(index+1),displayName(name,type),type.contentType,content);
    }

    private ApiException tooLarge(){return new ApiException(HttpStatus.CONTENT_TOO_LARGE,"Image exceeds the maximum size of "+DataSize.ofBytes(maxBytes).toMegabytes()+"MB");}

    /** Keeps only the file name (no directories, no control characters) and gives it the extension of the real type. */
    private static String displayName(String name,ImageType type){
        String clean=name==null?"":name.substring(Math.max(name.lastIndexOf('/'),name.lastIndexOf('\\'))+1).replaceAll("[^\\p{L}\\p{N}._ -]","_").trim();
        if(clean.isBlank()||clean.chars().allMatch(c->c=='.'||c=='_'))clean=DEFAULT_NAME;
        int dot=clean.lastIndexOf('.');
        String current=dot>0?clean.substring(dot+1).toLowerCase():"";
        if(type.extensions.contains(current))return clean;
        return (dot>0?clean.substring(0,dot):clean)+"."+type.extensions.get(0);
    }

    private enum ImageType {
        PNG("image/png",List.of("png")),
        JPEG("image/jpeg",List.of("jpg","jpeg")),
        GIF("image/gif",List.of("gif")),
        WEBP("image/webp",List.of("webp"));

        private final String contentType;
        private final List<String> extensions;
        ImageType(String contentType,List<String> extensions){this.contentType=contentType;this.extensions=extensions;}

        static ImageType detect(byte[] b){
            if(startsWith(b,0x89,'P','N','G'))return PNG;
            if(startsWith(b,0xFF,0xD8,0xFF))return JPEG;
            if(startsWith(b,'G','I','F','8'))return GIF;
            if(b.length>=12&&startsWith(b,'R','I','F','F')&&b[8]=='W'&&b[9]=='E'&&b[10]=='B'&&b[11]=='P')return WEBP;
            return null;
        }

        private static boolean startsWith(byte[] content,int... signature){
            if(content.length<signature.length)return false;
            for(int i=0;i<signature.length;i++)if((content[i]&0xFF)!=signature[i])return false;
            return true;
        }
    }
}
