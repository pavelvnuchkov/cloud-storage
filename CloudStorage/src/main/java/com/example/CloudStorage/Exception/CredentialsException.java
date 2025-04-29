package com.example.CloudStorage.Exception;

import com.example.CloudStorage.dto.ExceptionDto;

import javax.naming.AuthenticationException;

public class CredentialsException extends AuthenticationException {
    public CredentialsException(ExceptionDto exceptionDto) {
        super(exceptionDto.toString());
    }
}
