package com.gitee.sqlrest.core.dto;

import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.enums.ExecuteEngineEnum;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("API配置")
public class ApiAssignmentSaveRequest {

  @ApiModelProperty("ID编号(保存接口使用)")
  private Long id;

  @ApiModelProperty("分组ID")
  private Long groupId;

  @ApiModelProperty("模块ID")
  private Long moduleId;

  @ApiModelProperty("数据源ID")
  private Long datasourceId;

  @ApiModelProperty("API配置名称")
  private String name;

  @ApiModelProperty("描述")
  private String description;

  @ApiModelProperty("API请求方法:GET, HEAD, PUT, POST, DELETE")
  private HttpMethodEnum method;

  @ApiModelProperty("HTTP请求的contentType")
  private String contentType;

  @ApiModelProperty("请求路径(不带api前缀)")
  private String path;

  @ApiModelProperty("是否公开")
  private Boolean open;

  @ApiModelProperty("执行引擎:SQL, SCRIPT")
  private ExecuteEngineEnum engine;

  @ApiModelProperty("SQL列表")
  private List<String> contextList;

  @ApiModelProperty("接口入参")
  private List<ItemParam> params;
}
