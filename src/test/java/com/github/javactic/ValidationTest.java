package com.github.javactic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Fail;
import com.github.javactic.Pass;

public class ValidationTest {

	@Test
	public void passTest() {
		assertTrue(Pass.instance().and(Pass.instance()).isPass());
		assertFalse(Pass.instance().isFail());
		try{
			Pass.instance().getError();
			Assert.fail();
		} catch (Exception e) {
			// expected
		}
	}
	
	@Test
	public void failTest() {
		assertTrue(Fail.of("error").and(Pass.instance()).isFail());
		assertFalse(Fail.of("error").isPass());
		Assert.assertEquals("error", Fail.of("error").getError());
	}
}
