package com.gitee.sqlrest.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DateCount {

  private String ofDate;
  private Long total;
  private Long success;
}
