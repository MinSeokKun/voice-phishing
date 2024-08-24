package com.voice.app.jwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.voice.app.domain.Member;
import com.voice.app.dto.MemberDetails;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final List<AntPathRequestMatcher> permitAllMatchers;

    public JWTFilter(JWTUtil jwtUtil, List<AntPathRequestMatcher> permitAllMatchers) {
        this.jwtUtil = jwtUtil;
        this.permitAllMatchers = permitAllMatchers;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
//        System.out.println("JWTFilter의 doFilterInternal");
        
        // 쿠키에서 access 토큰을 찾음
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
//                    System.out.println("Found access token in cookie: " + accessToken);
                    break;
                }
            }
        }
        
        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
//            System.out.println("JWTFilter의 doFilterInternal > accessToken이 없음");
//            System.out.println(Arrays.toString(request.getCookies()));
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료시 재발급 시도
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
//            System.out.println("JWTFilter의 doFilterInternal > isExpired, trying to reissue");
            String newAccessToken = jwtUtil.reissueToken(request, response);
            if (newAccessToken != null) {
                // 새 토큰으로 인증 처리를 계속합니다.
                accessToken = newAccessToken;
            } else {
                // 재발급 실패 시 에러 응답
                PrintWriter writer = response.getWriter();
                writer.print("access token expired and reissue failed");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
//            System.out.println("JWTFilter의 doFilterInternal > category가 access");
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Member userEntity = new Member();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        MemberDetails customUserDetails = new MemberDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
    
}