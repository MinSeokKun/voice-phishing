package com.voice.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MainController {
	

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/")
	public String test() {
		return "main/main";
	}

	@GetMapping("/dadeum")
	public String dadeumMain() {
		return "dadeum/main";
	}
	
	
//	@GetMapping("/")
//	public String mainP() {
//		
//		return "Main Controller";
//	}
}
