package com.gitee.sqlrest.core.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.OutParam;
import com.gitee.sqlrest.common.enums.CacheKeyTypeEnum;
import com.gitee.sqlrest.common.enums.ExecuteEngineEnum;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.enums.NamingStrategyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("API配置")
public class ApiAssignmentSaveRequest {

  @ApiModelProperty("ID编号(保存接口使用)")
  private Long id;

  @NotNull(message = "groupId不能为null")
  @ApiModelProperty("分组ID")
  private Long groupId;

  @NotNull(message = "moduleId不能为null")
  @ApiModelProperty("模块ID")
  private Long moduleId;

  @NotNull(message = "datasourceId不能为null")
  @ApiModelProperty("数据源ID")
  private Long datasourceId;

  @NotBlank(message = "name不能为空")
  @ApiModelProperty("API配置名称")
  private String name;

  @ApiModelProperty("描述")
  private String description;

  @NotNull(message = "method不能为null")
  @ApiModelProperty("API请求方法:GET, HEAD, PUT, POST, DELETE")
  private HttpMethodEnum method;

  @NotBlank(message = "contentType不能为空")
  @ApiModelProperty("HTTP请求的contentType")
  private String contentType;

  @NotBlank(message = "path不能为空")
  @ApiModelProperty("请求路径(不带api前缀)")
  private String path;

  @NotNull(message = "open不能为null")
  @ApiModelProperty("是否公开")
  private Boolean open;

  @NotNull(message = "engine不能为null")
  @ApiModelProperty("执行引擎:SQL, SCRIPT")
  private ExecuteEngineEnum engine;

  @NotEmpty(message = "contextList不能为空列表")
  @ApiModelProperty("SQL列表")
  private List<String> contextList;

  @ApiModelProperty("接口入参列表")
  private List<ItemParam> params;

  @ApiModelProperty("接口出参列表")
  private List<OutParam> outputs;

  @ApiModelProperty("接口出参数据类型转换格式")
  private List<DataTypeFormatMapValue> formatMap;

  @NotNull(message = "namingStrategy不能为null")
  @ApiModelProperty("接口出参属性命名策略")
  private NamingStrategyEnum namingStrategy;

  @NotNull(message = "flowStatus不能为null")
  @ApiModelProperty("是否开启流量控制")
  private Boolean flowStatus;

  @ApiModelProperty("阈值类型")
  private Integer flowGrade;

  @ApiModelProperty("单机阈值")
  private Integer flowCount;

  @ApiModelProperty("缓存键类型")
  private CacheKeyTypeEnum cacheKeyType;

  @TableField("缓存表达式")
  private String cacheKeyExpr;

  @TableField("缓存时常")
  private Long cacheExpireSeconds;
}
