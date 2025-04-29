package com.example.CloudStorage.Exception;

import com.example.CloudStorage.dto.ExceptionDto;

public class ServerException extends RuntimeException {
    public ServerException(ExceptionDto exceptionDto) {
        super(exceptionDto.toString());
    }
}
