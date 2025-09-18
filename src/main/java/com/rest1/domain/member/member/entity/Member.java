package com.rest1.domain.member.member.entity;

import com.rest1.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Entity
public class Member extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    private String nickname;
    @Column(unique = true)  //식별자 역할을 하기위해 unique속성을 부여
    //unique가 붙은 속성은 인덱스가 만들어져서 이 속성으로 검색하면 매우 빨라진다는 장점이 있다.
    private String apiKey;  // apiKey는 아무 의미없는 값이 좋다.

    public Member(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.apiKey = UUID.randomUUID().toString(); //중복되지 않으면서 랜덤한 값을 만들어준다.

    }

    public String getName(){
        return nickname;
    }

    public void updateApiKey(String apiKey){
        this.apiKey = apiKey;
    }
}
