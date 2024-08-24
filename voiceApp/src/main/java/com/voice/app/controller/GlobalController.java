package com.voice.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.voice.app.dto.MemberDTO;
import com.voice.app.service.MemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalController {
	private final MemberService memberService;
	
	@ModelAttribute("loggedInUser")
    public MemberDTO userInfo(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return memberService.getUserInfo(authentication.getName());
        }
        return null;
    }
}
