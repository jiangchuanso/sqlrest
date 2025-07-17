// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.servlet;

import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.BaseParam;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.OutParam;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.persistence.dao.ApiAssignmentDao;
import com.gitee.sqlrest.persistence.dao.ApiModuleDao;
import com.gitee.sqlrest.persistence.dao.SystemParamDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.ApiModuleEntity;
import com.gitee.sqlrest.persistence.entity.SystemParamEntity;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 文档格式说明：https://openapi.apifox.cn/
 * <p>
 * SwaggerEditor: https://editor.swagger.io/
 * </p>
 */
@Slf4j
@Service
public class ApiSwaggerService {

  private static final String INFO_TITLE = "SQLREST在线接口文档";
  private static final String INFO_VERSION = "1.0";
  private static final String INFO_DESCRIPTION = "基于SQLREST配置生成的Swagger在线接口文档";
  private static final String TOKEN_MODEL = "TOKEN认证授权";
  private static final String APPLICATION_JSON = "application/json";
  private static final String AUTHORIZATION = "Authorization";

  @Resource
  private ApiAssignmentDao apiAssignmentDao;
  @Resource
  private ApiModuleDao apiModuleDao;
  @Resource
  private SystemParamDao systemParamDao;

  private String getApiUrlPrefix() {
    return String.format("/%s/", Constants.API_PATH_PREFIX);
  }

  private Triple<String, String, String> getOpenApiInfo() {
    SystemParamEntity title = systemParamDao.getByParamKey(Constants.SYS_PARAM_KEY_SWAGGER_INFO_TITLE);
    SystemParamEntity version = systemParamDao.getByParamKey(Constants.SYS_PARAM_KEY_SWAGGER_INFO_VERSION);
    SystemParamEntity description = systemParamDao.getByParamKey(Constants.SYS_PARAM_KEY_SWAGGER_INFO_DESCRIPTION);
    return Triple.of(
        Optional.ofNullable(title).map(SystemParamEntity::getParamValue).orElse(INFO_TITLE),
        Optional.ofNullable(version).map(SystemParamEntity::getParamValue).orElse(INFO_VERSION),
        Optional.ofNullable(description).map(SystemParamEntity::getParamValue).orElse(INFO_DESCRIPTION)
    );
  }

