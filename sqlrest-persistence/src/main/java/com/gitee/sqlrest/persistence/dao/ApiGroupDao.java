package com.gitee.sqlrest.persistence.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.sqlrest.persistence.entity.ApiGroupEntity;
import com.gitee.sqlrest.persistence.mapper.ApiGroupMapper;
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
