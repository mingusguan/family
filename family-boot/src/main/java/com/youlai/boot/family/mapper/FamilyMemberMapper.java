package com.youlai.boot.family.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.family.model.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;

/** 家庭成员关系 Mapper。 */
@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
}