  public OpenAPI getSwaggerJson(HttpServletRequest request) {
    Triple<String, String, String> entity = getOpenApiInfo();
    Info info = new Info().title(entity.getLeft())
        .version(entity.getMiddle())
        .description(entity.getRight());

    OpenAPI openAPI = new OpenAPI();
    openAPI.info(info);

    // 模块
    List<ApiModuleEntity> moduleEntities = apiModuleDao.listAll();
    for (ApiModuleEntity module : moduleEntities) {
      openAPI.addTagsItem(new Tag().name(module.getName()));
    }

    //token获取接口
    openAPI.path("/token/generate", getTokenGeneratePathItem());

    // 自定义的接口
    Map<Long, ApiModuleEntity> moduleIdMap = moduleEntities.stream()
        .collect(Collectors.toMap(ApiModuleEntity::getId,
            Function.identity(), (a, b) -> a));
    String urlPrefix = getApiUrlPrefix();
    for (ApiAssignmentEntity assignment : apiAssignmentDao.listAll()) {
      if (!assignment.getStatus()) {
        // 过滤掉未发布的
        continue;
      }
      String path = urlPrefix + assignment.getPath();
      HttpMethodEnum method = assignment.getMethod();

      PathItem pathItem = new PathItem();
      pathItem.setSummary(assignment.getName());
      pathItem.setDescription(StringUtils.defaultIfBlank(assignment.getDescription(), assignment.getName()));

      Operation operation = new Operation();
      operation.setOperationId(String.valueOf(assignment.getId()));
      operation.addTagsItem(moduleIdMap.get(assignment.getModuleId()).getName());
      //FIX：导入接口文档到apifox时，方法名不能正常显示接口名称问题
      operation.setSummary(assignment.getName());
      operation.setDescription(StringUtils.defaultIfBlank(assignment.getDescription(), assignment.getName()));

      // 入参
      List<ItemParam> params = assignment.getParams();
      if (!CollectionUtils.isEmpty(params)) {
        List<ItemParam> paramList = params.stream()
            .filter(i -> i.getLocation().isParameter())
            .collect(Collectors.toList());
        for (ItemParam param : paramList) {
          ParamTypeEnum type = param.getType();

          Parameter parameter =
              param.getLocation().isHeader()
                  ? new HeaderParameter()
                  : new QueryParameter();
          parameter.setName(param.getName());

          Schema schema = new Schema().type(type.getJsType());
          if (param.getIsArray()) {
            ArraySchema arraySchema = new ArraySchema().items(schema);
            parameter.setSchema(arraySchema);
          } else {
            parameter.setSchema(schema);
          }
          parameter.setDescription(param.getRemark());
          parameter.setRequired(param.getRequired());
          parameter.setDeprecated(false);
          parameter.allowEmptyValue(true);
          parameter.setExample(param.getType().getExample());

          operation.addParametersItem(parameter);
        }

        List<ItemParam> requestBodyList = params.stream()
            .filter(i -> i.getLocation().isRequestBody())
            .collect(Collectors.toList());
        if (requestBodyList.size() > 0) {
          RequestBody requestBody = new RequestBody();
          requestBody.setRequired(true);
          requestBody.setDescription(assignment.getName());

          Schema objectSchema = new ObjectSchema().name("type");
          for (ItemParam param : requestBodyList) {
            ParamTypeEnum type = param.getType();

            Schema schema = new Schema().type(type.getJsType());
            if (param.getIsArray()) {
              if (type.isObject()) {
                Schema subObjectSchema = new ObjectSchema().name(param.getName());
                if (null != param.getChildren()) {
                  for (BaseParam baseParam : param.getChildren()) {
                    Schema subSchema = new Schema().type(baseParam.getType().getJsType());
                    if (Optional.ofNullable(baseParam.getIsArray()).orElse(false)) {
                      ArraySchema subArraySchema = new ArraySchema().items(subSchema);
                      subObjectSchema.addProperties(baseParam.getName(), subArraySchema);
                    } else {
                      subObjectSchema.addProperties(baseParam.getName(), subSchema);
                    }
                  }
                }
                ArraySchema arraySchema = new ArraySchema().items(subObjectSchema);
                objectSchema.addProperties(param.getName(), arraySchema);
              } else {
                ArraySchema arraySchema = new ArraySchema().items(schema);
                objectSchema.addProperties(param.getName(), arraySchema);
              }
            } else {
              if (type.isObject()) {
                Schema subObjectSchema = new ObjectSchema().name(param.getName());
                if (null != param.getChildren()) {
                  for (BaseParam baseParam : param.getChildren()) {
                    Schema subSchema = new Schema().type(baseParam.getType().getJsType());
                    if (Optional.ofNullable(baseParam.getIsArray()).orElse(false)) {
                      ArraySchema subArraySchema = new ArraySchema().items(subSchema);
                      subObjectSchema.addProperties(baseParam.getName(), subArraySchema);
                    } else {
                      subObjectSchema.addProperties(baseParam.getName(), subSchema);
                    }
                  }
                }
                objectSchema.addProperties(param.getName(), subObjectSchema);
              } else {
                objectSchema.addProperties(param.getName(), schema);
              }
            }
          }

          Content content = new Content();
          content.addMediaType(
              assignment.getContentType(),
              new MediaType().schema(objectSchema)
          );
          requestBody.setContent(content);
          operation.setRequestBody(requestBody);
        }
      }

      if (HttpMethodEnum.GET == method) {
        pathItem.setGet(operation);
      } else if (HttpMethodEnum.HEAD == method) {
        pathItem.setHead(operation);
      } else if (HttpMethodEnum.PUT == method) {
        pathItem.setPut(operation);
      } else if (HttpMethodEnum.POST == method) {
        pathItem.setPost(operation);
      } else if (HttpMethodEnum.DELETE == method) {
        pathItem.setDelete(operation);
      } else {
        continue;
      }

      // 响应
      operation.setResponses(getApiResponses(assignment.getOutputs()));
      operation.security(Collections.singletonList(new SecurityRequirement().addList(AUTHORIZATION)));

      openAPI.path(path, pathItem);
    }

    Map<String, SecurityScheme> securitySchemes = ImmutableMap.of(
        AUTHORIZATION,
        new SecurityScheme()
            .type(Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
    );
    openAPI.setComponents(new Components().securitySchemes(securitySchemes));

    return openAPI;
  }

  private PathItem getTokenGeneratePathItem() {
    PathItem pathItem = new PathItem();
    pathItem.setSummary(TOKEN_MODEL);
    pathItem.setDescription(TOKEN_MODEL);

    RequestBody requestBody = new RequestBody();
    requestBody.setRequired(true);
    requestBody.setDescription(TOKEN_MODEL);
    Content content = new Content();
    content.addMediaType(APPLICATION_JSON,
        new MediaType().schema(
            new ObjectSchema().name("type")
                .addProperties("clientId", new StringSchema())
                .addProperties("secret", new StringSchema())
        )
    );
    requestBody.setContent(content);

    Operation operation = new Operation();
    operation.setOperationId("0");
    operation.addTagsItem(TOKEN_MODEL);
    operation.setRequestBody(requestBody);
    //FIX：导入接口文档到apifox时，方法名不能正常显示接口名称问题
    operation.setSummary(TOKEN_MODEL);
    operation.setDescription(TOKEN_MODEL);

    ApiResponses apiResponses = new ApiResponses();
    apiResponses.addApiResponse("200",
        new ApiResponse().description("OK")
            .content(
                new Content().addMediaType(
                    "application/json",
                    new MediaType().schema(
                        new Schema().name("type")
                            .type("object")
                            .addProperties("accessToken", new StringSchema())
                            .addProperties("expireSeconds", new NumberSchema())
                    )
                )
            )
    );
    apiResponses.addApiResponse("201",
        new ApiResponse().description("Created")
    );
    apiResponses.addApiResponse("401",
        new ApiResponse().description("Unauthorized")
    );
    apiResponses.addApiResponse("403",
        new ApiResponse().description("Forbidden")
    );
    apiResponses.addApiResponse("404",
        new ApiResponse().description("Not Found")
    );
    operation.setResponses(apiResponses);

    pathItem.setPost(operation);

    return pathItem;
  }

  private ApiResponses getApiResponses(List<OutParam> outputs) {
    Schema rootSchema = new ObjectSchema()
        .addProperties("code", new NumberSchema())
        .addProperties("message", new StringSchema());
    if (!CollectionUtils.isEmpty(outputs)) {
      ObjectSchema objectSchema = new ObjectSchema();
      for (OutParam param : outputs) {
        ParamTypeEnum typeItem = param.getType();
        if (Optional.ofNullable(param.getIsArray()).orElse(false)) {
          Schema subSchema = new Schema().type(param.getType().getJsType())
              .description(param.getRemark())
              .format(getTypeFormat(param.getType()));
          ArraySchema subArraySchema = new ArraySchema().items(subSchema);
          objectSchema.addProperties(param.getName(), subArraySchema);
        } else {
          Schema propertiesItem;
          if (Optional.ofNullable(typeItem.isObject()).orElse(false)) {
            propertiesItem = new ObjectSchema().description(param.getRemark());
            if (!CollectionUtils.isEmpty(param.getChildren())) {
              for (OutParam subParam : param.getChildren()) {
                Schema subSchema = new Schema().type(subParam.getType().getJsType())
                    .description(subParam.getRemark())
                    .format(getTypeFormat(subParam.getType()));
                if (Optional.ofNullable(subParam.getIsArray()).orElse(false)) {
                  ArraySchema subArraySchema = new ArraySchema().items(subSchema);
                  propertiesItem.addProperties(subParam.getName(), subArraySchema);
                } else {
                  propertiesItem.addProperties(subParam.getName(), subSchema);
                }
              }
            }
          } else {
            propertiesItem = new Schema().type(typeItem.getJsType())
                .description(param.getRemark())
                .format(getTypeFormat(typeItem));
          }
          objectSchema.addProperties(param.getName(), propertiesItem);
        }
      }
      rootSchema.addProperties("data", objectSchema);
    }

    ApiResponses apiResponses = new ApiResponses();
    apiResponses.addApiResponse("200",
        new ApiResponse().description("OK").content(
            new Content().addMediaType(
                "*/*",
                new MediaType().schema(rootSchema)
            )
        )
    );
    apiResponses.addApiResponse("201",
        new ApiResponse().description("Created")
    );
    apiResponses.addApiResponse("401",
        new ApiResponse().description("Unauthorized")
    );
    apiResponses.addApiResponse("403",
        new ApiResponse().description("Forbidden")
    );
    apiResponses.addApiResponse("404",
        new ApiResponse().description("Not Found")
    );
    return apiResponses;
  }

  private String getTypeFormat(ParamTypeEnum type) {
    String format = null;
    switch (type) {
      case LONG:
        format = "int64";
        break;
      case DOUBLE:
        format = "float32";
        break;
      case DATE:
        format = "date-time";
        break;
      case TIME:
        format = "time";
        break;
      case BOOLEAN:
      default:
        break;
    }
    return format;
  }
}
