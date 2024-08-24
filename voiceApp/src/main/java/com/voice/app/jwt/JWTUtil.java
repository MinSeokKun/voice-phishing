package com.voice.app.jwt;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.voice.app.domain.RefreshEntity;
import com.voice.app.repository.RefreshRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTUtil {

    private SecretKey secretKey;

    private final RefreshRepository refreshRepository;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret, RefreshRepository refreshRepository) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshRepository = refreshRepository;
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            
            // 토큰 만료 확인
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey) // 알고리즘 명시
                .compact();
    }

    public String reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // Refresh 토큰을 쿠키에서 가져옵니다.
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return null; // Refresh 토큰이 없으면 재발급 불가
        }

        try {
            // Refresh 토큰 검증
            if (isExpired(refreshToken)) {
            	System.out.println("JWTUtil에 refresh토큰 만료");
                return null; // Refresh 토큰이 만료됨
            }

            String username = getUsername(refreshToken);
            String role = getRole(refreshToken);

            // DB에서 Refresh 토큰 확인
            if (!refreshRepository.existsByRefresh(refreshToken)) {
                return null; // DB에 저장된 Refresh 토큰이 아님
            }

            // 새로운 Access 토큰 생성
            String newAccessToken = createJwt("access", username, role, 3600000L); // 10분

            // 새로운 Refresh 토큰 생성 (선택적)
            String newRefreshToken = createJwt("refresh", username, role, 86400000L); // 24시간

            // DB에서 기존 Refresh 토큰 삭제 및 새 Refresh 토큰 저장
            refreshRepository.deleteByRefresh(refreshToken);
            
            // RefreshEntity 객체 생성
            RefreshEntity refreshEntity = new RefreshEntity();
            refreshEntity.setUsername(username);
            refreshEntity.setRefresh(newRefreshToken);

            // 만료 시간 설정 (예: 24시간 후)
            long expirationTimeMillis = System.currentTimeMillis() + 86400000L; // 현재 시간 + 24시간
            String expirationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(expirationTimeMillis));
            refreshEntity.setExpiration(expirationTime);

            // 저장
            refreshRepository.save(refreshEntity);

            // 새로운 토큰을 쿠키에 설정
            response.addCookie(createCookie("access", newAccessToken));
            response.addCookie(createCookie("refresh", newRefreshToken));
            System.out.println("create refresh " + newRefreshToken);
            
            return newAccessToken;
        } catch (Exception e) {
            // 예외 처리
            System.out.println("Token reissue failed: " + e.getMessage());
            return null;
        }
    }
    
    public Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(key.equals("access") ? 3600 : 86400);
        cookie.setHttpOnly(true);
    
        return cookie;
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    
    @Transactional
    public void invalidateToken(String token, HttpServletResponse response) {
        // Access 토큰 쿠키 삭제
        Cookie accessCookie = new Cookie("access", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // Refresh 토큰 쿠키 삭제
        Cookie refreshCookie = new Cookie("refresh", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        // DB에서 Refresh 토큰 삭제
        String username = getUsername(token);
        refreshRepository.deleteByUsername(username);
    }
}
