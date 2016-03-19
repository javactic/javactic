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

import javaslang.Tuple;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Stream;
import javaslang.collection.Vector;
import javaslang.control.Option;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

import static org.junit.Assert.*;

public class EveryTest {

  @Test
  public void testTrivial() {
    Every<String> every = Every.of("a", "b");
    assertTrue(every.isDefinedAt(0));
    assertTrue(every.isDefinedAt(1));
    assertFalse(every.isDefinedAt(2));
    assertFalse(every.isDefinedAt(-1));
    assertFalse(every.isEmpty());
    assertTrue(every.headOption().isDefined());
    assertTrue(every.lastOption().isDefined());
    assertEquals(2, every.size());
    assertEquals(0, every.lengthCompare(2));
    assertTrue(every.sameElements(Stream.of("a", "b")));
    assertTrue(every.nonEmpty());
    assertNull(Helper.parse(null));
  }

  @Test
  public void reverse() {
    Every<String> every = Every.of("a", "b");
    Every<String> reversed = Every.of("b", "a");
    assertEquals(reversed, every.reverse());
    assertEquals(Vector.ofAll(every.reverse().iterator()), Vector.ofAll(every.reverseIterator()));

  }

  @Test
  public void testConversions() {
    Every<String> every = Every.of("a", "b");
    assertEquals(every.length(), every.toArray().length());
    assertEquals(every.length(), every.toJavaArray(String.class).length);
    assertEquals(every.length(), every.toJavaList().size());
    assertEquals(every.length(), every.toJavaMap(s -> Tuple.of(s, s)).size());
    assertEquals(every.length(), every.toJavaSet().size());
    assertEquals(every.length(), every.toJavaStream().count());
    assertEquals(every.length(), every.toList().length());
    assertEquals(every.length(), every.toMap(s -> Tuple.of(s, s)).length());
    assertEquals(every.length(), every.toSeq().length());
    assertEquals(every.length(), every.toSet().length());
    assertEquals(every.length(), every.toStream().length());
    assertEquals(every.length(), every.toTraversable().length());
    assertEquals(every.length(), every.toVector().length());
  }

  @Test
  public void hashCodeEqualsToString() {
    Many<String> pair1 = Many.of("a", "b");
    Many<String> pair2 = Many.of("a", "b");
    assertEquals(pair1, pair2);
    assertEquals(pair1.hashCode(), pair2.hashCode());
    assertEquals(pair1.toString(), pair2.toString());

    One<String> single1 = One.ofString("c");
    One<String> single2 = One.of("c");
    assertEquals(single1, single2);
    assertEquals(single1.hashCode(), single2.hashCode());
    assertEquals(single1.toString(), single2.toString());

    assertNotEquals(pair1, single1);
    assertNotEquals(single2, pair2);
  }

  @Test
  public void lift() {
    Every<Integer> e = Every.of(4, 8, 12);
    IntFunction<Option<Integer>> lift = e.lift();
    assertEquals(8, lift.apply(1).get().intValue());
    assertTrue(lift.apply(3).isEmpty());
  }

  @Test
  public void orElse() {
    Every<Integer> e = Every.of(4, 8, 12);
    IntFunction<Integer> orElse = e.orElse(i -> 66);
    assertEquals(8, orElse.apply(1).intValue());
    assertEquals(66, orElse.apply(5).intValue());
  }

  @Test
  public void runWith() {
    Every<Integer> e = Every.of(4, 8, 12);
    AtomicReference<String> ref = new AtomicReference<>();
    IntPredicate runWith = e.runWith(i -> ref.set("" + i));
    assertFalse(runWith.test(6));
    assertNull(ref.get());
    assertTrue(runWith.test(1));
    assertEquals("8", ref.get());
  }

