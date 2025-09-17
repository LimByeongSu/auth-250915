package com.rest1.domain.post.post.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.member.member.service.MemberService;
import com.rest1.domain.post.post.dto.PostDto;
import com.rest1.domain.post.post.entity.Post;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.global.Rq.Rq;
import com.rest1.global.exception.ServiceException;
import com.rest1.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1PostController", description = "글 API")
public class ApiV1PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final Rq rq;


    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "글 다건 조회")
    public List<PostDto> getItems() {
        return postService.findAll().reversed().stream()
                .map(PostDto::new)
                .toList();
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "글 단건 조회")
    public PostDto getItem(
            @PathVariable Long id
    ) {

        System.out.println("memberService: " + memberService);
        System.out.println("rq : " + rq);

        Post post = postService.findById(id).get();
        return new PostDto(post);

    }


    @DeleteMapping("/{id}")
    @Operation(summary = "글 삭제")
    public RsData<Void> deleteItem(
            @PathVariable Long id,
            @RequestHeader("Authorization") @NotBlank @Size(min=30, max=50) String apiKey
    ) {
        String authorization = apiKey.replace("Bearer ", "");

        Member actor = memberService.findByApiKey(authorization).orElseThrow(() -> new ServiceException("401-1", "API 키가 올바르지 않습니다."));

        Post post = postService.findById(id).get();
        if(!actor.equals(post.getAuthor())) throw new ServiceException("403-1", "삭제 권한이 없습니다.");

        postService.delete(post);

        return new RsData<Void>(
                "200-1",
                "%d번 게시물이 삭제되었습니다.".formatted(id)
        );
    }


    record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 10)
            String title,

            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    record PostWriteResBody(
            PostDto postDto
    ) {
    }

    @PostMapping
    @Transactional
    @Operation(summary = "글 작성")
    public RsData<PostWriteResBody> createItem(
            @RequestBody @Valid PostWriteReqBody reqBody,
            @RequestHeader("Authorization") @NotBlank @Size(min = 10, max=100) String apiKey
    ) {

        String authorization = apiKey.replace("Bearer ", "");
        //실제 DB에 등록되어있는지 확인하기 -> apiKey로 확인 한다.
        Member actor = memberService.findByApiKey(authorization).orElseThrow(() -> new ServiceException("401-1", "API 키가 올바르지 않습니다."));
        Post post = postService.write(actor, reqBody.title, reqBody.content);
        
        return new RsData<>(
                "201-1",
                "%d번 게시물이 생성되었습니다.".formatted(post.getId()),
                new PostWriteResBody(
                        new PostDto(post)
                )
        );
    }


    record PostModifyReqBody(
            @NotBlank
            @Size(min = 2, max = 10)
            String title,

            @NotBlank
            @Size(min = 2, max = 100)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "글 수정")
    public RsData<Void> modifyItem(
            @PathVariable Long id,
            @RequestBody @Valid PostModifyReqBody reqBody,
            @RequestHeader("Authorization") @NotBlank @Size(min = 10, max=100) String apiKey
    ) {

        String authorization = apiKey.replace("Bearer ", "");
        //실제 DB에 등록되어있는지 확인하기 -> apiKey로 확인 한다.
        //인증
        Member actor = memberService.findByApiKey(authorization).orElseThrow(() -> new ServiceException("401-1", "API 키가 올바르지 않습니다."));

        //권한 체크( 인가 )
        Post post = postService.findById(id).get();
        if( ! actor.equals(post.getAuthor())){
            throw new ServiceException("403-1", "수정 권한이 없습니다.");
        }


        //수정 로직
        postService.modify(post, reqBody.title, reqBody.content);

        return new RsData(
                "200-1",
                "%d번 게시물이 수정되었습니다.".formatted(id)
        );
    }
}
