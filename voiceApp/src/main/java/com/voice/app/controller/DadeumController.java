package com.voice.app.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.voice.app.domain.Member;
import com.voice.app.domain.STTResult;
import com.voice.app.dto.MemberDetails;
import com.voice.app.service.MemberService;
import com.voice.app.stt.TutorialService;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("dadeum")
@RequiredArgsConstructor
public class DadeumController {

    private final TutorialService tutorialService;
    private final MemberService memberService;

    @GetMapping("/guide")
    public String guide() {
        return "dadeum/guide";
    }
    
    
    @GetMapping("/api-key-validation")
    public ResponseEntity<Boolean> validateApi(@RequestParam("apiKey") String apiKey) {
        boolean isValid = memberService.hasValidApiKey(apiKey);
        return ResponseEntity.ok(isValid);
    }
    
    @GetMapping("/UseSTT")
    public String UseStt(@AuthenticationPrincipal MemberDetails memberDetails,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "3") int size,
                        Model model, HttpSession session) {
        Member member = memberService.getMember(memberDetails.getUsername());
        model.addAttribute("member", member);
        System.out.println(member.getId());
        return "dadeum/usestt";
    }
    
    @PostMapping(value = "/transcribe/file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> transcribeFile(@RequestParam("file") MultipartFile file,
                                            Authentication authentication) {
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload");
            }

            // 인증된 사용자 정보 가져오기
            System.out.println("*********************************************" + authentication.getName());
            Member member = memberService.getMember(authentication.getName());
            Long memberId = member.getId();
            System.err.println("*******************" + memberId);

            // Transcribe 실행
            STTResult result = tutorialService.transcribeFile(file, memberId);

            if (result.getResultText() == null || result.getResultText().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No transcription result");
            }

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            // log.error("File processing error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File processing error: " + e.getMessage());
        } catch (InterruptedException e) {
            // log.error("Transcription process interrupted", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transcription process interrupted");
        } catch (Exception e) {
            // log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/results")
    public String getSTTResults(Authentication authentication,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "3") int size,
                                Model model,
                                HttpSession session) {
        // index 메서드의 기능
        String apiKey = (String) session.getAttribute("apiKey");
        Member member = memberService.getMember(authentication.getName());
        model.addAttribute("apiKey", apiKey);
        model.addAttribute("member", member);

        // getSTTResults 메서드의 기능
        Page<STTResult> resultPage = tutorialService.getSTTResultsByMemberNum(member.getId(), page, size);
        
        model.addAttribute("results", resultPage.getContent());
        model.addAttribute("currentPage", resultPage.getNumber());
        model.addAttribute("totalPages", Math.max(resultPage.getTotalPages(), 1));
        model.addAttribute("totalItems", resultPage.getTotalElements());
        model.addAttribute("size", size);
        
        return "dadeum/resultlist";  // resultlist 뷰를 반환
    }
    
}
