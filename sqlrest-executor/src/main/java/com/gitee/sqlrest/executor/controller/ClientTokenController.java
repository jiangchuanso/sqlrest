package com.gitee.sqlrest.executor.controller;

import com.gitee.sqlrest.common.dto.AccessToken;
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
  public AccessToken generateToken(@RequestBody Map<String, String> body) {
    return clientTokenService.generateToken(body.get("clientId"), body.get("secret"));
  }
}
