package com.youlai.boot.appuser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.appuser.model.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

/** APP 用户持久化接口。 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
