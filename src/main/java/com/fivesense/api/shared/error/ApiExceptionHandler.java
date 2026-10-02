package com.fivesense.api.shared.error;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApi(ApiException ex){return problem(ex.getStatus(),ex.getMessage());}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        var p=problem(HttpStatus.BAD_REQUEST,"Request validation failed");
        p.setProperty("errors",ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList());
        return p;
    }
    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNotFound(NoResourceFoundException ex){return problem(HttpStatus.NOT_FOUND,"Resource not found");}
    private ProblemDetail problem(HttpStatus status,String detail){var p=ProblemDetail.forStatusAndDetail(status,detail);p.setType(URI.create("about:blank"));return p;}
}
