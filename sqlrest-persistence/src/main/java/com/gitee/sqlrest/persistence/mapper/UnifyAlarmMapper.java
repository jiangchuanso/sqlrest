package com.gitee.sqlrest.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.sqlrest.common.enums.OnOffEnum;
import com.gitee.sqlrest.persistence.entity.UnifyAlarmEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UnifyAlarmMapper extends BaseMapper<UnifyAlarmEntity> {

  @Update("update `SQLREST_UNIFY_ALARM` set status = #{status}　where id = #{id}")
  void updateStatus(@Param("id") Long id, @Param("status") OnOffEnum status);
}
