package com.voice.app.controller;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voice.app.domain.RefreshEntity;
import com.voice.app.dto.MemberDTO;
import com.voice.app.dto.SignupDTO;
import com.voice.app.exception.UserNotFoundException;
import com.voice.app.jwt.JWTUtil;
import com.voice.app.repository.RefreshRepository;
import com.voice.app.service.MemberService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    
    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        String token = getAccessToken(request);
        String username = jwtUtil.getUsername(token);

        try {
            memberService.deleteUser(username);
            // JWT 토큰 무효화
            jwtUtil.invalidateToken(token, response);
            return ResponseEntity.ok("User successfully deleted");
        } catch (Exception e) {
            log.error("Error while deleting user: " + username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while deleting the user: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/checkUsername")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam("username") String username) {
        boolean isAvailable = memberService.checkUsername(username);
        Map<String, Boolean> response = Collections.singletonMap("available", isAvailable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/api/user-roles")
    public List<String> getUserRoles(Authentication authentication) {
        // 현재 인증된 사용자의 Authentication 객체를 가져옴
        authentication = SecurityContextHolder.getContext().getAuthentication();

        // 사용자의 권한 목록을 가져와 문자열 리스트로 변환
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signupProcess(@RequestBody SignupDTO signupDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            memberService.signupProcess(signupDTO);
            response.put("code", 0);
            response.put("message", "회원가입이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            response.put("code", 1);
            response.put("message", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    } 

    @GetMapping("/validate-token")
    public ResponseEntity<Void> validateToken(@RequestHeader("access") String token) {
        try {
            // 토큰 만료 여부 확인
            jwtUtil.isExpired(token);
            
            // 토큰의 타입이 'access'인지 확인
            String category = jwtUtil.getCategory(token);
            if (!category.equals("access")) {
                throw new JwtException("Invalid token type");
            }

            // 여기에 추가적인 검증 로직을 넣을 수 있습니다.
            // 예: 사용자 존재 여부 확인, 토큰 블랙리스트 확인 등

            return ResponseEntity.ok().build(); // 200 OK
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
    }
    
    // 마이페이지 접근시 api 호출로 유저 정보 가져오기
    @GetMapping("/api/mypage")
    public ResponseEntity<MemberDTO> getMyPageInfo(Authentication authentication) {
        String username = authentication.getName();
        MemberDTO memberInfo = memberService.getUserInfo(username);
        return ResponseEntity.ok(memberInfo);
    }

    @PostMapping("/api/mypage/update")
    public ResponseEntity<MemberDTO> updateMyInfo(@RequestBody MemberDTO updateInfo, Authentication authentication) {
        String username = authentication.getName();
        try {
            MemberDTO updatedMemberInfo = memberService.updateUserInfo(username, updateInfo);
            return ResponseEntity.ok(updatedMemberInfo);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    
    // Refresh토큰으로 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }
        
        //DB에 저장되어 있는지 확인
            Boolean isExist = refreshRepository.existsByRefresh(refresh);
            if (!isExist) {
            
                //response body
                return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
            }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
        
        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefresh, 86400000L);
        
        //response
        // response.setHeader("access", newAccess);
        response.addCookie(createCookie("access", newAccess));
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);
    
        return cookie;
    }
    
    @GetMapping("/checkLogin")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpServletRequest request) {
        boolean isLoggedIn = checkUserLoginStatus(request);

        Map<String, Object> response = new HashMap<>();
        response.put("isLoggedIn", isLoggedIn);
        String token = getAccessToken(request);
        response.put("accessToken", token);
        return ResponseEntity.ok(response);
    }

    private boolean checkUserLoginStatus(HttpServletRequest request) {
        // 쿠키에서 'access' 토큰을 추출
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("cookies가 널이 아님");
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
    
        // 토큰 검증
        if (token != null) {
            return jwtUtil.validateToken(token);
        }
        return false;
    }

    private String getAccessToken(HttpServletRequest request) {
        // 쿠키에서 'access' 토큰을 추출
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("cookies가 널이 아님");
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
    
        // 토큰 검증
        if (token != null) {
            if (jwtUtil.validateToken(token))
                return token;
        }
        return null;
    }

}
