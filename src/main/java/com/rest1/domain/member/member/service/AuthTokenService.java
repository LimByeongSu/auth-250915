package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.standard.ut.Ut;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Service
public class AuthTokenService {

    //yml파일에 있는 값을 사용하기 위한 어노테이션
    @Value("${custom.jwt.secretPattern}")
    private String secretPattern;
    @Value("${custom.jwt.expireSeconds}")
    private long expireSeconds;

    //접근 제어자를 적지 않으면 default 접근제어자가 적용되고 같은 패키지 에서만 사용할 수 있게된다.
    String genAccessToken(Member member) {

        return Ut.jwt.toString(
                secretPattern,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername())
        );
    }

    //접근 제어자를 적지 않으면 default 접근제어자가 적용되고 같은 패키지 에서만 사용할 수 있게된다.
    Map<String, Object> payloadOrNull(String jwt) {
        Map<String, Object> payload = Ut.jwt.payloadOrNull(jwt, secretPattern);

        if(payload == null) {
            return null;
        }

        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();

        String username = (String)payload.get("username");

        return Map.of("id", id, "username", username);
    }
}
