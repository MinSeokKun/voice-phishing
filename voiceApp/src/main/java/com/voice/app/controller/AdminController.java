package com.voice.app.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.voice.app.domain.Voice;
import com.voice.app.dto.MemberAdminDTO;
import com.voice.app.service.MemberService;
import com.voice.app.service.VoiceService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class AdminController {

	private final VoiceService voiceService;
	private final MemberService memberService;
		
	@DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVoice(@PathVariable("id") Long id) {
        Voice voice = voiceService.getVoiceById(id);
        if (voice == null) {
            return ResponseEntity.notFound().build();
        }
        voiceService.deleteVoice(id);
        return ResponseEntity.ok().build();
    }
	
	@GetMapping("/voices")
	public String list(Model model, 
					@RequestParam(value="kw", defaultValue="0") String kw, 
	                @RequestParam(value="page", defaultValue="0") int page, 
	                @RequestParam(value="size", defaultValue="5") int size) {
	    Page<Voice> paging = voiceService.getAllVoices(kw, page, size);
	    long totalCount = paging.getTotalElements();
	    
	    model.addAttribute("paging", paging);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", paging.getTotalPages());
	    model.addAttribute("totalCount", totalCount);
	    model.addAttribute("size", size);
	    
	    List<Integer> pageNumbers = getPageNumbers(page, paging.getTotalPages());
	    model.addAttribute("pageNumbers", pageNumbers);
	    
	    return "admin/voices";
	}
	
	@GetMapping("/members")
	public String getMembers(
	    @RequestParam(value = "page", defaultValue = "0") int page,
	    @RequestParam(value = "size", defaultValue = "10") int size,
	    @RequestParam(value = "kw", defaultValue = "") String kw,
	    Model model) {
	    Page<MemberAdminDTO> paging = memberService.getAllMembersPaged(kw, page, size);
	    long totalElements = paging.getTotalElements();
	    
	    model.addAttribute("paging", paging);
	    model.addAttribute("kw", kw);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalCount", totalElements);
	    model.addAttribute("size", size);
	    
	    int totalPages = paging.getTotalPages();
	    if (totalPages > 0) {
	        List<Integer> pageNumbers = getPageNumbers(page, totalPages);
	        model.addAttribute("pageNumbers", pageNumbers);
	    }
	    return "admin/members";
	}

	private List<Integer> getPageNumbers(int currentPage, int totalPages) {
	    List<Integer> pageNumbers = new ArrayList<>();
	    
	    int startPage = Math.max(0, currentPage - 1);
	    int endPage = Math.min(totalPages - 1, startPage + 2);
	    
	    // 시작 페이지가 0이 아니라면, 시작 페이지를 조정합니다.
	    if (startPage > 0) {
	        startPage = Math.max(0, endPage - 2);
	    }
	    
	    for (int i = startPage; i <= endPage; i++) {
	        pageNumbers.add(i);
	    }
	    
	    return pageNumbers;
	}
}