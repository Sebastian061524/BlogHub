package com.sebastian.bloghub.exception;

public class BusinessException extends RuntimeException{

    private final int httpStatus;

    public int getHttpStatus() {
        return httpStatus;
    }

    public BusinessException(int httpStatus, String message){
        super(message);
        this.httpStatus = httpStatus;
    }

}
