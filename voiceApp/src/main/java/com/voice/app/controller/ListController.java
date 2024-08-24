package com.voice.app.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.voice.app.domain.Voice;
import com.voice.app.service.VoiceService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ListController {

    private final VoiceService voiceService;

//    @GetMapping("/list")
//    public String list(Model model,
//                       @RequestParam(value="page", defaultValue="0") int page,
//                       @RequestParam(value="size", defaultValue="5") int size,
//                       @RequestParam(value="risk", required=false) String risk,
//                       Authentication authentication) {
//        String username = authentication.getName();
//        Page<Voice> paging = voiceService.getVoicesByUsernameAndRisk(username, risk, page, size);
//        model.addAttribute("paging", paging);
//        model.addAttribute("username", username);
//        model.addAttribute("currentRisk", risk);
//
//        List<Integer> pageNumbers = getPageNumbers(page, paging.getTotalPages());
//        model.addAttribute("pageNumbers", pageNumbers);
//
//        return "main/list";
//    }

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="size", defaultValue="5") int size,
                       @RequestParam(value="risk", required=false) String risk,
                       @RequestParam(value="search", required=false) String searchTerm,
                       Authentication authentication) {
        String username = authentication.getName();
        List<Voice> voices;
        long totalCount;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            voices = voiceService.searchVoices(username, searchTerm, risk, page, size);
            totalCount = voiceService.countSearchVoices(username, searchTerm, risk);
        } else {
            voices = voiceService.getVoicesByUsernameAndRisk(username, risk, page, size);
            totalCount = voiceService.countVoicesByUsernameAndRisk(username, risk);
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);
        model.addAttribute("voices", voices);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentRisk", risk);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("size", size);
        model.addAttribute("searchTerm", searchTerm);
        List<Integer> pageNumbers = getPageNumbers(page, totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        return "main/list";
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
    
    @DeleteMapping("/voice/delete/{id}")
    public ResponseEntity<?> deleteVoice(@PathVariable("id") Long id) {
        Voice voice = voiceService.getVoiceById(id);
        if (voice == null) {
            return ResponseEntity.notFound().build();
        }
        voiceService.deleteVoice(id);
        return ResponseEntity.ok().build();
    }
}