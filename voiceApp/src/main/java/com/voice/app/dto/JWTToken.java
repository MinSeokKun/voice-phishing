package com.voice.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTToken {
    private String token;

    public JWTToken(String token) {
        this.token = token;
    }
}
