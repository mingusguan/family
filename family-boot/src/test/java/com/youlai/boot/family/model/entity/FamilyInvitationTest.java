package com.youlai.boot.family.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FamilyInvitationTest {

    @Test
    void shouldCreateReusableInviteWithExpiration() {
        LocalDateTime now = LocalDateTime.now();

        FamilyInvite invite = FamilyInvite.create(10L, 20L, now.plusDays(7));

        assertThat(invite.getFamilyId()).isEqualTo(10L);
        assertThat(invite.getInviterId()).isEqualTo(20L);
        assertThat(invite.getCode()).hasSize(32);
        assertThat(invite.isValidAt(now)).isTrue();
        assertThat(invite.isValidAt(now.plusDays(8))).isFalse();
    }

    @Test
    void shouldCreateInvitedUserAsNormalMember() {
        FamilyMember member = FamilyMember.member(10L, 30L);

        assertThat(member.getFamilyId()).isEqualTo(10L);
        assertThat(member.getUserId()).isEqualTo(30L);
        assertThat(member.getRole()).isEqualTo(FamilyMember.ROLE_MEMBER);
        assertThat(member.getIsDeleted()).isZero();
    }
}