package com.fivesense.api.shared.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApi(ApiException ex){
        log.warn("Request rejected ({}): {}",ex.getStatus().value(),ex.getMessage());
        return problem(ex.getStatus(),ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        var errors=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList();
        log.warn("Request validation failed: {}",errors);
        var p=problem(HttpStatus.BAD_REQUEST,"Request validation failed");
        p.setProperty("errors",errors);
        return p;
    }
    /** Form (multipart) binding and validation failures, e.g. a malformed UUID or a non-positive quantity. */
    @ExceptionHandler(BindException.class)
    ProblemDetail handleBinding(BindException ex){
        var errors=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList();
        log.warn("Request binding failed: {}",errors);
        var p=problem(HttpStatus.BAD_REQUEST,"Request validation failed");
        p.setProperty("errors",errors);
        return p;
    }
    /** The uploaded image(s) exceeded the configured multipart limit; thrown before the controller runs. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleUploadTooLarge(MaxUploadSizeExceededException ex){
        log.warn("Upload rejected: payload exceeds the multipart size limit");
        return problem(HttpStatus.PAYLOAD_TOO_LARGE,"The image is too large. Reduce the photo size and try again.");
    }
    /** Any other multipart parsing failure (malformed body, wrong content type, etc.). */
    @ExceptionHandler(MultipartException.class)
    ProblemDetail handleMultipart(MultipartException ex){
        log.warn("Multipart request could not be parsed: {}",ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST,"The image upload could not be read. Check the request format.");
    }
    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNotFound(NoResourceFoundException ex){return problem(HttpStatus.NOT_FOUND,"Resource not found");}
    /** Anything not mapped above: log the full stack trace and return a generic 500 instead of a silent failure. */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex){
        log.error("Unexpected error handling request",ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error");
    }
    private ProblemDetail problem(HttpStatus status,String detail){var p=ProblemDetail.forStatusAndDetail(status,detail);p.setType(URI.create("about:blank"));return p;}
}
