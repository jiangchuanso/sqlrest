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
import org.dromara.sqlrest.persistence.entity.ApiGroupEntity;
import org.dromara.sqlrest.persistence.mapper.ApiGroupMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class ApiGroupDao {

  @Resource
  private ApiGroupMapper apiGroupMapper;

  public void insert(ApiGroupEntity entity) {
    apiGroupMapper.insert(entity);
  }

  public ApiGroupEntity getById(Long id) {
    return apiGroupMapper.selectById(id);
  }

  public List<ApiGroupEntity> listAll() {
    return listAll(null);
  }

  public List<ApiGroupEntity> listAll(String searchText) {
    return apiGroupMapper.selectList(
        Wrappers.<ApiGroupEntity>lambdaQuery()
            .like(StringUtils.hasText(searchText), ApiGroupEntity::getName, searchText)
            .orderByDesc(ApiGroupEntity::getId)
    );
  }

  public void updateById(ApiGroupEntity entity) {
    apiGroupMapper.updateById(entity);
  }

  public void deleteById(Long id) {
    apiGroupMapper.deleteById(id);
  }
}
