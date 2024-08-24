package com.voice.app.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDTO {
    private String username;

    private String name;

    private String password;

    private String email;

    private String phoneNumber;

    private Integer gender;

    private Integer nation;

    private LocalDate birth;
}
