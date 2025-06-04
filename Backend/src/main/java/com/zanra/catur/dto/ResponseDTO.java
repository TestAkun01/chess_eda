package com.zanra.catur.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    private Integer status;
    private String error;
    private String message;
    private T data;

    public static <T> ResponseDTO<T> error(HttpStatus status, String message) {
        return new ResponseDTO<>(
                status.value(),
                status.getReasonPhrase(),
                message,
                null);
    }

    public static <T> ResponseDTO<T> success(String message) {
        return new ResponseDTO<>(
                HttpStatus.OK.value(),
                null,
                message,
                null);
    }

    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>(
                HttpStatus.OK.value(),
                null,
                message,
                data);
    }
}