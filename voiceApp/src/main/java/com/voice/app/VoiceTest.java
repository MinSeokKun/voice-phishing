package com.voice.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VoiceTest {
	
	
	@GetMapping("/minseok")
	public String testMin() {
		return "main/result";
	}
	
}
