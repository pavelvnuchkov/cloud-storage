package com.example.CloudStorage.dto;

public class ExceptionDto {
    private String message;
    private int id;

    public ExceptionDto(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "message:'" + message + '\'' +
                ", 'id:" + id + '\'';
    }
}
