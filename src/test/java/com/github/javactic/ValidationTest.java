/**
 *    ___                       _   _      
 *   |_  |                     | | (_)     
 *     | | __ ___   ____ _  ___| |_ _  ___ 
 *     | |/ _` \ \ / / _` |/ __| __| |/ __|
 * /\__/ / (_| |\ V / (_| | (__| |_| | (__   -2015-
 * \____/ \__,_| \_/ \__,_|\___|\__|_|\___|
 *                                          
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
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
