package com.gitee.sqlrest.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.enums.ExecuteEngineEnum;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.persistence.handler.ListParamHandler;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "SQLREST_API_ASSIGNMENT", autoResultMap = true)
public class ApiAssignmentEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("group_id")
  private Long groupId;

  @TableField("module_id")
  private Long moduleId;

  @TableField("datasource_id")
  private Long datasourceId;

  @TableField("name")
  private String name;

  @TableField("description")
  private String description;

  @TableField(value = "method", typeHandler = EnumTypeHandler.class)
  private HttpMethodEnum method;

  @TableField("path")
  private String path;

  @TableField(value = "params", typeHandler = ListParamHandler.class)
  private List<ItemParam> params;

  @TableField("status")
  private Boolean status;

  @TableField("open")
  private Boolean open;

  @TableField("content_type")
  private String contentType;

  @TableField(value = "engine", typeHandler = EnumTypeHandler.class)
  private ExecuteEngineEnum engine;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;

  @TableField(exist = false)
  private List<ApiContextEntity> contextList;
}
