// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.sqlrest.persistence.entity.VersionCommitEntity;

public interface VersionCommitMapper extends BaseMapper<VersionCommitEntity> {

  @Select("SELECT max(version) FROM SQLREST_VERSION_COMMIT WHERE biz_id = #{bizId} ")
  Integer getMaxVersion(@Param("bizId") Long bizId);

  @Select("SELECT * FROM SQLREST_VERSION_COMMIT WHERE biz_id = #{bizId} ORDER BY version DESC LIMIT 1 ")
  VersionCommitEntity getLatestVersion(@Param("bizId") Long bizId);
}
