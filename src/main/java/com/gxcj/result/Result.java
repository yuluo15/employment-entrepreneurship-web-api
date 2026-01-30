package com.gxcj.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private String message;
    private T data;


    public static <T> Result<T> success(T data){
        return new Result<>("success", data);
    }

    public static <T> Result<T> success(){
        return new Result<>("success", null);
    }

    public static <T> Result<T> fail(String message){
        return new Result<>(message, null);
    }
}
