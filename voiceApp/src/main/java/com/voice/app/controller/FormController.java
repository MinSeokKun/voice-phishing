package com.voice.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class FormController {

	// 회원가입 페이지
    @GetMapping("/sign-up")
    public String signUp(Model model, HttpSession session) {
        boolean serviceTermsAgreed = session.getAttribute("serviceTermsAgreed") != null 
                                     && (boolean) session.getAttribute("serviceTermsAgreed");
        boolean privacyTermsAgreed = session.getAttribute("privacyTermsAgreed") != null 
                                     && (boolean) session.getAttribute("privacyTermsAgreed");
        boolean thirdPartyTermsAgreed = session.getAttribute("thirdPartyTermsAgreed") != null 
                                        && (boolean) session.getAttribute("thirdPartyTermsAgreed");
                
        model.addAttribute("serviceTermsAgreed", serviceTermsAgreed);
        model.addAttribute("privacyTermsAgreed", privacyTermsAgreed);
        model.addAttribute("thirdPartyTermsAgreed", thirdPartyTermsAgreed);
        
        return "user/personal-info";
    }
    
    @GetMapping("/sign-up1")
    public String signUp() {
        return "user/sign-up";
    }
    
    
    // 개인정보처리 상세 페이지
    @GetMapping("/personal-info1")
    public String personalInfo1() {
        return "user/personal-info1";
    }

    @PostMapping("/personal-info1/agree")
    public String agreePersonalInfo1(HttpSession session) {
        session.setAttribute("serviceTermsAgreed", true);
        return "redirect:/member/sign-up";
    }

    @GetMapping("/personal-info2")
    public String personalInfo2() {
        return "user/personal-info2";
    }

    @PostMapping("/personal-info2/agree")
    public String agreePersonalInfo2(HttpSession session) {
        session.setAttribute("privacyTermsAgreed", true);
        return "redirect:/member/sign-up";
    }

    @GetMapping("/personal-info3")
    public String personalInfo3() {
        return "user/personal-info3";
    }

    @PostMapping("/personal-info3/agree")
    public String agreePersonalInfo3(HttpSession session) {
        session.setAttribute("thirdPartyTermsAgreed", true);
        return "redirect:/member/sign-up";
    }
    
    // 로그인 페이지
    @GetMapping("/login")
    public String login(Authentication authentication, HttpSession session) {
    	if (authentication != null)
    		return "redirect:/";
    	session.removeAttribute("serviceTermsAgreed");
        session.removeAttribute("privacyTermsAgreed");
        session.removeAttribute("thirdPartyTermsAgreed");
        return "user/login";
    }
    
    @GetMapping("/mypage")
    public String mypage() {
    	return "user/mypage";
    }
    
    
}
