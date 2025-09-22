package com.rest1.domain.post.post.controller;

import com.rest1.domain.member.member.entity.Member;
import com.rest1.domain.post.post.service.PostService;
import com.rest1.global.Rq.Rq;
import com.rest1.global.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/adm/posts")
@Tag(name = "ApiV1AdmPostController", description = "관리자용 글 API")
@SecurityRequirement(name = "bearerAuth")
public class ApiV1AdmPostController {

    private final PostService postService;
    private final Rq rq;

    record CountResBody(
            long totalCount
    ) {
    }

    @GetMapping("/count")
    @Transactional(readOnly = true)
    @Operation(summary = "글 개수 조회")
    public CountResBody count() {

        Member actor = rq.getActor();

        if(!actor.isAdmin()) {
            throw new ServiceException("403-1", "권한이 없습니다");
        }

        return new CountResBody(postService.count());
    }

}
