package com.example.CloudStorage.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token_blocked")
public class Token {

    @Id
    private String id;

    private String token;

    public Token() {
    }

    public Token(String id, String token) {
        this.id = id;
        this.token = token;
    }
}
