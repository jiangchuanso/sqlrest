package com.gitee.sqlrest.script.compile;

import com.gitee.sqlrest.script.BaseTest;
import org.junit.Test;

public class CompileTests extends BaseTest {

	@Test
	public void defineVar() {
		System.out.println(execute("compile/var.ms"));
	}

	@Test
	public void operator() {
		System.out.println(execute("compile/operator.ms"));
	}

	@Test
	public void compile_combine_assign() {
		System.out.println(execute("compile/compile_combine_assign.ms"));
	}

}
