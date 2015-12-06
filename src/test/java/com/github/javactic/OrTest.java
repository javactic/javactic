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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import javaslang.control.Failure;
import javaslang.control.Left;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Success;
import javaslang.control.Try;

public class OrTest {
	
	@Test
	public void from() {
		Or<String, String> good = Or.fromJavaOptional(Optional.of("good"), "bad");
		assertEquals("good", good.get());
		good = Or.fromJavaOptional(Optional.of("good"), () -> "fool");
		assertEquals("good", good.get());
		Or<String, String> bad = Or.fromJavaOptional(Optional.empty(), "bad");
		assertEquals("bad", bad.getBad());
		bad = Or.fromJavaOptional(Optional.empty(), () -> "bad");
		assertEquals("bad", bad.getBad());
		
		Or<String, String> any = Or.fromAny(Optional.of("good"), toOr("bad"));
		assertEquals("good", any.get());
		assertEquals("bad", bad.toAny(bad2 -> bad2.getBad()));
		
		good = Or.from(new Right<>("good"));
		assertEquals("good", good.get());
		bad = Or.from(new Left<>("bad"));
		assertEquals("bad", bad.getBad());
		
		good = Or.from(Option.some("good"), "bad");
		assertEquals("good", good.get());
		bad = Or.from(Option.none(), "bad");
		assertEquals("bad", bad.getBad());
        bad = Or.from(Option.none(), () -> "bad");
        assertEquals("bad", bad.getBad());		
		
		Or<String, Throwable> goodTry = Or.from(new Success<>("good"));
		assertEquals("good", goodTry.get());
		RuntimeException ex = new RuntimeException();
        Or<String, Throwable> badTry = Or.from(new Failure<>(ex));
        assertEquals(ex, badTry.getBad());
        
	}
	
	public static <G,B> Function<Optional<G>, Or<G, B>> toOr(B bad) {
		return opt -> opt.map(Or::<G,B> good).orElse(Or.<G,B>bad(bad));
	}
	
	@Test
	public void accumulating() {
	    
	    
//	    Or<String, String> good = Good.of("Good");
//	    good.<Object>map(s -> 123).getOrElse("");
	    
		Or<String, String> bad = Bad.of("Bad");
		Or<String, One<String>> badacc = bad.accumulating();
		assertTrue(badacc.isBad());
		Or<String, String> good = Good.of("Good");
		Or<String, One<String>> goodacc = good.accumulating();
		assertTrue(goodacc.isGood());
	}
	
	@Test
	public void map(){
		Or<String, String> good = Good.of("1");
		Or<Integer, String> goodI = good.map(s -> Integer.parseInt(s));
		assertEquals(1, goodI.get().intValue());
		Or<String, String> bad = Bad.of("2");
		Or<Integer, String> badI = bad.map(s -> Integer.parseInt(s));
		assertTrue(badI.isBad());
	}
	
	@Test
	public void badMap() {
		Or<String, String> good = Good.of("1");
		assertTrue(good.badMap(b -> b.toLowerCase()).isGood());
		Or<String, String> bad = Bad.of("foo");
		assertEquals("bad", bad.badMap(s -> "bad").getBad());
	}
	
	@Test
	public void flatMap() {
		Or<String, String> good = Good.of("1");
		Or<Integer, String> goodI = good.flatMap(s -> Good.of(Integer.parseInt(s)));
		assertEquals(1, goodI.get().intValue());
		Or<String, String> bad = Bad.of("");
		Or<Integer, String> badI = bad.flatMap(s -> Bad.of(""));
		assertTrue(badI.isBad());
	}
	
	@Test
	public void exists() {
		Or<String, String> good = Good.of("1");
		assertTrue(good.exists(s -> s.equals("1")));
		Or<String, String> bad = Bad.of("bad");
		assertFalse(bad.exists(s -> s.equals("bad")));
	}
	
	@Test
	public void forall() {
		Or<String, String> good = Good.of("1");
		assertTrue(good.forAll(s -> s.equals("1")));
		Or<String, String> bad = Bad.of("bad");
		assertTrue(bad.forAll(s -> s.equals("bad")));
	}
	
	@Test
	public void fold() {
		Or<String, String> good = Good.of("good");
		assertEquals("good", good.fold(s -> s, s -> "bad"));
		Or<String, String> bad = Bad.of("bad");
		assertEquals("bad", bad.fold(s -> "good", s -> s));
	}
	
	@Test
	public void forEach() {
		AtomicReference<String> foo = new AtomicReference<String>("bar");
		Or<String, String> good = Good.of("foo");
		good.forEach(s -> foo.set(s));
		assertEquals("foo", foo.get());
		Or<String, String> bad = Bad.of("baad");
		bad.forEach(s -> foo.set(s));
		assertEquals("foo", foo.get());
	}
	
	@Test
	public void getForBad() {
		Or<String, String> bad = Bad.of("bad");
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
		Or<String, String> good = Good.of("good");
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
		Or<String, String> good = Good.of("good");
		assertEquals("good", good.getOrElse("bad"));
		assertEquals("good", good.getOrElse(b -> "bad"));
		Or<String, String> bad = Bad.of("foo");
		assertEquals("bad", bad.getOrElse("bad"));
		assertEquals("bad2", bad.getOrElse(b -> "bad2"));
	}
	
