package com.voice.app.dto;

import java.time.LocalDateTime;

import com.voice.app.domain.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAdminDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private LocalDateTime createDate;
    private String role;
    
    public static MemberAdminDTO fromEntity(Member member) {
        return MemberAdminDTO.builder()
            .id(member.getId())
            .name(member.getName())
            .username(member.getUsername())
            .email(member.getEmail())
            .createDate(member.getCreateDate())
            .role(member.getRole())
            .build();
    }
}