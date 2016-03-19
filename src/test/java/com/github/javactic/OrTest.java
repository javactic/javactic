/**
 * ___                       _   _
 * |_  |                     | | (_)
 * | | __ ___   ____ _  ___| |_ _  ___
 * | |/ _` \ \ / / _` |/ __| __| |/ __|
 * /\__/ / (_| |\ V / (_| | (__| |_| | (__   -2015-
 * \____/ \__,_| \_/ \__,_|\___|\__|_|\___|
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.github.javactic;

import javaslang.control.Either;
import javaslang.control.Option;
import javaslang.control.Try;
import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.*;

public class OrTest {

  @Test
  public void from() {
    Or<String, String> good = Or.fromJavaOptional(Optional.of("success"), "failure");
    assertEquals("success", good.get());
    good = Or.fromJavaOptional(Optional.of("success"), () -> "fool");
    assertEquals("success", good.get());
    Or<String, String> bad = Or.fromJavaOptional(Optional.empty(), "failure");
    assertEquals("failure", bad.getBad());
    bad = Or.fromJavaOptional(Optional.empty(), () -> "failure");
    assertEquals("failure", bad.getBad());

    Or<Object, Object> any = Or.fromAny(Optional.of("success"), toOr("failure"));
    assertEquals("success", any.get());
    assertEquals("failure", bad.toAny(Or::getBad));

    good = Or.from(Either.right("success"));
    assertEquals("success", good.get());
    bad = Or.from(Either.left("failure"));
    assertEquals("failure", bad.getBad());

    good = Or.from(Option.some("success"), "failure");
    assertEquals("success", good.get());
    bad = Or.from(Option.none(), "failure");
    assertEquals("failure", bad.getBad());
    bad = Or.from(Option.none(), () -> "failure");
    assertEquals("failure", bad.getBad());

    Or<String, Throwable> goodTry = Or.from(Try.success("success"));
    assertEquals("success", goodTry.get());
    RuntimeException ex = new RuntimeException();
    Or<String, Throwable> badTry = Or.from(Try.failure(ex));
    assertEquals(ex, badTry.getBad());

  }

  public static <G, B> Function<Optional<G>, Or<G, B>> toOr(B bad) {
    return opt -> opt.map(Or::<G, B>good).orElse(Or.<G, B>bad(bad));
  }

  @Test
  public void accumulating() {
    Or<String, String> bad = Bad.of("Bad");
    Or<String, One<String>> badacc = bad.accumulating();
    assertTrue(badacc.isBad());
    Or<String, String> good = Good.of("Good");
    Or<String, One<String>> goodacc = good.accumulating();
    assertTrue(goodacc.isGood());
  }

  @Test
  public void map() {
    Or<String, String> good = Good.of("1");
    Or<Integer, String> goodI = good.map(Integer::parseInt);
    assertEquals(1, goodI.get().intValue());
    Or<String, String> bad = Bad.of("2");
    Or<Integer, String> badI = bad.map(Integer::parseInt);
    assertTrue(badI.isBad());
  }

  @Test
  public void badMap() {
    Or<String, String> good = Good.of("1");
    assertTrue(good.badMap(String::toLowerCase).isGood());
    Or<String, String> bad = Bad.of("foo");
    assertEquals("failure", bad.badMap(s -> "failure").getBad());
  }

  @Test
  public void contains() {
    Or<String, String> good1 = Good.of("1");
    assertTrue(good1.contains("1"));
    assertFalse(good1.containsBad("1"));

    Or<String, String> bad1 = Bad.of("failure");
    assertTrue(bad1.containsBad("failure"));
    assertFalse(bad1.contains("failure"));

    Or<String[], String[]> good2 = Good.of(new String[]{"1"});
    assertTrue(good2.contains(new String[]{"1"}));
  }

  @Test
  public void casts() {
    Bad.of("b").asOr();
    Good.of("g").asOr();
  }

  @Test
  public void flatMap() {
    Or<String, String> good = Good.of("1");
    Function<String, Good<Integer, String>> f = s -> Good.of(Integer.parseInt(s));
    Or<Integer, String> goodI = good.flatMap(f);
    assertEquals(1, goodI.get().intValue());
    Or<String, String> bad = Bad.of("");
    Or<Integer, String> badI = bad.flatMap(s -> Bad.of(""));
    assertTrue(badI.isBad());
  }

  @Test
  public void exists() {
    Or<String, String> good = Good.of("1");
    assertTrue(good.exists(s -> s.equals("1")));
    Or<String, String> bad = Bad.of("failure");
    assertFalse(bad.exists(s -> s.equals("failure")));
  }

  @Test
  public void forall() {
    Or<String, String> good = Good.of("1");
    assertTrue(good.forAll(s -> s.equals("1")));
    Or<String, String> bad = Bad.of("failure");
    assertTrue(bad.forAll(s -> s.equals("failure")));
  }

  @Test
  public void fold() {
    Or<String, String> good = Good.of("success");
    assertEquals("success", good.fold(s -> s, s -> "failure"));
    Or<String, String> bad = Bad.of("failure");
    assertEquals("failure", bad.fold(s -> "success", s -> s));
  }

  @Test
  public void forEach() {
    AtomicReference<String> foo = new AtomicReference<>("bar");
    Or<String, String> good = Good.of("foo");
    good.forEach(foo::set);
    assertEquals("foo", foo.get());
    Or<String, String> bad = Bad.of("baad");
    bad.forEach(foo::set);
    assertEquals("foo", foo.get());
  }

  @Test
  public void getForBad() {
    Or<String, String> bad = Bad.of("failure");
    try {
      bad.get();
      Assert.fail("should throw exception");
    } catch (NoSuchElementException e) {
      // expected
    }
  }

  @Test
  public void getBadForGood() {
    Or<String, String> good = Good.of("success");
    try {
      good.getBad();
      Assert.fail("should throw exception");
    } catch (NoSuchElementException e) {
      // expected
    }
  }

  @Test
  public void getOrElse() {
    Or<String, String> good = Good.of("success");
    assertEquals("success", good.getOrElse("failure"));
    assertEquals("success", good.getOrElse(b -> "failure"));
    Or<String, String> bad = Bad.of("foo");
    assertEquals("failure", bad.getOrElse("failure"));
    assertEquals("bad2", bad.getOrElse(b -> "bad2"));
  }

  @Test
  public void orElse() {
    Or<String, String> or = Good.of("success");
    assertEquals("success", or.orElse(() -> Bad.of("failure")).get());
    assertEquals("success", or.orElse(Bad.of("failure")).get());
    or = Bad.of("foo");
    assertEquals("failure", or.orElse(() -> Bad.of("failure")).getBad());
    assertEquals("failure", or.orElse(Bad.of("failure")).getBad());
  }

  @Test
  public void recover() {
    Or<String, String> or = Good.of("success");
    assertEquals("success", or.recover(bad -> "recovered-failure").get());
    or = Bad.of("failure");
    assertEquals("recovered", or.recover(bad -> "recovered").get());
  }

  @Test
  public void recoverWith() {
    Or<String, String> or = Good.of("success");
    assertEquals("success", or.recoverWith(bad -> Bad.of(123)).get());
    or = Bad.of("failure");
    assertEquals(123, or.recoverWith(bad -> Bad.of(123)).getBad().intValue());
  }

  @Test
  public void swap() {
    Or<String, Integer> or = Good.of("success");
    assertEquals("success", or.swap().getBad());
    or = Bad.of(123);
    assertEquals(123, or.swap().get().intValue());
  }

  @Test
  public void toOptional() {
    Or<String, Integer> or = Good.of("success");
    assertEquals("success", or.toOption().get());
    or = Bad.of(123);
    assertFalse(or.toOption().isDefined());
  }

  @Test
  public void transform() {
    Or<String, Integer> or = Good.of("good");
    Or<String, Integer> tr = or.transform(String::toUpperCase, b -> b - 100);
    assertEquals("GOOD", tr.get());
    or = Bad.of(123);
    tr = or.transform(String::toUpperCase, b -> b - 100);
    assertEquals(23, tr.getBad().intValue());
  }

  @Test
  public void filter() {
    Or<Integer, String> or = Good.of(123);
    Function<Integer, Validation<String>> validator = g -> g > 100 ? Validation.pass() : Validation.fail("fail");
    Or<Integer, String> filtered = or.filter(validator);
    assertEquals(123, filtered.get().intValue());
    filtered = Good.<Integer, String>of(99).filter(validator);
    assertEquals("fail", filtered.getBad());
    filtered = Bad.<Integer, String>of("foo").filter(validator);
    assertEquals("foo", filtered.getBad());
  }

  @Test
  public void misc() {
    assertTrue(Good.of("success").isGood());
    assertFalse(Good.of("success").isBad());

    assertTrue(Bad.of("failure").isBad());
    assertFalse(Bad.of("failure").isGood());

    assertEquals("Good(success)", Good.of("success").toString());
    assertEquals("Bad(failure)", Bad.of("failure").toString());
  }

  @Test
  public void toStuff() {
    assertEquals("success", Good.of("success").toJavaOptional().get());
    assertFalse(Bad.of("string").toJavaOptional().isPresent());

    assertEquals("success", Good.of("success").toEither().right().get());
    assertEquals("failure", Bad.of("failure").toEither().left().get());

    Try<Object> try1 = Or.toTry(Good.<String, RuntimeException>of("success"));
    assertEquals(Try.of(() -> "success"), try1);
    assertEquals(
      RuntimeException.class,
      Or.toTry(Bad.of(new RuntimeException())).getCause().getClass());
  }

  @Test
  public void withValues() {
    AtomicReference<String> value = new AtomicReference<>("empty");
    Good.<String, String>of("success").forEach(value::set, value::set);
    assertEquals("success", value.get());
    Bad.<String, String>of("failure").forEach(value::set, value::set);
    assertEquals("failure", value.get());
  }

  @Test
  public void hashCodeEqualsToString() {
    Bad<Object, String> bad1 = Bad.of("b");
    Bad<Object, String> bad2 = Bad.ofString("b");
    Bad<Object, String> bad3 = Bad.ofString("too {}", "failure");
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
