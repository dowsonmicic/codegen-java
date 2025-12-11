package com.dowson.codegen.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(0).msg("success").data(data).build();
    }

    public static Result<Void> success() {
        return Result.<Void>builder().code(0).msg("success").build();
    }

    public static <T> Result<T> failure(int code, String msg) {
        return Result.<T>builder().code(code).msg(msg).build();
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        return Result.<T>builder().code(code).msg(msg).data(data).build();
    }

    public static <T> Result<T> failure(String msg) {
        return Result.<T>builder().code(-1).msg(msg).build();
    }
}

