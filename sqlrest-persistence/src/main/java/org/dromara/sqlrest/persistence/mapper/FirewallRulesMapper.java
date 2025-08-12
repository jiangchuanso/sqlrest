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
import org.dromara.sqlrest.common.enums.OnOffEnum;
import org.dromara.sqlrest.persistence.entity.FirewallRulesEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface FirewallRulesMapper extends BaseMapper<FirewallRulesEntity> {

  @Update("update SQLREST_FIREWALL_RULES set status = #{status}　where id = #{id}")
  void updateStatus(@Param("id") Long id, @Param("status") OnOffEnum status);

}
