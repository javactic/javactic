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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javaslang.control.Try;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Validation;

public class OrTest {
	
	@Test
	public void from() {
		Or<String, String> good = Or.fromJavaOptional(Optional.of("good"), "bad");
		assertEquals("good", good.get());
		Or<String, String> bad = Or.fromJavaOptional(Optional.empty(), "bad");
		assertEquals("bad", bad.getBad());
		Or<String, String> any = Or.fromAny(Optional.of("good"), toOr("bad"));
		assertEquals("good", any.get());
		assertEquals("bad", bad.toAny(bad2 -> bad2.getBad()));
	}
	
	public static <G,B> Function<Optional<G>, Or<G, B>> toOr(B bad) {
		return opt -> opt.map(good -> (Or<G,B>)Or.<G,B>good(good)).orElse(Or.<G,B>bad(bad));
	}
	
	@Test
	public void accumulating() {
		Or<String, String> bad = Or.bad("Bad");
		Or<String, One<String>> badacc = bad.accumulating();
		assertTrue(badacc.isBad());
		Or<String, String> good = Or.good("Good");
		Or<String, One<String>> goodacc = good.accumulating();
		assertTrue(goodacc.isGood());
	}
	
	@Test
	public void map(){
		Or<String, String> good = Or.good("1");
		Or<Integer, String> goodI = good.map(s -> Integer.parseInt(s));
		assertEquals(1, goodI.get().intValue());
		Or<String, String> bad = Or.bad("2");
		Or<Integer, String> badI = bad.map(s -> Integer.parseInt(s));
		assertTrue(badI.isBad());
	}
	
	@Test
	public void badMap() {
		Or<String, String> good = Or.good("1");
		assertTrue(good.badMap(b -> b.toLowerCase()).isGood());
		Or<String, String> bad = Or.bad("foo");
		assertEquals("bad", bad.badMap(s -> "bad").getBad());
	}
	
	@Test
	public void flatMap() {
		Or<String, String> good = Or.good("1");
		Or<Integer, String> goodI = good.flatMap(s -> Or.good(Integer.parseInt(s)));
		assertEquals(1, goodI.get().intValue());
		Or<String, String> bad = Or.bad("");
		Or<Integer, String> badI = bad.flatMap(s -> Or.bad(""));
		assertTrue(badI.isBad());
	}
	
	@Test
	public void exists() {
		Or<String, String> good = Or.good("1");
		assertTrue(good.exists(s -> s.equals("1")));
		Or<String, String> bad = Or.bad("bad");
		assertFalse(bad.exists(s -> s.equals("bad")));
	}
	
	@Test
	public void forall() {
		Or<String, String> good = Or.good("1");
		assertTrue(good.forAll(s -> s.equals("1")));
		Or<String, String> bad = Or.bad("bad");
		assertTrue(bad.forAll(s -> s.equals("bad")));
	}
	
	@Test
	public void fold() {
		Or<String, String> good = Or.good("good");
		assertEquals("good", good.fold(s -> s, s -> "bad"));
		Or<String, String> bad = Or.bad("bad");
		assertEquals("bad", bad.fold(s -> "good", s -> s));
	}
	
	@Test
	public void forEach() {
		AtomicReference<String> foo = new AtomicReference<String>("bar");
		Or<String, String> good = Or.good("foo");
		good.forEach(s -> foo.set(s));
		assertEquals("foo", foo.get());
		Or<String, String> bad = Or.bad("baad");
		bad.forEach(s -> foo.set(s));
		assertEquals("foo", foo.get());
	}
	
	@Test
	public void getForBad() {
		Or<String, String> bad = Or.bad("bad");
		try {
			bad.get();
			Assert.fail("should throw exception");
		}
		catch(NoSuchElementException e) {
			// expected
		}
	}
	
	@Test
	public void getBadForGood() {
		Or<String, String> good = Or.good("good");
		try {
			good.getBad();
			Assert.fail("should throw exception");
		}
		catch(NoSuchElementException e) {
			// expected
		}
	}
	
