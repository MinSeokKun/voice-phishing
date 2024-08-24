package com.voice.app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voice.app.domain.Member;
import com.voice.app.domain.Voice;
import com.voice.app.repository.MemberRepository;
import com.voice.app.repository.VoiceRepository;
@Service
public class VoiceService {
    @Autowired
    private VoiceRepository voiceRepository;
    @Autowired
    private MemberRepository memberRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(VoiceService.class);
    
    // 검사 결과 데이터 저장
    @Transactional
    public Voice saveVoiceResult(Voice voice, String username) {
    	Member member = memberRepository.findByUsername(username)
    			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));;
    			logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + member);
    	voice.setCreateDate(LocalDateTime.now());
    	voice.setMember(member);
    	return voiceRepository.save(voice);
    }
    
    // 검사 목록
    public Page<Voice> getList(int page) {
    	Pageable pageable = PageRequest.of(page, 10);
    	return this.voiceRepository.findAll(pageable);
    }
    
    // risk를 포함한 검색
    public List<Voice> getVoicesByUsernameAndRisk(String username, String risk, int page, int size) {
    	Member member = memberRepository.findByUsername(username)
    			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));;
        int startRow = page * size;
        int endRow = (page + 1) * size;
        int[] perRange = getRiskPerRange(risk);

        return voiceRepository.findByMemberAndRiskWithPagination(member.getId(), perRange[0], perRange[1], startRow, endRow);
    }

    public long countVoicesByUsernameAndRisk(String username, String risk) {
        int[] perRange = getRiskPerRange(risk);
        Member member = memberRepository.findByUsername(username)
    			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));;
        return voiceRepository.countByMemberAndRisk(member.getId(), perRange[0], perRange[1]);
    }

    private int[] getRiskPerRange(String risk) {
        if (risk == null) {
            return new int[]{0, 100};
        }
        switch (risk) {
            case "low":
                return new int[]{0, 39};
            case "medium":
                return new int[]{40, 74};
            case "high":
                return new int[]{75, 100};
            default:
                return new int[]{0, 100};
        }
    }


    
    public Page<Voice> getVoicesByUsername(String username, int page, int size) {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        int startRow = page * size;
        int endRow = (page + 1) * size;
        List<Voice> voices = voiceRepository.findByMemberWithPagination(member.getId(), startRow, endRow);
        long total = voiceRepository.countByMember(member);
        return new PageImpl<>(voices, PageRequest.of(page, size), total);
    }

    public Voice getVoiceById(Long id) {
        return voiceRepository.findById(id).orElse(null);
    }

    public void deleteVoice(Long id) {
        voiceRepository.deleteById(id);
    }
    
 // 관리자가 모든 voice 정보 조회 (검색 기능 포함)
    public Page<Voice> getAllVoices(String kw, int page, int size) {
        long startRow = (long) page * size;
        long endRow = startRow + size;
        
        List<Voice> voices = voiceRepository.findAllWithPaginationAndKeyword(startRow, endRow, kw);
        
        long total = voiceRepository.countByKeyword(kw); // 검색 결과의 전체 voice 수를 가져옵니다.
        
        return new PageImpl<>(
            voices,
            PageRequest.of(page, size, Sort.by("createDate").descending()),
            total
        );
    }
    
    public List<Voice> searchVoices(String username, String searchTerm, String risk, int page, int size) {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        int startRow = page * size;
        int endRow = (page + 1) * size;
        int[] perRange = getRiskPerRange(risk);

        return voiceRepository.searchVoices(member.getId(), searchTerm, perRange[0], perRange[1], startRow, endRow);
    }

    public long countSearchVoices(String username, String searchTerm, String risk) {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        int[] perRange = getRiskPerRange(risk);

        return voiceRepository.countSearchVoices(member.getId(), searchTerm, perRange[0], perRange[1]);
    }
}