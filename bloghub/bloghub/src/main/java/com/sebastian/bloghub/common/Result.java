package com.sebastian.bloghub.common;

import lombok.Data;

@Data
public class Result<T> {
    private int status;
    private String message;
    private T data;

    public Result(int status, String message, T data){
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String message, T data){
        return new Result<>(1, message, data);
    }

    public static Result<Void> error(String message){
        return new Result<>(0, message, null);
    }


}

