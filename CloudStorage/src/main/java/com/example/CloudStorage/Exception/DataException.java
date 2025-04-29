package com.example.CloudStorage.Exception;

import com.example.CloudStorage.dto.ExceptionDto;

public class DataException extends RuntimeException {
    public DataException(ExceptionDto exceptionDto) {
        super(exceptionDto.toString());
    }
}
