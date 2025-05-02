package com.gitee.sqlrest.core.dto;

import com.gitee.sqlrest.common.enums.ProductTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataSourceBaseRequest {
  
  @NotBlank(message = "name不能为空")
  @ApiModelProperty("名称")
  private String name;

  @NotNull(message = "type不能为null")
  @ApiModelProperty("类型")
  private ProductTypeEnum type;

  @NotBlank(message = "version不能为空")
  @ApiModelProperty("驱动版本")
  private String version;

  @NotBlank(message = "driver不能为空")
  @ApiModelProperty("驱动类型")
  private String driver;

  @NotBlank(message = "url不能为空")
  @ApiModelProperty("连接JDBC-URL")
  private String url;

  @NotBlank(message = "username不能为空")
  @ApiModelProperty("账号")
  private String username;

  @ApiModelProperty("密码")
  private String password;
}
