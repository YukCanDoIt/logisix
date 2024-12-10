package com.sparta.user.entity;

import com.sparta.common.entity.BaseEntity;
import lombok.*;
import jakarta.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Entity
@Table(
        name="p_user",
        uniqueConstraints = {
                @UniqueConstraint(name="UK_USER", columnNames={"username"})
        }
)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id", nullable=false, updatable=false)
    private Long userId;

    @Column(name="username", nullable = false, length = 100)
    private String username;

    @Column(name="password", nullable = false, length = 255)
    private String password;

    @Column(name="slackAccount", nullable = false, length = 255)
    private String slackAccount;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable = false)
    private Role role;

    @Builder
    private User(String username, String password, String slackAccount, Role role) {
        this.username = username;
        this.password = password;
        this.slackAccount = slackAccount;
        this.role = role;
        this.createBase(username);
    }

    // 새로운 회원 생성
    public static User create(String username, String password, String slackAccount, String createdBy) {
        User newUser = User.builder()
                .username(username)
                .password(password)
                .slackAccount(slackAccount)
                .role(Role.ANONYMOUS)
                .build();
        newUser.createBase(createdBy);
        return newUser;
    }

    // 회원 정보 수정
    public void update(String username, String password, String slackAccount, String updatedBy) {
        if(username!=null) this.username = username;
        if(password!=null) this.password = password;
        if(slackAccount!=null) this.slackAccount = slackAccount;
        this.updateBase(updatedBy);
    }

    // 권한 부여
    public void grantRole(Role role, String updatedBy) {
        this.role = role;
        this.updateBase(updatedBy);
    }
}