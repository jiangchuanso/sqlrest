package com.gitee.sqlrest.core.exec.extractor;

import java.nio.charset.Charset;
import java.util.Map;
import javax.servlet.ServletInputStream;
import org.springframework.http.MediaType;

public interface HttpRequestBodyExtractor {

  boolean support(MediaType mediaType);

  Map<String, Object> read(Charset charset, ServletInputStream inputStream);
}
