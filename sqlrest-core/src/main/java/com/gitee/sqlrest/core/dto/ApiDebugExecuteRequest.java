package com.gitee.sqlrest.core.dto;

import com.gitee.sqlrest.common.dto.ParamValue;
import com.gitee.sqlrest.common.enums.ExecuteEngineEnum;
import com.gitee.sqlrest.common.enums.NamingStrategyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("SQL调试执行")
public class ApiDebugExecuteRequest {

  @NotNull(message = "datasourceId不能为null")
  @ApiModelProperty("数据源的ID")
  private Long dataSourceId;

  @NotNull(message = "engine不能为null")
  @ApiModelProperty("执行引擎:SQL, SCRIPT")
  private ExecuteEngineEnum engine;

  @ApiModelProperty("数据类型转换格式")
  private List<DataTypeFormatMapValue> formatMap;

  @NotNull(message = "namingStrategy不能为null")
  @ApiModelProperty("属性命名策略")
  private NamingStrategyEnum namingStrategy;

  @NotEmpty(message = "contextList不能为空")
  @ApiModelProperty("SQL列表")
  private List<String> contextList;

  @ApiModelProperty("接口入参列表")
  private List<ParamValue> paramValues;
}
