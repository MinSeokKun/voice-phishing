package com.voice.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voice.app.domain.Member;
import com.voice.app.dto.MemberAdminDTO;
import com.voice.app.dto.MemberDTO;
import com.voice.app.dto.SignupDTO;
import com.voice.app.exception.UserNotFoundException;
import com.voice.app.repository.MemberRepository;
import com.voice.app.repository.VoiceRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final VoiceRepository voiceRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Transactional
    public void deleteUser(String username) {
        try {
            Member user = memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
            voiceRepository.deleteAllByMemberId(user.getId());
            memberRepository.delete(user);
        } catch (Exception e) {
            log.error("Error deleting user: " + username, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
    
    public Member getMember(String username) {
        return memberRepository.findByUsername(username).orElse(null);
    }
    
    public void signupProcess(SignupDTO signupDTO) {
        String username = signupDTO.getUsername();
        if (memberRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        Member member = new Member();
        member.setUsername(username);
        member.setName(signupDTO.getName());
        member.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        member.setEmail(signupDTO.getEmail());
        member.setCreateDate(LocalDateTime.now());
        
        if (username.equalsIgnoreCase("ADMIN")) {
            member.setRole("ROLE_ADMIN");
        } else {
            member.setRole("ROLE_USER");
        }

        memberRepository.save(member);
    }
    
    public boolean checkUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public void modify(Member m, String username, String password, String name, String email) {
        m.setUsername(username);
        if (password != null)
            m.setPassword(passwordEncoder.encode(password));
        m.setName(name);
        m.setEmail(email);
        memberRepository.save(m);
}
    
    // 관리자가 모든 회원정보 조회
    public Page<MemberAdminDTO> getAllMembersPaged(String kw, int page, int size) {
        long startRow = (long) page * size;
        long endRow = startRow + size;
        
        List<Member> members = memberRepository.findAllWithPaginationAndKeyword(startRow, endRow, kw);
        
        long total = memberRepository.countByKeyword(kw); // 검색 결과의 전체 회원 수를 가져옵니다.
        
        return new PageImpl<>(
            members.stream().map(MemberAdminDTO::fromEntity).collect(Collectors.toList()),
            PageRequest.of(page, size),
            total
        );
    }
    
    public MemberDTO getUserInfo(String username) {
        return memberRepository.findByUsername(username)
            .map(MemberDTO::fromEntity)
            .orElseThrow(() -> new UserNotFoundException("유저 정보를 찾아올 수 없습니다"));
    }
    
    public MemberDTO updateUserInfo(String username, MemberDTO updateInfo) {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("유저 정보를 찾아올 수 없습니다"));
        
        member.setName(updateInfo.getName());
        
        Member updatedMember = memberRepository.save(member);
        return MemberDTO.fromEntity(updatedMember);
    }

    public String generateApiKey(String username) {
        
            Optional<Member> member = memberRepository.findByUsername(username);
            
            String apiKey = UUID.randomUUID().toString();
            member.get().setApiKey(apiKey);
            memberRepository.save(member.get());
            return apiKey;
    }

    public boolean validateApiKey(String apiKey) {
		if (apiKey == null || apiKey.isEmpty()) {
			return false;
		}
		return memberRepository.findByApiKey(apiKey).isPresent();
	}
	
	public boolean hasValidApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey).isPresent();
    }
	
	public String getApiKey(String username) {
        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member != null) {
            return member.getApiKey();
        }
        return null;
    }
	
	public boolean hasApiKey(String username) {
        Member member = memberRepository.findByUsername(username).orElse(null);
        return member != null && member.getApiKey() != null && !member.getApiKey().isEmpty();
    }
	
	public Member findByApiKey(String apiKey) {
		Optional<Member> member = this.memberRepository.findByApiKey(apiKey);
		if (member.isPresent()) {
            return member.get();
        } else {
            throw new RuntimeException("User not found with apiKey: " + apiKey);
        }
		
	}

    
}
