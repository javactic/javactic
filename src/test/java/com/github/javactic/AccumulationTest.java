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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javaslang.collection.Vector;

import org.junit.Test;

import com.github.javactic.Accumulation;
import com.github.javactic.Every;
import com.github.javactic.Fail;
import com.github.javactic.One;
import com.github.javactic.Or;
import com.github.javactic.Pass;
import com.github.javactic.Validation;

public class AccumulationTest {

	
	@Test
	public void testCombined() {
		Vector<Or<String, Every<String>>> vec = Vector.ofAll(Or.good("A"), Or.good("B"));
		Or<Vector<String>, Every<String>> result = Accumulation.combined(vec, Vector.collector());
		assertTrue(result.isGood());
		assertEquals("A", result.get().head());
		assertEquals("B", result.get().tail().head());
		
		List<Or<String, Every<String>>> list = Arrays.asList(Or.good("A"), Or.bad(Every.of("B")));
		Or<List<String>, Every<String>> result2 = Accumulation.combined(list, Collectors.toList());
		assertTrue(result2.isBad());
		assertEquals("B", result2.getBad().head());
	}
	
	@Test
	public void testValidatedBy() {
		Vector<Integer> vec = Vector.ofAll(1,2,3);
		Function<Integer, Or<Integer, Every<String>>> f = i -> {
			if(i < 10) return Or.good(i);
			else return Or.bad(One.of("wasn't under 10"));
		};
		Or<Vector<Integer>, Every<String>> res = Accumulation.validatedBy(vec, f, Vector.collector());
		assertTrue(res.isGood());
		assertEquals(vec, res.get());
		res = Accumulation.validatedBy(Vector.of(11), f, Vector.collector());
		assertTrue(res.isBad());
		assertTrue(res.getBad() instanceof One);
	}
	
	@Test
	public void testWhen() {
		Function<String, Validation<String>> f1 = f -> f.startsWith("s") ? Pass.instance() : Fail.of("does not start with s");
		Function<String, Validation<String>> f2 = f -> f.length() > 4 ? Fail.of("too long") : Pass.instance();
		Or<String, Every<String>> res = Accumulation.when(Or.bad(One.of("bad")), f1, f2);
		assertEquals("bad", res.getBad().get(0));
		res = Accumulation.when(Or.good("sub"), f1, f2);
		assertTrue(res.isGood());
		res = Accumulation.when(Or.good("fubiluuri"), f1, f2);
		assertTrue(res.isBad());
	}
	
	@Test
	public void testWithGood(){
		Function<Or<String, Every<String>>[], Or<?, Every<String>>> fun = 
				ors -> Accumulation.withGood(ors[0], ors[1], (a,b) -> "");
		testWithF(fun, 2);
		fun = o -> Accumulation.withGood(o[0], o[1], o[2], (a, b, c) -> "");
		testWithF(fun, 3);
		fun = o -> Accumulation.withGood(o[0],o[1],o[2],o[3], (a,b,c,d) -> "");
		testWithF(fun, 4);
		fun = o -> Accumulation.withGood(o[0],o[1],o[2],o[3],o[4], (a,b,c,d,e) -> "");
		testWithF(fun, 5);
		fun = o -> Accumulation.withGood(o[0],o[1],o[2],o[3],o[4],o[5], (a,b,c,d,e,f) -> "");
		testWithF(fun, 6);
		fun = o -> Accumulation.withGood(o[0],o[1],o[2],o[3],o[4],o[5],o[6], (a,b,c,d,e,f,g) -> "");
		testWithF(fun, 7);
		fun = o -> Accumulation.withGood(o[0],o[1],o[2],o[3],o[4],o[5],o[6],o[7], (a,b,c,d,e,f,g,h) -> "");
		testWithF(fun, 8);
	}
	
	@Test
	public void zips() {
		Function<Or<String, Every<String>>[], Or<?, Every<String>>> fun = ors -> Accumulation.zip(ors[0], ors[1]);
		testWithF(fun, 2);
		fun = o -> Accumulation.zip(o[0], o[1], o[2]);
		testWithF(fun, 3);
		fun = o -> Accumulation.zip(o[0],o[1],o[2],o[3]);
		testWithF(fun, 4);
		fun = o -> Accumulation.zip(o[0],o[1],o[2],o[3],o[4]);
		testWithF(fun, 5);
		fun = o -> Accumulation.zip(o[0],o[1],o[2],o[3],o[4],o[5]);
		testWithF(fun, 6);
		fun = o -> Accumulation.zip(o[0],o[1],o[2],o[3],o[4],o[5],o[6]);
		testWithF(fun, 7);
		fun = o -> Accumulation.zip(o[0],o[1],o[2],o[3],o[4],o[5],o[6],o[7]);
		testWithF(fun, 8);
		
	}
	
	private void testWithF(Function<Or<String, Every<String>>[], Or<?, Every<String>>> f, int size) {
		@SuppressWarnings("unchecked")
		Or<String, Every<String>>[] ors = new Or[size];
		for(int i = 0; i <= ors.length; i++){
			for(int j = 0; j < ors.length; j++) {
				if(j == i) ors[j] = Or.bad(One.of("bad"));
				else ors[j] = Or.good("good");
			}
			Or<?, Every<String>> val = f.apply(ors);
			if(i < ors.length)
				assertTrue(val.isBad());
			else
				assertTrue(val.isGood());
		}
	}
}
