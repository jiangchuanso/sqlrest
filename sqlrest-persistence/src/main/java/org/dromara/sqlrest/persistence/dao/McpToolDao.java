package org.dromara.sqlrest.persistence.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.sqlrest.persistence.entity.McpToolEntity;
import org.dromara.sqlrest.persistence.mapper.McpToolMapper;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class McpToolDao {

  @Resource
  private McpToolMapper mcpToolMapper;


  public void insert(McpToolEntity entity) {
    mcpToolMapper.insert(entity);
  }

  public McpToolEntity getById(Long id) {
    return mcpToolMapper.selectById(id);
  }

  public List<McpToolEntity> listAll(String searchText) {
    return mcpToolMapper.selectList(
        Wrappers.<McpToolEntity>lambdaQuery()
            .like(StringUtils.isNotBlank(searchText), McpToolEntity::getName, searchText)
            .orderByDesc(McpToolEntity::getCreateTime)
    );
  }

  public void updateById(McpToolEntity entity) {
    mcpToolMapper.updateById(entity);
  }

  public void deleteById(Long id) {
    mcpToolMapper.deleteById(id);
  }
}