	@Test
	public void orElse() {
		Or<String, String> or = Good.of("good");
		assertEquals("good", or.orElse(() -> Bad.of("bad")).get());
		assertEquals("good", or.orElse(Bad.of("bad")).get());
		or = Bad.of("foo");
		assertEquals("bad", or.orElse(() -> Bad.of("bad")).getBad());
		assertEquals("bad", or.orElse(Bad.of("bad")).getBad());
	}
	
	@Test
	public void recover() {
		Or<String, String> or = Good.of("good");
		assertEquals("good", or.recover(bad -> "recovered-bad").get());
		or = Bad.of("bad");
		assertEquals("recovered", or.recover(bad -> "recovered").get());
	}
	
	@Test
	public void recoverWith() {
		Or<String, String> or = Good.of("good");
		assertEquals("good", or.recoverWith(bad -> Bad.of(123)).get());
		or = Bad.of("bad");
		assertEquals(123, or.recoverWith(bad -> Bad.of(123)).getBad().intValue());
	}
	
	@Test
	public void swap() {
		Or<String, Integer> or = Good.of("good");
		assertEquals("good", or.swap().getBad());
		or = Bad.of(123);
		assertEquals(123, or.swap().get().intValue());
	}
	
	@Test
	public void toOptional() {
		Or<String, Integer> or = Good.of("good");
		assertEquals("good", or.toOption().get());
		or = Bad.of(123);
		assertFalse(or.toOption().isDefined());
	}
	
	@Test
	public void transform() {
		Or<String, Integer> or = Good.of("good");
		Or<String, Integer> tr = or.transform(g -> g.toUpperCase(), b -> b - 100);
		assertEquals("GOOD", tr.get());
		or = Bad.of(123);
		tr = or.transform(g -> g.toUpperCase(), b -> b - 100);
		assertEquals(23, tr.getBad().intValue());
	}
	
	@Test
	public void filter() {
		Or<Integer, String> or = Good.of(123);
		Function<Integer, Validation<String>> validator =  g -> g > 100 ? Validation.pass() : Validation.fail("fail");
		Or<Integer, String> filtered = or.filter(validator);
		assertEquals(123, filtered.get().intValue());
		filtered = Good.<Integer, String>of(99).filter(validator);
		assertEquals("fail", filtered.getBad());
		filtered = Bad.<Integer, String>of("foo").filter(validator);
		assertEquals("foo", filtered.getBad());
	}
	
	@Test
	public void misc() {
		assertTrue(Good.of("good").isGood());
		assertFalse(Good.of("good").isBad());
		
		assertTrue(Bad.of("bad").isBad());
		assertFalse(Bad.of("bad").isGood());
		
		assertEquals("Good(good)", Good.of("good").toString());
		assertEquals("Bad(bad)", Bad.of("bad").toString());
	}
	
	@Test
	public void toStuff() {
		assertEquals("good", Good.of("good").toJavaOptional().get());
		assertFalse(Bad.of("string").toJavaOptional().isPresent());
		
		assertEquals("good", Good.of("good").toEither().right().get());
		assertEquals("bad", Bad.of("bad").toEither().left().get());
		
		assertEquals(Try.of(() -> "good"), Or.toTry(Good.of("good")));
		assertEquals(
				new RuntimeException().getClass(), 
				Or.toTry(Bad.of(new RuntimeException())).getCause().getClass());
		
	}
	
	@Test
	public void withValues() {
		AtomicReference<String> value = new AtomicReference<>("empty");
		Good.<String, String>of("good").forEach(g -> value.set(g), b -> value.set(b));
		assertEquals("good", value.get());
		Bad.<String, String>of("bad").forEach(g -> value.set(g), b -> value.set(b));
		assertEquals("bad", value.get());
	}
	
	@Test
	public void hashCodeEqualsToString() {
	    Bad<Object, String> bad1 = Bad.of("b");
	    Bad<Object, String> bad2 = Bad.ofString("b");
	    Bad<Object, String> bad3 = Bad.ofString("too {}", "bad");
	    assertEquals(bad1, bad2);
	    assertEquals(bad1, bad1);
	    assertNotEquals(bad1, bad3);
	    assertNotEquals(bad1, null);
	    assertEquals(bad1.toString(), bad2.toString());
	    assertEquals(bad1.hashCode(), bad2.hashCode());
	    
	    Good<String, Object> good1 = Good.of("g");
	    Good<String, Object> good2 = Good.of("g");
	    Good<String, Object> good3 = Good.of("g2");
	    assertEquals(good1, good2);
	    assertEquals(good1, good1);
	    assertNotEquals(good1, good3);
	    assertNotEquals(good1, null);
        assertEquals(good1.toString(), good2.toString());
        assertEquals(good1.hashCode(), good2.hashCode());
        
        assertNotEquals(good1, bad1);
        assertNotEquals(bad1, good1);
        
        assertEquals(Bad.ofOneString("BofOneString"), Bad.ofOneString("BofOneString"));
	}
	
	@Test
	public void nullEquals() {
	    Good<String, String> g1 = Good.of(null);
	    Good<String, String> g2 = Good.of(null);
	    Good<String, String> g3 = Good.of("");
        assertEquals(g1, g2);
        assertEquals(g2, g1);
        assertNotEquals(g1, g3);
        assertEquals(g1.hashCode(), g2.hashCode());
        
        Bad<String, String> b1 = Bad.of(null);
        Bad<String, String> b2 = Bad.of(null);
        Bad<String, String> b3 = Bad.of("");
        assertEquals(b1, b2);
        assertEquals(b2, b1);
        assertNotEquals(b1, b3);
        assertEquals(b1.hashCode(), b2.hashCode());        
	}
	
}
