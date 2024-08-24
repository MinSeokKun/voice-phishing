package com.voice.app.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.voice.app.domain.Member;
import com.voice.app.dto.MemberDetails;
import com.voice.app.repository.MemberRepository;

@Service
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public MemberDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (member != null) {
            return new MemberDetails(member);
        }

        return null;
    }

}
