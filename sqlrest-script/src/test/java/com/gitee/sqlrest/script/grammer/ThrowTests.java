package com.gitee.sqlrest.script.grammer;

import com.gitee.sqlrest.script.exception.MagicScriptException;
import com.gitee.sqlrest.script.exception.MagicScriptRuntimeException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.gitee.sqlrest.script.BaseTest;

public class ThrowTests extends BaseTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void throw_1() {
		try {
			execute("grammar/throw_1.ms");
		} catch (MagicScriptException e) {
			Throwable cause = e.getCause();
			Assert.assertEquals(MagicScriptRuntimeException.class, cause.getClass());
			Assert.assertEquals("ex", cause.getMessage());
		}
	}

	@Test
	public void throw_2() {
		try {
			execute("grammar/throw_2.ms");
		} catch (MagicScriptException e) {
			Throwable cause = e.getCause();
			Assert.assertEquals(ArithmeticException.class, cause.getClass());
		}
	}
}
