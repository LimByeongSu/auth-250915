package com.rest1.global.Rq;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
//@RequestScope
@RequiredArgsConstructor
public class Rq {

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
    }
}