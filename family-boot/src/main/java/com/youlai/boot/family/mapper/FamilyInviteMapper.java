package com.youlai.boot.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.family.model.entity.FamilyInvite;
import org.apache.ibatis.annotations.Mapper;

/** 家庭邀请凭证持久化接口。 */
@Mapper
public interface FamilyInviteMapper extends BaseMapper<FamilyInvite> {
}