  @Test
  public void addString() {
    Every<String> e = Every.of("a", "b", "c");
    StringBuilder sb = e.addString(new StringBuilder());
    assertEquals("abc", sb.toString());
    sb = e.addString(new StringBuilder(), ":");
    assertEquals("a:b:c", sb.toString());
    sb = e.addString(new StringBuilder(), "[", ":", "]");
    assertEquals("[a:b:c]", sb.toString());
  }

  @Test
  public void append() {
    Every<String> e = Every.of("a", "b", "c");
    assertEquals("d", e.append("d").last());
    assertEquals("d", e.appendAll(Stream.of("d")).last());
    assertEquals("d", e.appendAll(Every.of("d")).last());
    assertEquals("c", e.appendAll(Stream.empty()).last());
  }

  @Test
  public void applyAndGet() {
    Every<String> e = Every.of("a", "b", "c");
    assertEquals("a", e.get(0)); // get calls apply, no separate test
    assertEquals("c", e.getOrElse(2, i -> "x")); // idem
    assertEquals("d", e.getOrElse(3, i -> "d"));

    assertEquals("c", e.getOrElse(2, "x"));
    assertEquals("d", e.getOrElse(3, "d"));
  }

  @Test
  public void compose() {
    Every<String> e = Every.of("1", "2");
    Function<String, String> compose = e.compose((String a) -> Integer.parseInt(a) - 1);
    assertEquals("1", compose.apply("1"));
  }

  @Test
  public void containsAndContainsSlice() {
    Every<String> e = Every.of("a", "b", "b", "c");
    assertTrue(e.contains("a"));
    assertTrue(e.containsSlice(Stream.of("b", "c")));
  }

  @Test
  public void copyToArray() {
    Every<String> e = Every.of("a", "b", "c");
    String[] target = new String[2]; // target < this
    e.copyToJavaArray(target);
    assertEquals(List.of("a", "b"), List.of(target));

    target = new String[4]; // target > this
    e.copyToJavaArray(target);
    assertEquals(List.of("a", "b", "c", null), List.of(target));

    target = new String[1];
    e.copyToJavaArray(target, 4, 10);
    assertEquals(null, target[0]);

    target = new String[5];
    e.copyToJavaArray(target, 0, 10);
    assertEquals(List.of("a", "b", "c", null, null), List.of(target));
  }

  @Test
  public void corresponds() {
    Every<String> e = Every.of("1", "2", "3");
    Stream<Integer> yes = Stream.of(1, 2, 3);
    Stream<Integer> not = Stream.of(1, 2, 3, 4);
    assertTrue(e.corresponds(yes, (s, i) -> Integer.parseInt(s) == i));
    assertFalse(e.corresponds(not, (s, i) -> Integer.parseInt(s) == i));
  }

  @Test
  public void count() {
    Every<Integer> e = Every.of(1, 2, 3, 4, 5);
    assertEquals(3, e.count(i -> i < 4));
  }

  @Test
  public void distinct() {
    Every<Integer> e = Every.of(1, 2, 2, 3, 3);
    assertEquals(3, e.distinct().length());
  }

  @Test
  public void forEach() {
    Every<Integer> e = Every.of(1, 2, 3, 4, 5);
    java.util.List<Integer> list = new ArrayList<>();
    e.forEach(list::add);
    assertEquals(e.toJavaList(), list);
  }

  @Test
  public void forAll() {
    Every<Integer> e = Every.of(1, 2, 3, 4, 5);
    assertTrue(e.forAll(i -> i < 6));
  }

  @Test
  public void flatten() {
    Every<Every<Integer>> e = Every.of(One.of(1), One.of(2), Many.of(3, 4));
    assertEquals(Every.of(1, 2, 3, 4), Every.flatten(e));
  }

