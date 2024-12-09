package com.gitee.sqlrest.common.dto;

@FunctionalInterface
public interface ThreeConsumer<X, Y, Z> {

  void accept(X arg1, Y arg2, Z arg3);
}
