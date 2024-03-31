package com.gitee.sqlrest.script.exception;

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String module) {
		super(module);
	}
}
