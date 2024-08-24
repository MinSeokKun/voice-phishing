package com.voice.app.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	@SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1)
	private Long id;
	
	@Column(length = 30 , nullable = false)
	private String name;
	
	//아이디
	@Column(length = 20, unique = true, nullable = false)
	private String username;
	
	@Column(length = 100, nullable = false)
	private String password;
	
	//이메일
	@Column(length = 100, unique = true, nullable = false)
	private String email;

	// apiKey
	@Column(unique = true)
	private String apiKey;
	
	@Column(name = "create_date")
	@CreatedDate
	private LocalDateTime createDate;

	private String role;
}
