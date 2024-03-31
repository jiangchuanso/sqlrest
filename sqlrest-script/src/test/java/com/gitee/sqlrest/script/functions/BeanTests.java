package com.gitee.sqlrest.script.functions;

import org.junit.Test;
import com.gitee.sqlrest.script.BaseTest;

public class BeanTests extends BaseTest {

	@Test
	public void map2bean() {
		System.out.println(execute("functions/map2bean.ms"));
	}

}
