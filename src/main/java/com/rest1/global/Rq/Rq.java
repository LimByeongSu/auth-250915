package com.rest1.global.Rq;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
//@RequestScope
@RequiredArgsConstructor
public class Rq {

    private final MemberService memberService;
    private final HttpServletRequest request;  //요청마다 Rq가 생성되기 때문에 요청마다 api에 맞는 request값이 들어옴

    public Member getActor() {
        // HttpServletRequest request 에서 요청에 대한 header와 응답이 나옴

        String authorization = request.getHeader("Authorization");

        if(authorization == null || authorization.isEmpty()) {
            throw new ServiceException("401-1", "헤더에 인증 정보가 없습니다.");
        }

        if(!authorization.startsWith("Bearer ")) {
            throw new ServiceException("401-2", "헤더의 인증 정보 형식이 올바르지 않습니다.");
        }

        Member actor = memberService.findByApiKey(authorization.replace("Bearer ", ""))
                .orElseThrow(() -> new ServiceException("401-3", "API 키가 올바르지 않습니다."));


        return actor;
    }

}