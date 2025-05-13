package com.gitee.sqlrest.executor.controller;

import com.gitee.sqlrest.common.dto.AccessToken;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.core.servlet.ClientTokenService;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class ClientTokenController {

  @Resource
  private ClientTokenService clientTokenService;

  @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity generateToken(@RequestBody Map<String, String> body) {
    try {
      AccessToken token = clientTokenService.generateToken(body.get("clientId"), body.get("secret"));
      return ResultEntity.success(token);
    } catch (CommonException e) {
      return ResultEntity.failed(e.getCode(), e.getMessage());
    } catch (Exception e) {
      return ResultEntity.failed(e.getMessage());
    }
  }
}
