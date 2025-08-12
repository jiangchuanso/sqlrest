// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.sqlrest.persistence.entity.ApiModuleEntity;
import org.dromara.sqlrest.persistence.mapper.ApiModuleMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ApiModuleDao {

  @Resource
  private ApiModuleMapper apiModuleMapper;

  public void insert(ApiModuleEntity entity) {
    apiModuleMapper.insert(entity);
  }

  public ApiModuleEntity getById(Long id) {
    return apiModuleMapper.selectById(id);
  }

  public List<ApiModuleEntity> listAll() {
    return listAll(null);
  }

  public List<ApiModuleEntity> listAll(String searchText) {
    return apiModuleMapper.selectList(
        Wrappers.<ApiModuleEntity>lambdaQuery()
            .like(StringUtils.hasText(searchText), ApiModuleEntity::getName, searchText)
            .orderByDesc(ApiModuleEntity::getId)
    );
  }

  public void updateById(ApiModuleEntity entity) {
    apiModuleMapper.updateById(entity);
  }

  public void deleteById(Long id) {
    apiModuleMapper.deleteById(id);
  }
}
