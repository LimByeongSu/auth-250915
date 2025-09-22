package com.rest1.domain.member.member.controller;


import com.rest1.domain.member.member.dto.MemberDto;
import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.AuthTokenService;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.global.Rq.Rq;
import com.rest1.global.exception.ServiceException;
import com.rest1.global.rsData.RsData;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;
    private final AuthTokenService authTokenService;

    record JoinReqBody( //받아야하는 데이터 (요청용)
            @NotBlank
            @Size(min=2, max=30)
            String username,

            @NotBlank
            @Size(min=2, max=30)
            String password,

            @NotBlank
            @Size(min=2, max=30)
            String nickname
    ){

    }

    record JoinResBody( //응답용
            MemberDto memberDto
    ){

    }

    @PostMapping("/join")
    public RsData<JoinResBody> join(
            @RequestBody @Valid JoinReqBody reqBody
    ){
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);

        return new RsData<JoinResBody>(
                "201-1",
                "회원가입이 완료되었습니다. %s님 환영합니다.".formatted(reqBody.nickname),
                new JoinResBody(
                        new MemberDto(member)
                )
        );
    }


    record LoginReqBody( //받아야하는 데이터 (요청용)
                        @NotBlank
                        @Size(min=2, max=30)
                        String username,

                        @NotBlank
                        @Size(min=2, max=30)
                        String password
    ){

    }

    record LoginResBody( //응답용
        MemberDto memberDto,
        String apiKey,
        String accessToken
    ){

    }

    @PostMapping("/login")
    public RsData<LoginResBody> login(
            @RequestBody @Valid LoginReqBody reqBody,
            HttpServletResponse response
    ){

        Member member = memberService.findByUsername(reqBody.username).orElseThrow(
                () -> new ServiceException("401-1", "존재하지 않는 아이디입니다.")
        );

        if( ! member.getPassword().equals(reqBody.password)){
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("apiKey", member.getApiKey()); //쿠키 생성
        rq.setCookie("accessToken", accessToken);

        return new RsData(
                "200-1",
                "%s님 환영합니다.".formatted(reqBody.username),
                new LoginResBody(
                    new MemberDto(member),
                    member.getApiKey(),  //apiKey를 사용하는 방식은 남겨놓는다. 모바일에서도 사용할 것 고려
                    accessToken

                )

        );
    }


    record MeResBody(
            MemberDto memberDto
    ) {
    }

    @GetMapping("/me")
    public RsData<MemberDto> me() {

        //인증, 인가를 받고 정보를 보여줘야함 
        Member  author = memberService.findById(rq.getActor().getId()).get();

        return new RsData(
                "200-1",
                "OK",
                new MeResBody(
                        new MemberDto(author)
                )
        );
    }

    @DeleteMapping("/logout")
    public RsData<Void> logout() {

        rq.deleteCookie("apiKey");

        return new RsData<>(
                "200-1",
                "로그아웃 되었습니다."
        );
    }

}
