package com.voice.app.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = null;
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("statusCode", statusCode);
            System.out.println(statusCode);
            
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "redirect:/member/login";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorMessage = "접근이 거부되었습니다.";
            } else if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorMessage = "페이지를 찾을 수 없습니다.";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorMessage = "서버 내부 오류가 발생했습니다.";
            } else {
                errorMessage = "알 수 없는 오류가 발생했습니다.";
            }
            
            model.addAttribute("errorMessage", errorMessage);
        }
        
        System.out.println(errorMessage);
        
        return "error/error";
    }
}