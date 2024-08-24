package com.voice.app.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.voice.app.domain.Voice;
import com.voice.app.service.VoiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VoiceController {
	private final VoiceService voiceService;
//	private final MemberService memberService;
	
	@PostMapping("/api/voice/save")
	public ResponseEntity<Voice> saveVoiceResult(@RequestBody Voice voice, Authentication authentication){
		String username = authentication.getName();
		Voice savedVoice = voiceService.saveVoiceResult(voice, username);
		return ResponseEntity.ok(savedVoice);
	}
}