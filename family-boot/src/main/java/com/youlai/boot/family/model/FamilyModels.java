package com.youlai.boot.family.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/** 家庭模块请求与响应模型集合。 */
public final class FamilyModels {
    private FamilyModels() {
    }

    /** 新建家庭参数。 */
    @Data
    public static class FamilyCreateRequest {
        @NotBlank(message = "家庭名称不能为空")
        @Size(max = 50, message = "家庭名称不能超过50个字符")
        private String name;

        @Size(max = 200, message = "家庭介绍不能超过200个字符")
        private String description;
    }

    /** 家庭相册创建参数。 */
    @Data
    public static class FamilyAlbumCreateRequest {
        @NotBlank(message = "相册名称不能为空")
        @Size(max = 50, message = "相册名称不能超过50个字符")
        private String name;

        @Size(max = 200, message = "相册介绍不能超过200个字符")
        private String description;
    }

    /** 当前用户所属家庭视图。 */
    @Data
    public static class FamilyVO {
        private Long id;
        private String name;
        private String description;
        private String coverUrl;
        private Long creatorId;
        private String role;
        private Long memberCount;
        private Long albumCount;
        private LocalDateTime createTime;
    }

    /** 家庭详情视图。 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FamilyDetailVO extends FamilyVO {
        private List<FamilyMemberVO> members;
    }

    /** 家庭成员视图。 */
    @Data
    public static class FamilyMemberVO {
        private Long userId;
        private String nickname;
        private String avatar;
        private String role;
        private LocalDateTime joinedAt;
    }

    /** 家庭邀请视图。 */
    @Data
    public static class FamilyInviteVO {
        private String code;
        private Long familyId;
        private String familyName;
        private String inviterName;
        private LocalDateTime expiresAt;
        private Boolean alreadyMember;
    }

    /** 家庭相册视图。 */
    @Data
    public static class FamilyAlbumVO {
        private Long id;
        private Long familyId;
        private String name;
        private String description;
        private String coverUrl;
        private Long creatorId;
        private Long assetCount;
        private LocalDateTime createTime;
    }
}
