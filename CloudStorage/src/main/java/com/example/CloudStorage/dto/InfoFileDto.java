package com.example.CloudStorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoFileDto {

    @JsonProperty("filename")
    private String name;

    private Long size;

    public InfoFileDto() {
    }

    public InfoFileDto(String name, Long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "InfoFileDto{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}
