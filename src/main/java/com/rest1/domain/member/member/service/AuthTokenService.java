package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.standard.ut.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    private String secretPattern= "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";
    private long expireSeconds = 1000L * 60 * 60 * 24 * 365;

    public String genAccessToken(Member member) {

        return Ut.jwt.toString(
                secretPattern,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getName())
        );
    }
}
