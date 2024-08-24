package com.voice.app.dto;

import com.voice.app.domain.Member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
	private String email;
	private String username;
	private String name;
//	private String password;
	
	public static MemberDTO fromEntity(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setUsername(member.getUsername());
        dto.setEmail(member.getEmail());
//        dto.setPassword(member.getPassword());
        dto.setName(member.getName());
        return dto;
    }
}
