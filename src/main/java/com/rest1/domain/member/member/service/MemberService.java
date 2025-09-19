package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;

    public long count(){
        return memberRepository.count();
    }

    public Member join(String username, String password, String nickname) {

        //username 중복 검증
        memberRepository.findByUsername(username)
                .ifPresent((Member m) ->{
                    throw new ServiceException("409-1", "이미 존재하는 아이디입니다.");
                });
        
        Member member = new Member(username, password, nickname);
        return memberRepository.save(member);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findByApiKey(String apiKey) {
        return memberRepository.findByApiKey(apiKey);
    }

    public String genAccessToken(Member member) {
        return authTokenService.genAccessToken(member);
    }
}
