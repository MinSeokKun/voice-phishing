package com.voice.app;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.voice.app.domain.Member;
import com.voice.app.domain.Voice;
import com.voice.app.repository.MemberRepository;
import com.voice.app.repository.VoiceRepository;
import com.voice.app.service.VoiceService;

@SpringBootTest
class VoiceAppApplicationTests {
	
	@Autowired
	private VoiceRepository voiceRepository;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private VoiceService voiceService;
	
	//@Test
	public void serviceSave() {
		Voice vo = new Voice();
		Member mem = memberRepository.getById(1);
		vo.setId(2);
		vo.setCreateDate(LocalDateTime.now());
		vo.setFileName("test2.wav");
		vo.setTit("test2");
		vo.setPer(20);
		vo.setData("테스트입니다.");
		vo.setMember(mem);
		
//		Voice save = voiceService.saveVoiceResult(vo);
	}
	
	//@Test
	public void voiceSave() {
		Voice vo = new Voice();
		Member mem = memberRepository.getById(1);
		vo.setId(1L);
		vo.setTit("test");
		vo.setFileName("test.wav");
		vo.setCreateDate(LocalDateTime.now());
		vo.setPer(80);
		vo.setMember(mem);
		vo.setData("test입니다.");
		
		voiceRepository.save(vo);
	}
		
}
