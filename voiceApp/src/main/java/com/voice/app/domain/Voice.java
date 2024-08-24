	
package com.voice.app.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "voice")
public class Voice {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "voice_seq")
	@SequenceGenerator(name = "voice_seq", sequenceName = "voice_seq", allocationSize = 1)
	private long id;
		
	@Column(length = 50, nullable = false)
	private String tit;
	
	@Column(length = 300, nullable = false)
	private String fileName;
	
	@Column(nullable = false)
	private Integer per;
	
	@Lob
	@Column(nullable = true)
	private String data;
	
	@Column(name = "create_date")
	@CreatedDate
	private LocalDateTime createDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;
}
