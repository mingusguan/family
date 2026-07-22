package com.youlai.boot.appuser.model;

import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * APP 用户管理请求与响应模型。
 */
public final class AppUserModels {

    private AppUserModels() {
    }

    /** APP 账号注册请求。 */
    @Data
    @Schema(description = "APP账号注册请求")
    public static class AppUserRegisterRequest {

        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 20, message = "用户名长度必须为4到20个字符")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
        private String username;

        @NotBlank(message = "昵称不能为空")
        @Size(min = 2, max = 20, message = "昵称长度必须为2到20个字符")
        private String nickname;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度必须为6到32个字符")
        private String password;
    }

    /** APP 账号密码登录请求。 */
    @Data
    @Schema(description = "APP账号密码登录请求")
    public static class AppUserLoginRequest {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /** APP 当前登录用户信息。 */
    @Data
    @Schema(description = "APP当前登录用户信息")
    public static class AppUserInfoVO {
        private Long userId;
        private String username;
        private String nickname;
        private String mobile;
        private String avatar;
        private Integer status;
    }

    /** APP 当前用户资料修改请求。 */
    @Data
    @Schema(description = "APP当前用户资料修改请求")
    public static class AppUserProfileUpdateRequest {

        @Size(min = 2, max = 20, message = "昵称长度必须为2到20个字符")
        @Schema(description = "用户昵称")
        private String nickname;

        @Size(max = 500, message = "头像地址长度不能超过500个字符")
        @Schema(description = "头像地址")
        private String avatar;
    }

    /** APP 当前用户资料。 */
    @Data
    @Schema(description = "APP当前用户资料")
    public static class AppUserProfileVO {

        @Schema(description = "APP用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "可选手机号")
        private String mobile;

        @Schema(description = "头像地址")
        private String avatar;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
    }

    /** APP 用户分页查询。 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AppUserQuery extends BaseQuery {
        @Schema(description = "用户名、昵称或手机号")
        private String keyword;

        @Schema(description = "状态：1-正常，0-禁用")
        private Integer status;
    }

    /** APP 用户管理视图。 */
    @Data
    public static class AppUserVO {
        private Long id;
        private String username;
        private String nickname;
        private String mobile;
        private String avatar;
        private Integer status;
        private Boolean wechatBound;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}

