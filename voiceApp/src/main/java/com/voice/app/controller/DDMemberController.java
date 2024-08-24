package com.voice.app.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.voice.app.domain.Member;
import com.voice.app.service.MemberService;
import com.voice.app.stt.TutorialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
@Slf4j
public class DDMemberController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final TutorialService tutorialService;

    @GetMapping("/modify")
	public String ViewModifyForm(Model model, Principal pc) {
		String memberId = pc.getName();
		Member member = memberService.getMember(memberId);
		model.addAttribute("memberForm", member);
		return "dadeum/modify_form";
	}

	@PostMapping("/modify")
	public String ProModifyForm(@ModelAttribute("memberForm") Member memberForm, @RequestParam("username") String username,
			@RequestParam("password") String password, @RequestParam("name") String name, @RequestParam("email") String email) {
                memberService.modify(memberForm, username, password, name, email);
		return "redirect:/members/mypage";
	}
    
    //마이페이지 그래프
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/mypage")
	public String mypage(
            Model model,
            Principal principal,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
	    
	    if (startDate == null) {
	        startDate = LocalDateTime.of(1999, 1, 1, 0, 0, 0);
	    }
	    if (endDate == null) {
	        endDate = LocalDateTime.now();
	    }
	    
	    String username = principal.getName();
	    Member member = memberService.getMember(username);
	    
	    // 회원 정보 설정
	    model.addAttribute("MemberId", member.getId());
	    model.addAttribute("MemberName", member.getName());
	    model.addAttribute("MemberEmail", member.getEmail());
	    model.addAttribute("apiKey", member.getApiKey());
	    
	    // 월별 사용량 데이터 가져오기
	    Map<YearMonth, Map<String, Double>> monthlyUsage = tutorialService.getMonthlyUsageForMember(member.getId(), startDate, endDate);
	    model.addAttribute("monthlyUsage", monthlyUsage);
	    
	    // Open API 총 사용 시간 계산
	    double totalOpenApiUsage = monthlyUsage.values().stream()
	            .mapToDouble(m -> m.getOrDefault("Open API 사용", 0.0))
	            .sum();
	    
	    // 시간으로 변환하고 소수점 둘째 자리까지 반올림
	    String formattedTotalOpenApiUsage = String.format("%.2f", totalOpenApiUsage / 60);
	    model.addAttribute("totalOpenApiUsage", formattedTotalOpenApiUsage);
	    return "dadeum/dd-mypage";
	}

	// api키발급
	@PostMapping("/generate-api-key")
	public String generateApiKey(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
		String apiKey = memberService.generateApiKey(authentication.getName());
		redirectAttributes.addFlashAttribute("apiKey", apiKey);
		
		return "redirect:/members/mypage";
	}
}
