package com.youlai.boot.appuser.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserTest {

    @Test
    void shouldUpdateAndTrimEditableProfileFields() {
        AppUser user = new AppUser();
        user.setNickname("旧昵称");
        user.setAvatar("old-avatar");

        user.updateProfile("  新昵称  ", "  new-avatar  ");

        assertThat(user.getNickname()).isEqualTo("新昵称");
        assertThat(user.getAvatar()).isEqualTo("new-avatar");
    }

    @Test
    void shouldKeepExistingProfileFieldsWhenRequestFieldIsNull() {
        AppUser user = new AppUser();
        user.setNickname("现有昵称");
        user.setAvatar("existing-avatar");

        user.updateProfile(null, null);

        assertThat(user.getNickname()).isEqualTo("现有昵称");
        assertThat(user.getAvatar()).isEqualTo("existing-avatar");
    }
}
