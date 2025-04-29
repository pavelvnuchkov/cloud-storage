package com.example.CloudStorage.Exception;

import com.example.CloudStorage.dto.ExceptionDto;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(ExceptionDto exceptionDto) {
        super(exceptionDto.toString());
    }
}
