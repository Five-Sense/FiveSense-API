package com.fivesense.api.shared.error;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status,String message){super(message);this.status=status;}
    public HttpStatus getStatus(){return status;}
    public static ApiException notFound(String resource){return new ApiException(HttpStatus.NOT_FOUND,resource+" not found");}
    public static ApiException conflict(String message){return new ApiException(HttpStatus.CONFLICT,message);}
    public static ApiException badRequest(String message){return new ApiException(HttpStatus.BAD_REQUEST,message);}
    public static ApiException forbidden(){return new ApiException(HttpStatus.FORBIDDEN,"Operation is not allowed");}
}
