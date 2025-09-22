package com.rest1.global.Rq;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
//@RequestScope
@RequiredArgsConstructor
public class Rq {
/*
    private final MemberService memberService;
    private final HttpServletRequest request;  //요청마다 Rq가 생성되기 때문에 요청마다 api에 맞는 request값이 들어옴
    private final HttpServletResponse response;

    public Member getActor() {
        // HttpServletRequest request 에서 요청에 대한 header와 응답이 나옴

        String apiKey=null;
        String authorization = request.getHeader("Authorization");

        //헤더에 authorization이 없다면 에러를 던졌음 (헤더 방식만 고려함)
        //그러나 이젠 쿠키가 있기때문에 헤더가 없더라도 인증이 가능해야함
        if(authorization != null && !authorization.isEmpty()){ // 헤더 방식
            if(!authorization.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "헤더의 인증 정보 형식이 올바르지 않습니다.");
            }
            apiKey = authorization.replace("Bearer ", "");
            
        }else{  //쿠키 방식 -> else인걸 보면 알겠지만 헤더가 없으면 여기로 오는 것이다.
            Cookie[] cookies = request.getCookies();

            if(cookies == null){    //헤더도 없는데 쿠키도 없음
                throw new ServiceException("401-1", "인증 정보가 없습니다.");
            }

            for(Cookie cookie : cookies){
                if(cookie.getName().equals("apiKey")){
                    apiKey = cookie.getValue();
                    break;
                }

            }

        }


        Member actor = memberService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 올바르지 않습니다."));


        return actor;
    }

    public void addCookie(String name, String value){

        Cookie cookie = new Cookie(name, value);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        cookie.setHttpOnly(true);   //보안 옵션 켜기, XSS공격 방지


        response.addCookie(cookie);
    }

    public void deleteCookie(String name) {   //진짜 쿠키를 삭제하는게 아니라 기간이 0인걸로 바꿔치기 하는것
        Cookie cookie = new Cookie(name, "");   //기존에 apiKey라는 쿠키가 있으면 새로들어온 쿠키로 바꿔치기됨
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }*/

    //위 내용은 apiKey(헤더방식)와 Cookie를 통한 인증

    //아래 내용은 AccessToken(Cookie)과 apiKey(헤더 방식)를 통한 인증

    private final MemberService memberService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public Member getActor() {
        String apiKey;
        String accessToken;

        String headerAuthorization = getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {   // 헤더 방식
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401-2", "Authorization 헤더가 Bearer 형식이 아닙니다.");

            String[] headerAuthorizationBits = headerAuthorization.split(" ", 3); //Bearer User1 AccessToken 형태로 온걸 쪼개는것이다.

            apiKey = headerAuthorizationBits[1];
            accessToken = headerAuthorizationBits.length == 3 ? headerAuthorizationBits[2] : "";
        } else {        //쿠키 방식
            apiKey = getCookieValue("apiKey", "");
            accessToken = getCookieValue("accessToken", "");
        }

        if (apiKey.isBlank())
            throw new ServiceException("401-1", "로그인 후 이용해주세요.");

        Member member = null;

        if (!accessToken.isBlank()) {
            Map<String, Object> payload = memberService.payloadOrNull(accessToken);

            if (payload != null) {
                long id = (long) payload.get("id");
                String username = (String) payload.get("username");
                String nickname = (String) payload.get("nickname");

                member = new Member(id, username, nickname);

            }
        }

        if (member == null) {
            member = memberService
                    .findByApiKey(apiKey)
                    .orElseThrow(() -> new ServiceException("401-3", "API 키가 유효하지 않습니다."));
        }

        return member;
    }

    private String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    private String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");

        // 값이 없다면 해당 쿠키변수를 삭제하라는 뜻
        if (value.isBlank()) {
            cookie.setMaxAge(0);
        }

        response.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}