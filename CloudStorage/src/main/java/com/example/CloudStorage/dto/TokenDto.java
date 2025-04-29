package com.example.CloudStorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor

@Getter
@Setter
@ToString

public class TokenDto {
    @JsonProperty("auth-token")
    private String token;


    private String id;

    public TokenDto(String token, String id) {
        this.token = token;
        this.id = id;
    }

    public TokenDto(String token) {
        this.token = token;
    }
}