  @Test
  public void fold() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(10, e.fold(0, (i1, i2) -> i1 + i2).intValue());
    assertEquals("1234", e.foldLeft("", (s1, i1) -> s1 + i1));
    assertEquals("4321", e.foldRight("", (i1, s1) -> s1 + i1));
  }

  @Test
  public void groupBy() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    Map<String, Every<Integer>> groupBy = e.groupBy(i -> (i % 2 == 0) ? "even" : "odd");
    assertEquals(Every.of(1, 3), groupBy.get("odd").get());
    assertEquals(Every.of(2, 4), groupBy.get("even").get());
  }

  @Test
  public void groupedTest1() {
    Every<String> e = Every.of("a", "b", "c");
    Iterator<Every<String>> it = e.grouped(2);
    Every<String> first = it.next();
    Every<String> secnd = it.next();
    assertEquals("a", first.get(0));
    assertEquals("b", first.get(1));
    assertEquals("c", secnd.get(0));
    try {
      it.next();
      Assert.fail("should have two");
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void groupedTest2() {
    Every<String> e = Every.of("a");
    Iterator<Every<String>> it = e.grouped(2);
    Every<String> first = it.next();
    assertEquals("a", first.get(0));
    try {
      it.next();
      Assert.fail("should have two");
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void groupedTest3() {
    Every<String> e = Every.of("a", "b");
    Iterator<Every<String>> it = e.grouped(2);
    Every<String> first = it.next();
    assertEquals("a", first.get(0));
    assertEquals("b", first.get(1));
    try {
      it.next();
      Assert.fail("should have two");
    } catch (NoSuchElementException ex) {
      // expected
    }
  }

  @Test
  public void indexOf() {
    Every<String> e = Every.of("a", "b", "c");
    assertEquals(0, e.indexOf("a"));
    assertEquals(2, e.indexOf("c", 1));
    assertEquals(1, e.indexOf("b", 1));
    assertEquals(2, e.indexOf("c", 2));
    assertEquals(-1, e.indexOf("c", 3));
  }

  @Test
  public void indexOfSlice() {
    Every<String> e = Every.of("a", "b", "c", "b", "c");
    Stream<String> s = Stream.of("b", "c");
    assertEquals(1, e.indexOfSlice(s));
    assertEquals(3, e.indexOfSlice(s, 2));
  }

  @Test
  public void indexWhere() {
    Every<Integer> e = Every.of(3, 4, 5, 6, 7, 1, 2, 5, 6, 7);
    assertEquals(2, e.indexWhere(i -> i > 4));
    assertEquals(7, e.indexWhere(i -> i > 4, 5));
  }

  @Test
  public void lastIndex() {
    Every<Integer> e = Every.of(3, 4, 5, 6, 7, 1, 2, 5, 6, 7);
    assertEquals(9, e.lastIndexOf(7));
    assertEquals(4, e.lastIndexOf(7, 8));
  }

  @Test
  public void lastIndexOfSlice() {
    Every<Integer> e = Every.of(1, 2, 3, 1, 2, 3);
    Stream<Integer> s = Stream.of(1, 2, 3);
    assertEquals(3, e.lastIndexOfSlice(s));
    assertEquals(3, e.lastIndexOfSlice(s, 6));
    // assertEquals(0, e.lastIndexOfSlice(s, 2)); Javaslang bug
  }

  @Test
  public void lastIndexWhere() {
    Every<Integer> e = Every.of(3, 4, 5, 6, 7, 1, 2, 5, 6, 7);
    assertEquals(9, e.lastIndexWhere(i -> i > 4));
    assertEquals(4, e.lastIndexWhere(i -> i > 4, 5));
  }

  @Test
  public void map() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(Every.of("1", "2", "3", "4"), e.map(i -> "" + i));
  }

  @Test
  public void max() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(4, e.max().get().intValue());
    assertEquals(1, e.maxBy(Comparator.reverseOrder()).intValue());
    assertEquals(4, e.maxBy(i -> "" + i).intValue());
  }

  @Test
  public void min() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(1, e.min().get().intValue());
    assertEquals(4, e.minBy(Comparator.reverseOrder()).intValue());
    assertEquals(1, e.minBy(i -> "" + i).intValue());
  }

  @Test
  public void padTo() {
    Every<Integer> e = Every.of(1, 2);
    assertEquals(Every.of(1, 2, 3, 3), e.padTo(4, 3));
  }

  @Test
  public void reduces() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(10, e.reduce((i1, i2) -> i1 + i2).intValue());
    assertEquals(10, e.reduceOption((i1, i2) -> i1 + i2).get().intValue());

    Every<String> e2 = Every.of("1", "2", "3", "4");
    assertEquals("1234", e2.reduceLeft((s1, s2) -> s1 + s2));
    assertEquals("1234", e2.reduceLeftOption((s1, s2) -> s1 + s2).get());
    assertEquals("4321", e2.reduceRight((s2, s1) -> s1 + s2));
    assertEquals("4321", e2.reduceRightOption((s2, s1) -> s1 + s2).get());
  }

  @Test
  public void reverseMap() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertEquals(Every.of("4", "3", "2", "1"), e.reverseMap(i -> "" + i));
  }

  @Test
  public void endsWith() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    assertTrue(e.endsWith(Stream.of(3, 4)));
  }

  @Test
  public void flatMap() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    Every<Integer> exp = Every.of(1, 1, 2, 2, 3, 3, 4, 4);
    assertEquals(exp, e.flatMap(i -> Many.of(i, i)));
  }

  @Test
  public void permutations() {
    Every<Integer> e = Every.of(1, 2, 3);
    Vector<Every<Integer>> permutations = Vector.ofAll(e.permutations());
    assertTrue(permutations.contains(Every.of(1, 2, 3)));
    assertTrue(permutations.contains(Every.of(2, 3, 1)));
    assertTrue(permutations.contains(Every.of(3, 1, 2)));
    assertTrue(permutations.contains(Every.of(1, 3, 2)));
    assertTrue(permutations.contains(Every.of(2, 1, 3)));
    assertTrue(permutations.contains(Every.of(3, 2, 1)));
  }

  @Test
  public void prefixSegmentLength() {
    Every<Integer> e = Every.of(1, 2, 3);
    assertEquals(2, e.prefixLength(i -> i < 3));
    assertEquals(2, e.segmentLength(i -> i > 1, 1));
  }

  @Test
  public void scans() {
    Every<String> e = Every.of("1", "2", "3");
    Every<String> left = Every.of("z", "z1", "z12", "z123");
    assertEquals(left, e.scan("z", (s1, s2) -> s1 + s2));
    assertEquals(left, e.scanLeft("z", (acc, s2) -> acc + s2));
    Every<String> right = Every.of("z321", "z32", "z3", "z");
    assertEquals(right, e.scanRight("z", (s, acc) -> acc + s));
  }

  @Test
  public void sliding() {
    Every<Integer> e = Every.of(1, 2, 3, 4);
    Iterator<Every<Integer>> slide = e.sliding(2);
    assertEquals(Every.of(1, 2), slide.next());
    assertEquals(Every.of(2, 3), slide.next());
    assertEquals(Every.of(3, 4), slide.next());
    slide = e.sliding(2, 2);
    assertEquals(Every.of(1, 2), slide.next());
    assertEquals(Every.of(3, 4), slide.next());
  }

  @Test
  public void patch() {
    Every<String> e = Every.of("a", "b", "c");
    Every<String> patch = Every.of("d");
    assertEquals(Every.of("a", "b", "c", "d"), e.patch(3, patch, 0));
    assertEquals(Every.of("a", "b", "d", "c"), e.patch(2, patch, 0));
    assertEquals(Every.of("a", "b", "c", "d"), e.patch(5, patch, 5));
    assertEquals(Every.of("a", "b", "c", "d"), e.patch(5, patch, -5));
    assertEquals(Every.of("d", "a", "b", "c"), e.patch(0, patch, -5));
    assertEquals(Every.of("d"), e.patch(0, patch, 5));
    assertEquals(Every.of("d", "b", "c"), e.patch(0, patch, 1));
    assertEquals(Every.of("a", "b", "d"), e.patch(2, patch, 1));
  }

  @Test
  public void sortWith() {
    Every<Integer> e = Every.of(3, 6, 1, 7, 7);
    Every<Integer> increasing = e.sortWith((l, r) -> l < r);
    assertEquals(Every.of(1, 3, 6, 7, 7), increasing);
    Every<Integer> decreasing = e.sortWith((l, r) -> l > r);
    assertEquals(Every.of(7, 7, 6, 3, 1), decreasing);
    assertEquals(Every.of(7, 7, 6, 3, 1), e.sortBy(i -> -i));

    assertEquals(Every.of(1, 3, 6, 7, 7), e.sorted());
    assertEquals(Every.of(1, 3, 6, 7, 7), e.sorted(Comparator.naturalOrder()));
  }

  @Test
  public void startsWith() {
    Every<Integer> e = Every.of(1, 2, 3, 1, 2, 3);
    assertTrue(e.startsWith(Stream.of(1, 2)));
    assertTrue(e.startsWith(Stream.of(3, 1), 2));
  }

  @Test
  public void union() {
    Every<Integer> e1 = Every.of(1, 2, 3);
    Every<Integer> e2 = Every.of(3, 2, 1);
    assertEquals(Every.of(1, 2, 3, 3, 2, 1), e1.union(e2));
  }

  @Test
  public void unzips() {
    Every<Integer> e = Every.of(1, 2, 3);
    assertEquals(Tuple.of(Every.of(1, 2, 3), Every.of(-1, -2, -3)), e.unzip(i -> Tuple.of(i, -i)));
    assertEquals(Tuple.of(Every.of(1, 2, 3), Every.of("1", "2", "3"), Every.of(-1, -2, -3)), e.unzip3(i -> Tuple.of(i, "" + i, -i)));
  }

  @Test
  public void updated() {
    Every<Integer> e = Every.of(1, 2, 3);
    assertEquals(Every.of(1, -2, 3), e.updated(1, -2));
  }

  @Test
  public void zips() {
    Every<Integer> e = Every.of(1, 2);
    assertEquals(Every.of(Tuple.of(1, 5), Tuple.of(2, 9)), e.zipAll(List.of(5), 0, 9));
    assertEquals(Every.of(Tuple.of(1, 5), Tuple.of(2, 6), Tuple.of(0, 7)), e.zipAll(List.of(5, 6, 7), 0, 19));
    Every<String> e2 = Every.of("A", "B");
    assertEquals(Every.of(Tuple.of("A", 0L), Tuple.of("B", 1L)), e2.zipWithIndex());
  }

  @Test
  public void productSum() {
    Every<Integer> e = Every.of(4, 4);
    assertEquals(16L, e.product());
    assertEquals(8L, e.sum());
  }

  @Test
  public void prepend() {
    Every<Integer> e = Every.of(2, 3);
    assertEquals(Every.of(1, 2, 3), e.prepend(1));
  }

  @Test
  public void mkString() {
    Every<Integer> e = Every.of(4, 5, 6);
    assertEquals("{4:5:6}", e.mkString("{", ":", "}"));
    assertEquals("4:5:6", e.mkString(":"));
    assertEquals("456", e.mkString());
  }

  @Test
  public void exists() {
    Every<Integer> e = Every.of(4, 5, 6);
    assertTrue(e.exists(i -> i == 5));
  }

  @Test
  public void find() {
    Every<Integer> e = Every.of(4, 8, 12);
    assertEquals(8, e.find(i -> i > 4).get().intValue());
  }

  @Test
  public void equals() {
    One<String> one1 = One.of("one1");
    One<String> one2 = One.of("one2");
    Object o = new Object();
    Assert.assertFalse(one1.equals(o));
    Assert.assertFalse(one1.equals(null));
    Assert.assertFalse(one1.equals(one2));
    Assert.assertTrue(one1.equals(one1));
  }

}