	@Test
	public void getOrElse() {
		Or<String, String> good = Or.good("good");
		assertEquals("good", good.getOrElse("bad"));
		assertEquals("good", good.getOrElse(() -> "bad"));
		Or<String, String> bad = Or.bad("foo");
		assertEquals("bad", bad.getOrElse("bad"));
		assertEquals("bad", bad.getOrElse(() -> "bad"));
	}
	
	@Test
	public void orElse() {
		Or<String, String> or = Or.good("good");
		assertEquals("good", or.orElse(() -> Or.bad("bad")).get());
		or = Or.bad("foo");
		assertEquals("bad", or.orElse(() -> Or.bad("bad")).getBad());
	}
	
	@Test
	public void recover() {
		Or<String, String> or = Or.good("good");
		assertEquals("good", or.recover(bad -> "recovered-bad").get());
		or = Or.bad("bad");
		assertEquals("recovered", or.recover(bad -> "recovered").get());
	}
	
	@Test
	public void recoverWith() {
		Or<String, String> or = Or.good("good");
		assertEquals("good", or.recoverWith(bad -> Or.bad(123)).get());
		or = Or.bad("bad");
		assertEquals(123, or.recoverWith(bad -> Or.bad(123)).getBad().intValue());
	}
	
	@Test
	public void swap() {
		Or<String, Integer> or = Or.good("good");
		assertEquals("good", or.swap().getBad());
		or = Or.bad(123);
		assertEquals(123, or.swap().get().intValue());
	}
	
	@Test
	public void toOptional() {
		Or<String, Integer> or = Or.good("good");
		assertEquals("good", or.toOption().get());
		or = Or.bad(123);
		assertFalse(or.toOption().isDefined());
	}
	
	@Test
	public void transform() {
		Or<String, Integer> or = Or.good("good");
		Or<String, Integer> tr = or.transform(g -> g.toUpperCase(), b -> b - 100);
		assertEquals("GOOD", tr.get());
		or = Or.bad(123);
		tr = or.transform(g -> g.toUpperCase(), b -> b - 100);
		assertEquals(23, tr.getBad().intValue());
	}
	
	@Test
	public void filter() {
		Or<Integer, String> or = Or.good(123);
		Function<Integer, Validation<String>> validator =  g -> g > 100 ? Validation.pass() : Validation.fail("fail");
		Or<Integer, String> filtered = or.filter(validator);
		assertEquals(123, filtered.get().intValue());
		filtered = Or.<Integer, String>good(99).filter(validator);
		assertEquals("fail", filtered.getBad());
		filtered = Or.<Integer, String>bad("foo").filter(validator);
		assertEquals("foo", filtered.getBad());
	}
	
	@Test
	public void misc() {
		assertTrue(Or.good("good").isGood());
		assertFalse(Or.good("good").isBad());
		
		assertTrue(Or.bad("bad").isBad());
		assertFalse(Or.bad("bad").isGood());
		
		assertEquals("Good(good)", Or.good("good").toString());
		assertEquals("Bad(bad)", Or.bad("bad").toString());
	}
	
	@Test
	public void toStuff() {
		assertEquals("good", Or.good("good").toJavaOptional().get());
		assertFalse(Or.bad("string").toJavaOptional().isPresent());
		
		assertEquals("good", Or.good("good").toEither().right().get());
		assertEquals("bad", Or.bad("bad").toEither().left().get());
		
		assertEquals(Try.of(() -> "good"), Good.of("good").toTry());
		assertEquals(
				"foo", 
				Bad.of("foo").toTry().getCause().getMessage());
		
		assertEquals(
				new RuntimeException().getClass(), 
				Bad.of(new RuntimeException()).toTry().getCause().getClass());
		
	}
	
	@Test
	public void withValues() {
		AtomicReference<String> value = new AtomicReference<>("empty");
		Or.<String, String>good("good").forEach(g -> value.set(g), b -> value.set(b));
		assertEquals("good", value.get());
		Or.<String, String>bad("bad").forEach(g -> value.set(g), b -> value.set(b));
		assertEquals("bad", value.get());
	}
	
}
