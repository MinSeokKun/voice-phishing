package com.voice.app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "REFRESH_ENTITY")
public class RefreshEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REFRESH_SEQ")
    @SequenceGenerator(name = "REFRESH_SEQ", sequenceName = "REFRESH_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "USERNAME", length = 50)
    private String username;

    @Column(name = "REFRESH", length = 255)
    private String refresh;

    @Column(name = "EXPIRATION", length = 50)
    private String expiration;


}