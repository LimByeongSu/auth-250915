package com.rest1.domain.member.member.service;

import com.rest1.domain.member.member.repository.MemberRepository;
import com.rest1.domain.member.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private MemberRepository memberRepository;

    public long count(){
        return memberRepository.count();
    }

    public void join(String username, String password, String nickname) {
        Member member = new Member(username, password, nickname);
    }
}
