package com.voice.app.exception;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("error", "접근이 거부되었습니다.");
        modelAndView.addObject("message", accessDeniedException.getMessage());
        request.setAttribute("javax.servlet.error.status_code", HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/error").forward(request, response);
    }
}