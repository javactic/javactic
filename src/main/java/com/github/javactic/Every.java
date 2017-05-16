package com.github.javactic;
/*
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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.collection.Array;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.control.Option;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static com.github.javactic.Helper.fromNonEmptySeq;

/**
 * An ordered, immutable, non-empty collection of elements. Class Every has two
 * and only two subtypes: {@link One} and {@link Many}. A One contains exactly
 * one element. A Many contains two or more elements. Thus no way exists for an
 * Every to contain zero elements.
 *
 * <h1>Motivation for Everys</h1>
 *
 * Although Every is a general-purpose, non-empty ordered collection, it was
 * motivated by the desire to enable easy accumulation of errors in {@link Or}s.
 * Please refer to the original documentation for the
 * <a href="http://www.scalactic.org/">Scalactic</a> library on
 * <a href="http://www.scalactic.org/user_guide/OrAndEvery">Ors and Everys</a>.
 *
 * @author mvh
 *
 * @param <T> the type of the Every
 */
public interface Every<T> extends Iterable<T>, IntFunction<T> {

  /**
   * Converts this value to a {@link Vector}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toVector: Vector[T]
   * </pre>
   *
   * @return a {@link Vector}.
   */
  Vector<T> toVector();

  /**
   * Tests whether this Every contains given index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def isDefinedAt(idx: Int): Boolean
   * </pre>
   *
   * @param index
   *            the index to test
   * @return true if this Every contains an element at position idx, false
   *         otherwise.
   */
  default boolean isDefinedAt(Integer index) {
    return index >= 0 && index < length();
  }

  /**
   * Returns false to indicate this Every, like all Everys, is non-empty.
   *
   * <pre class="stHighlighted">
   * Scalactic: def isEmpty(): Boolean
   * </pre>
   *
   * @return false
   */
  default boolean isEmpty() {
    return false;
  }

  /**
   * Creates and returns a new iterator over all elements contained in this
   * Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toIterator: Iterator[T]
   * </pre>
   *
   * @return the new iterator
   */
  @Override
  default Iterator<T> iterator() {
    return toVector().iterator();
  }

  @SafeVarargs
  static <T> Every<T> of(T first, T... rest) {
    return of(first, Vector.of(rest));
  }

  static <T> Every<T> of(T first, Seq<? extends T> rest) {
    if (rest.length() == 0)
      return One.of(first);
    else
      return new Many<>(first, rest);
  }

  /**
   * Returns a new Every containing the elements of this Every followed by the
   * elements of the passed Iterable.
   *
   * <pre class="stHighlighted">
   * Scalactic: def ++[U &gt;: T](other: GenTraversableOnce[U]): Every[U]
   * </pre>
   *
   * @param iterable
   *            the Iterable to append
   * @return a new Every that contains all the elements of this Every followed
   *         by all elements of other.
   */
  default Every<T> appendAll(Iterable<? extends T> iterable) {
    if (!iterable.iterator().hasNext())
      return this;
    else {
      return fromNonEmptySeq(toVector().appendAll(iterable));
    }
  }

  /**
   * Returns a new Many containing the elements of this Every followed by the
   * elements of the passed Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def ++[U &gt;: T](other: Every[U]): Many[U]
   * </pre>
   *
   * @param other
   *            the Every to append
   * @return a new Many that contains all the elements of this Every followed
   *         by all elements of other.
   */
  default Many<T> appendAll(Every<? extends T> other) {
    Vector<T> all = toVector().appendAll(other.toVector());
    return new Many<>(all.head(), all.tail());
  }

  /**
   * Returns a new Every with the given element appended.
   *
   * <pre class="stHighlighted">
   * Scalactic: def :+[U &gt;: T](element: U): Many[U]
   * </pre>
   *
   * @param element
   *            the element to append to this Every
   * @return a new Every consisting of all elements of this Every followed by
   *         element.
   */
  default Many<T> append(T element) {
    Vector<T> all = toVector().append(element);
    return new Many<>(all.head(), all.tail());
  }

  /**
   * Returns a new Every with the given element prepended.
   *
   * <pre class="stHighlighted">
   * Scalactic: def +:[U &gt;: T](element: U): Many[U]
   * </pre>
   *
   * @param element
   *            the element to prepend to this Every
   * @return a new Every consisting of element followed by all elements of
   *         this Every.
   */
  default Many<T> prepend(T element) {
    Vector<T> all = toVector().prepend(element);
    return new Many<>(all.head(), all.tail());
  }

  /**
   * Appends all elements of this Every to a string builder using start, end,
   * and separator strings. The written text will consist of a concatenation
   * of the string start; the result of invoking toString on all elements of
   * this Every, separated by the string sep; and the string end.
   *
   * <pre class="stHighlighted">
   * Scalactic: def addString(sb: StringBuilder, start: String, sep: String, end: String): StringBuilder
   * </pre>
   *
   * @param sb
   *            the string builder to which elements will be appended
   * @param start
   *            the ending string
   * @param sep
   *            the separator string
   * @param end
   *            the ending string
   * @return the string builder, sb, to which elements were appended.
   */
  default StringBuilder addString(StringBuilder sb, String start, String sep, String end) {
    sb.append(start);
    Iterator<T> it = iterator();
    while (it.hasNext()) {
      sb.append(it.next());
      if (it.hasNext())
        sb.append(sep);
    }
    sb.append(end);
    return sb;
  }

  /**
   * Appends all elements of this Every to a string builder using a separator
   * string. The written text will consist of a concatenation of the result of
   * invoking toString on of every element of this Every, separated by the
   * string sep.
   *
   * <pre class="stHighlighted">
   * Scalactic: def addString(sb: StringBuilder, sep: String): StringBuilder
   * </pre>
   *
   * @param sb
   *            the string builder to which elements will be appended
   * @param sep
   *            the separator string
   * @return the string builder, sb, to which elements were appended.
   */
  default StringBuilder addString(StringBuilder sb, String sep) {
    return addString(sb, "", sep, "");
  }

  /**
   * Appends all elements of this Every to a string builder. The written text
   * will consist of a concatenation of the result of invoking toString on of
   * every element of this Every, without any separator string.
   *
   * <pre class="stHighlighted">
   * Scalactic: def addString(sb: StringBuilder): StringBuilder
   * </pre>
   *
   * @param sb
   *            the string builder to which elements will be appended
   * @return the string builder, sb, to which elements were appended.
   */
  default StringBuilder addString(StringBuilder sb) {
    return addString(sb, "");
  }

  /**
   * Indicates whether this Every contains a given value as an element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def contains(elem: Any): Boolean
   * </pre>
   *
   * @param elem
   *            the element to look for
   * @return true if this Every has an element that is equal (as determined by
   *         ==) to elem, false otherwise.
   */
  default boolean contains(T elem) {
    return toVector().contains(elem);
  }

  /**
   * Indicates whether this Every contains a given Iterable as a slice.
   *
   * <pre class="stHighlighted">
   * Scalactic: def containsSlice[B](that: GenSeq[B]): Boolean <br>
   * Scalactic: def containsSlice[B](that: Every[B]): Boolean
   * </pre>
   *
   * @param that
   *            the Iterable slice to look for
   * @return true if this Every contains a slice with the same elements as
   *         that, otherwise false.
   */
  default boolean containsSlice(Iterable<? extends T> that) {
    return toVector().containsSlice(that);
  }

  /**
   * Copies values of this Every to an array. Fills the given array arr with
   * at most len elements of this Every, beginning at index start. Copying
   * will stop once either the end of the current Every is reached, the end of
   * the array is reached, or len elements have been copied.
   *
   * <pre class="stHighlighted">
   * Scalactic: def copyToArray[U &gt;: T](arr: Array[U], start: Int, len: Int): Unit
   * </pre>
   *
   * @param target
   *            the array to fill
   * @param start
   *            the starting index
   * @param length
   *            the maximum number of elements to copy
   */
  default void copyToJavaArray(T[] target, int start, int length) {
    int i = start;
    int end = Math.min((start + length), target.length);
    Iterator<T> it = iterator();
    while (i < end && it.hasNext()) {
      target[i] = it.next();
      i += 1;
    }
  }

  /**
   * Copies values of this Every to an array. Fills the given array arr with
   * values of this Every, beginning at index start. Copying will stop once
   * either the end of the current Every is reached, or the end of the array
   * is reached.
   *
   * <pre class="stHighlighted">
   * Scalactic: def copyToArray[U &gt;: T](arr: Array[U], start: Int): Unit
   * </pre>
   *
   * @param target
   *            the array to fill
   * @param start
   *            the starting index
   */
  default void copyToJavaArray(T[] target, int start) {
    copyToJavaArray(target, start, length());
  }

  /**
   * Copies values of this Every to an array. Fills the given array arr with
   * values of this Every. Copying will stop once either the end of the
   * current Every is reached, or the end of the array is reached.
   *
   * <pre class="stHighlighted">
   * Scalactic: def copyToArray[U &gt;: T](arr: Array[U]): Unit
   * </pre>
   *
   * @param target
   *            the array to fill
   */
  default void copyToJavaArray(T[] target) {
    copyToJavaArray(target, 0);
  }

  /**
   * Indicates whether every element of this Every relates to the
   * corresponding element of a given Iterable by satisfying a given
   * predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def corresponds[B](that: GenSeq[B])(p: (T, B) =&gt; Boolean): Boolean <br>
   * Scalactic: def corresponds[B](that: Every[B])(p: (T, B) =&gt; Boolean): Boolean
   * </pre>
   *
   * @param <B>
   *            the type of the elements of that
   * @param that
   *            the Iterable to compare for correspondence
   * @param predicate
   *            the predicate, which relates elements from this and the passed
   *            Iterable
   * @return true if this and the passed Iterable have the same length and
   *         predicate(x, y) is true for all corresponding elements x of this
   *         Every and y of that, otherwise false.
   */
  default <B> boolean corresponds(Iterable<? extends B> that, BiPredicate<? super T, ? super B> predicate) {
    return toVector().corresponds(that, predicate);
  }

  /**
   * Counts the number of elements in the Every that satisfy a predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def count(p: (T) =&gt; Boolean): Int
   * </pre>
   *
   * @param predicate
   *            the predicate used to test elements.
   * @return the number of elements satisfying the predicate p.
   */
  default long count(Predicate<? super T> predicate) {
    return toVector().filter(predicate).length();
  }

  /**
   * Indicates whether a predicate holds for at least one of the elements of
   * this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def exists(p: (T) =&gt; Boolean): Boolean
   * </pre>
   *
   * @param predicate
   *            the predicate to use for matching.
   * @return true if the given predicate p holds for some of the elements of
   *         this Every, otherwise false.
   */
  default boolean exists(Predicate<? super T> predicate) {
    return toVector().exists(predicate);
  }

  /**
   * Finds the first element of this Every that satisfies the given predicate,
   * if any.
   *
   * <pre class="stHighlighted">
   * Scalactic: def find(p: (T) =&gt; Boolean): Option[T]
   * </pre>
   *
   * @param predicate
   *            the predicate used to test elements
   * @return a Some containing the first element in this Every that satisfies
   *         predicate, or None if none exists.
   */
  default Option<T> find(Predicate<? super T> predicate) {
    return toVector().find(predicate);
  }

  /**
   * Same as {@link #apply(int)}.
   *
   * @param index
   *            the index to select from.
   * @return the element of this Every at index index, where 0 indicates the
   *         first element.
   */
  default T get(int index) {
    return apply(index);
  }

  /**
   * Same as {@link #applyOrElse(int, IntFunction)}.
   *
   * @param index
   *            the index to select from.
   * @param fallback
   *            the function to execute if index is out of bounds.
   * @return returns the element of this Every at index or result of the
   *         fallback function.
   */
  default T getOrElse(int index, IntFunction<? extends T> fallback) {
    return applyOrElse(index, fallback);
  }

  default T getOrElse(int index, T def) {
    return applyOrElse(index, def);
  }

  /**
   * Selects an element by index in the Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def apply(idx: Int): T
   * </pre>
   *
   * @param index
   *            the index to select from.
   * @return the element of this Every at index index, where 0 indicates the
   *         first element.
   */
  @Override
  default T apply(int index) {
    return toVector().get(index);
  }

  /**
   * Selects the element at index in this Every or applies fallback function
   * if index is out of bounds.
   *
   * <pre class="stHighlighted">
   * Scalactic: def applyOrElse[A1 &lt;: Int, B1 &gt;: T](x: A1, default: (A1) =&gt; B1): B1
   * </pre>
   *
   * @param index
   *            the index to select from.
   * @param fallback
   *            the function to execute if index is out of bounds.
   * @return returns the element of this Every at index or result of the
   *         fallback function.
   */
  default T applyOrElse(int index, IntFunction<? extends T> fallback) {
    return isDefinedAt(index) ? apply(index) : fallback.apply(index);
  }

  default T applyOrElse(int index, T def) {
    return isDefinedAt(index) ? apply(index) : def;
  }

  /**
   * Composes an instance of ToIntFunction with this IntFunction.
   *
   * <pre class="stHighlighted">
   * Scalactic: def compose[A](g: (A) =&gt; Int): (A) =&gt; T
   * </pre>
   *
   * @param <A>
   *            the type to which function g can be applied
   * @param g
   *            a function from A to Int
   * @return a new function f such that f(x) == apply(g(x))
   */
  default <A> Function<A, T> compose(ToIntFunction<? super A> g) {
    return (A a) -> apply(g.applyAsInt(a));
  }

  /**
   * Builds a new Every from this Every without any duplicate elements.
   *
   * <pre class="stHighlighted">
   * Scalactic: def distinct: Every[T]
   * </pre>
   *
   * @return A new Every that contains the first occurrence of every element
   *         of this Every.
   */
  default Every<T> distinct() {
    return fromNonEmptySeq(toVector().distinct());
  }

  /**
   * Indicates whether a predicate holds for all elements of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def forall(p: (T) =&gt; Boolean): Boolean
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @return true if the given predicate p holds for all elements of this
   *         Every, otherwise false.
   */
  default boolean forAll(Predicate<? super T> p) {
    return toVector().forAll(p);
  }

  /**
   * Applies a function f to all elements of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def foreach(f: (T) =&gt; Unit): Unit
   * </pre>
   *
   * @param action
   *            the function that is applied for its side-effect to every
   *            element. The result of function f is discarded.
   */
  default void forEach(Consumer<? super T> action) {
    toVector().forEach(action);
  }

  /**
   * Converts this Every of Everys into an Every formed by the elements of the
   * nested Everys. This is a static method because there's no way in Java to
   * require that T is an Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def flatten[B](implicit ev: &lt;:&lt;[T, Every[B]]): Every[B]
   * </pre>
   *
   * @param <B>
   *            the type of the new Every
   * @param every the Every to flatten
   * @return a new Every resulting from concatenating all nested Everys.
   */
  static <B> Every<B> flatten(Every<? extends Every<? extends B>> every) {
    Vector<B> acc = Vector.empty();
    for (Every<? extends B> nested : every) {
      acc = acc.appendAll(nested.toVector());
    }
    return fromNonEmptySeq(acc);
  }

  /**
   * Folds the elements of this Every using the specified associative binary
   * operator.
   *
   * <pre class="stHighlighted">
   * Scalactic: def fold[U &gt;: T](z: U)(op: (U, U) =&gt; U): U
   * </pre>
   *
   * @param z
   *            a neutral element for the fold operation; may be added to the
   *            result an arbitrary number of times, and must not change the
   *            result (e.g., 0 for addition, or 1 for multiplication.)
   * @param op
   *            a binary operator that must be associative
   * @return the result of applying fold operator op between all the elements
   *         and z
   */
  default T fold(T z, BiFunction<? super T, ? super T, ? extends T> op) {
    return toVector().fold(z, op);
  }

  /**
   * Applies a binary operator to a start value and all elements of this
   * Every, going left to right.
   *
   * <pre class="stHighlighted">
   * Scalactic: def foldLeft[B](z: B)(op: (B, T) =&gt; B): B
   * </pre>
   *
   * @param <B>
   *            the type of the new Every
   * @param z
   *            the start value.
   * @param op
   *            the binary operator.
   * @return the result of inserting op between consecutive elements of this
   *         Every, going left to right, with the start value, z, on the left.
   */
  default <B> B foldLeft(B z, BiFunction<? super B, ? super T, ? extends B> op) {
    return toVector().foldLeft(z, op);
  }

  /**
   * Applies a binary operator to all elements of this Every and a start
   * value, going right to left.
   *
   * <pre class="stHighlighted">
   * Scalactic: def foldRight[B](z: B)(op: (T, B) =&gt; B): B
   * </pre>
   *
   * @param <B>
   *            the type of the new Every
   * @param z
   *            the start value
   * @param op
   *            the binary operator
   * @return the result of inserting op between consecutive elements of this
   *         Every, going right to left, with the start value, z, on the
   *         right.
   */
  default <B> B foldRight(B z, BiFunction<? super T, ? super B, ? extends B> op) {
    return toVector().foldRight(z, op);
  }

  /**
   * Partitions this Every into a map of Everys according to some
   * discriminator function.
   *
   * <pre class="stHighlighted">
   * Scalactic: def groupBy[K](f: (T) =&gt; K): Map[K, Every[T]]
   * </pre>
   *
   * @param <K>
   *            the type of keys returned by the discriminator function.
   * @param f
   *            the discriminator function.
   * @return A map from keys to Everys.
   */
  default <K> Map<K, Every<T>> groupBy(Function<? super T, ? extends K> f) {
    return toVector().groupBy(f).map((k, v) -> Tuple.of(k, fromNonEmptySeq(v)));
  }

  /**
   * Partitions elements into fixed size Everys.
   *
   * <pre class="stHighlighted">
   * Scalactic: def grouped(size: Int): Iterator[Every[T]]
   * </pre>
   *
   * @param size
   *            the number of elements per group
   * @return An iterator producing Everys of size size, except the last will
   *         be truncated if the elements don't divide evenly.
   */
  default Iterator<Every<T>> grouped(int size) {
    return toVector().grouped(size).map(Helper::fromNonEmptySeq);
  }

  /**
   * Selects the first element of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def head: T
   * </pre>
   *
   * @return the first element of this Every.
   */
  default T head() {
    return toVector().head();
  }

  /**
   * Selects the first element of this Every and returns it wrapped in a Some.
   *
   * <pre class="stHighlighted">
   * Scalactic: def headOption: Option[T]
   * </pre>
   *
   * @return the first element of this Every, wrapped in a Some.
   */
  default Option<T> headOption() {
    return toVector().headOption();
  }

  /**
   * Finds index of first occurrence of some value in this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexOf[U &gt;: T](elem: U): Int
   * </pre>
   *
   * @param <U>
   *            the type of the searched element.
   * @param elem
   *            the element value to search for.
   * @return the index of the first element of this Every that is equal (as
   *         determined by ==) to elem, or -1, if none exists.
   */
  default <U extends T> int indexOf(U elem) {
    return toVector().indexOf(elem);
  }

  /**
   * Finds index of first occurrence of some value in this Every after or at
   * some start index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexOf[U &gt;: T](elem: U, from: Int): Int
   * </pre>
   *
   * @param <U>
   *            type of the searched element.
   * @param elem
   *            the element value to search for.
   * @param from
   *            the start index
   * @return the index &gt;= from of the first element of this Every that is
   *         equal (as determined by ==) to elem, or -1, if none exists.
   */
  default <U extends T> int indexOf(U elem, int from) {
    return toVector().indexOf(elem, from);
  }

  /**
   * Finds first index where this Every contains a given Iterable as a slice.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexOfSlice[U &gt;: T](that: GenSeq[U]): Int <br>
   * Scalactic: def indexOfSlice[U &gt;: T](that: Every[U]): Int
   * </pre>
   *
   * @param that
   *            the Iterable defining the slice to look for
   * @return the first index at which the elements of this Every starting at
   *         that index match the elements of Iterable that, or -1 of no such
   *         subsequence exists.
   */
  default int indexOfSlice(Iterable<? extends T> that) {
    return toVector().indexOfSlice(that);
  }

  /**
   * Finds first index after or at a start index where this Every contains a
   * given Iterable as a slice.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexOfSlice[U &gt;: T](that: GenSeq[U], end: Int): Int <br>
   * Scalactic: def indexOfSlice[U &gt;: T](that: Every[U], end: Int): Int
   * </pre>
   *
   * @param that
   *            the Iterable defining the slice to look for
   * @param from
   *            the start index
   * @return the first index &gt;= from at which the elements of this Every
   *         starting at that index match the elements of Iterable that, or -1
   *         of no such subsequence exists.
   */
  default int indexOfSlice(Iterable<? extends T> that, int from) {
    return toVector().indexOfSlice(that, from);
  }

  /**
   * Finds index of the first element satisfying some predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexWhere(p: (T) =&gt; Boolean): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @return the index of the first element of this Every that satisfies the
   *         predicate p, or -1, if none exists.
   */
  default int indexWhere(Predicate<? super T> p) {
    return indexWhere(p, 0);
  }

  /**
   * Finds index of the first element satisfying some predicate after or at
   * some start index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def indexWhere(p: (T) =&gt; Boolean, from: Int): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @param from
   *            the start index
   * @return the index &gt;= from of the first element of this Every that
   *         satisfies the predicate p, or -1, if none exists.
   */
  default int indexWhere(Predicate<? super T> p, int from) {
    return toVector().indexWhere(p, from);
  }

  /**
   * Selects the last element of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def last: T
   * </pre>
   *
   * @return the last element of this Every.
   */
  default T last() {
    return toVector().last();
  }

  /**
   * Returns the last element of this Every, wrapped in a Some.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastOption: Option[T]
   * </pre>
   *
   * @return the last element, wrapped in a Some.
   */
  default Option<T> lastOption() {
    return Option.of(last());
  }

  /**
   * Finds the index of the last occurrence of some value in this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexOf[U &gt;: T](elem: U): Int
   * </pre>
   *
   * @param elem
   *            the element value to search for.
   * @return the index of the last element of this Every that is equal (as
   *         determined by ==) to elem, or -1, if none exists.
   */
  default int lastIndexOf(T elem) {
    return toVector().lastIndexOf(elem);
  }

  /**
   * Finds the index of the last occurrence of some value in this Every before
   * or at a given end index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexOf[U &gt;: T](elem: U, end: Int): Int
   * </pre>
   *
   * @param elem
   *            the element value to search for.
   * @param end
   *            the end index.
   * @return the index &gt;= end of the last element of this Every that is
   *         equal (as determined by ==) to elem, or -1, if none exists.
   */
  default int lastIndexOf(T elem, int end) {
    return toVector().lastIndexOf(elem, end);
  }

  /**
   * Finds the last index where this Every contains a given Iterable as a
   * slice.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexOfSlice[U &gt;: T](that: GenSeq[U]): Int <br>
   * Scalactic: def lastIndexOfSlice[U &gt;: T](that: Every[U]): Int
   * </pre>
   *
   * @param that
   *            the Iterable defining the slice to look for
   * @return the last index at which the elements of this Every starting at
   *         that index match the elements of Iterable that, or -1 of no such
   *         subsequence exists.
   */
  default int lastIndexOfSlice(Iterable<? extends T> that) {
    return toVector().lastIndexOfSlice(that);
  }

  /**
   * Finds the last index before or at a given end index where this Every
   * contains a given Iterable as a slice.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexOfSlice[U &gt;: T](that: GenSeq[U], end: Int): Int <br>
   * Scalactic: def lastIndexOfSlice[U &gt;: T](that: Every[U], end: Int): Int
   * </pre>
   *
   * @param that
   *            the Iterable defining the slice to look for
   * @param end
   *            the end index
   * @return the last index &gt;= end at which the elements of this Every
   *         starting at that index match the elements of Iterable that, or -1
   *         of no such subsequence exists.
   */
  default int lastIndexOfSlice(Iterable<? extends T> that, int end) {
    return toVector().lastIndexOfSlice(that, end);
  }

  /**
   * Finds index of last element satisfying some predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexWhere(p: (T) =&gt; Boolean): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @return the index of the last element of this Every that satisfies the
   *         predicate p, or -1, if none exists.
   */
  default int lastIndexWhere(Predicate<? super T> p) {
    return toVector().lastIndexWhere(p);
  }

  /**
   * Finds index of last element satisfying some predicate before or at given
   * end index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lastIndexWhere(p: (T) =&gt; Boolean, end: Int): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @param end
   *            the end index
   * @return the index &gt;= end of the last element of this Every that
   *         satisfies the predicate p, or -1, if none exists.
   */
  default int lastIndexWhere(Predicate<? super T> p, int end) {
    return toVector().lastIndexWhere(p, end);
  }

  /**
   * The size of this Every. Note: length and size yield the same result,
   * which will be &gt;= 1.
   *
   * <pre class="stHighlighted">
   * Scalactic: def size: Int
   * </pre>
   *
   * @return the number of elements in this Every.
   */
  default int size() {
    return length();
  }

  /**
   * The length of this Every. Note: length and size yield the same result,
   * which will be &gt;= 1.
   *
   * <pre class="stHighlighted">
   * Scalactic: def length: Int
   * </pre>
   *
   * @return the number of elements in this Every.
   */
  default int length() {
    return toVector().length();
  }

  /**
   * Compares the length of this Every to a test value.
   *
   * <pre class="stHighlighted">
   * Scalactic: def lengthCompare(len: Int): Int
   * </pre>
   *
   * @param len
   *            the test value that gets compared with the length.
   * @return a value x where: <br>
   *         x &lt; 0 if this.length &lt; len <br>
   *         x == 0 if this.length == len <br>
   *         x &gt; 0 if this.length &gt; len
   */
  default int lengthCompare(int len) {
    return length() - len;
  }

  /**
   * Builds a new Every by applying a function to all elements of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def map[U](f: (T) =&gt; U): Every[U]
   * </pre>
   *
   * @param <U>
   *            the element type of the returned Every.
   * @param f
   *            the function to apply to each element.
   * @return a new Every resulting from applying the given function f to each
   *         element of this Every and collecting the results.
   */
  default <U> Every<U> map(Function<? super T, U> f) {
    return fromNonEmptySeq(toVector().map(f));
  }

  /**
   * Finds the largest element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def max[U &gt;: T](implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @return {@code Some(maximum)} of this elements or {@code None} if this
   *         elements are not comparable.
   */
  default Option<T> max() {
    return toVector().max();
  }

  /**
   * Finds the largest result according to the supplied {@link Comparator}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def maxBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @param c
   *            the comparator to use.
   * @return the largest result according to the supplied {@link Comparator}.
   */
  default T maxBy(Comparator<? super T> c) {
    return toVector().maxBy(c).get();
  }

  /**
   * Finds the largest result after applying the given function to every
   * element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def maxBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @param <U>
   *            the comparable type returned by the function f
   * @param f
   *            the mapping function
   * @return the largest result of applying the given function to every
   *         element of this Every.
   */
  default <U extends Comparable<? super U>> T maxBy(Function<? super T, ? extends U> f) {
    return toVector().maxBy(f).get();
  }

  /**
   * Finds the smallest element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def min[U &gt;: T](implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @return {@code Some(minimum)} of this elements or {@code None} if this
   *         elements are not comparable.
   */
  default Option<T> min() {
    return toVector().min();
  }

  /**
   * Finds the smallest result according to the supplied {@link Comparator}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def minBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @param c
   *            the comparator to use.
   * @return the smallest result according to the supplied {@link Comparator}.
   */
  default T minBy(Comparator<? super T> c) {
    return toVector().minBy(c).get();
  }

  /**
   * Finds the smallest result after applying the given function to every
   * element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def minBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T
   * </pre>
   *
   * @param <U>
   *            comparable type returned by function f.
   * @param f
   *            the mapping function
   * @return the smallest result of applying the given function to every
   *         element of this Every.
   */
  default <U extends Comparable<? super U>> T minBy(Function<? super T, ? extends U> f) {
    return toVector().minBy(f).get();
  }

  /**
   * A copy of this Every with an element value appended until a given target
   * length is reached.
   *
   * <pre class="stHighlighted">
   * Scalactic: def padTo[U &gt;: T](len: Int, elem: U): Every[U]
   * </pre>
   *
   * @param len
   *            the target length
   * @param elem
   *            the padding value
   * @return a new Every consisting of all elements of this Every followed by
   *         the minimal number of occurrences of elem so that the resulting
   *         Every has a length of at least len.
   */
  default Every<T> padTo(int len, T elem) {
    return fromNonEmptySeq(toVector().padTo(len, elem));
  }

  /**
   * Produces a new Every where a slice of elements in this Every is replaced
   * by another Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def patch[U &gt;: T](from: Int, that: Every[U], replaced: Int): Every[U]
   * </pre>
   *
   * @param from
   *            the index of the first replaced element
   * @param that
   *            the Every whose elements should replace a slice in this Every
   * @param replaced
   *            the number of elements to drop in the original Every
   * @return a new Every where a slice of elements in this Every is replaced
   *         by another Every
   */
  default Every<T> patch(int from, Every<? extends T> that, int replaced) {
    return fromNonEmptySeq(toVector().patch(from, that.toVector(), replaced));
  }

  /**
   * Reduces the elements of this Every using the specified associative binary
   * operator. The order in which operations are performed on elements is
   * unspecified and may be nondeterministic.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduce[U &gt;: T](op: (U, U) =&gt; U): U
   * </pre>
   *
   * @param op
   *            a binary operator that must be associative.
   * @return the result of applying reduce operator op between all the
   *         elements of this Every.
   */
  default T reduce(BiFunction<? super T, ? super T, ? extends T> op) {
    return toVector().reduce(op);
  }

  /**
   * Reduces the elements of this Every using the specified associative binary
   * operator returning the result in a Some. The order in which operations
   * are performed on elements is unspecified and may be nondeterministic.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduceOption[U &gt;: T](op: (U, U) =&gt; U): Option[U]
   * </pre>
   *
   * @param op
   *            a binary operator that must be associative.
   * @return the result of applying reduce operator op between all the
   *         elements of this Every.
   */
  default Option<T> reduceOption(BiFunction<? super T, ? super T, ? extends T> op) {
    return Option.of(toVector().reduce(op));
  }

  /**
   * Applies a binary operator to all elements of this Every, going left to
   * right.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduceLeft[U &gt;: T](op: (U, T) =&gt; U): U
   * </pre>
   *
   * @param op
   *            the binary operator.
   * @return the result of inserting op between consecutive elements of this
   *         Every, going left to right.
   */
  default T reduceLeft(BiFunction<? super T, ? super T, ? extends T> op) {
    return toVector().reduceLeft(op);
  }

  /**
   * Applies a binary operator to all elements of this Every, going left to
   * right, returning the result in a Some.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduceLeftOption[U &gt;: T](op: (U, T) =&gt; U): Option[U]
   * </pre>
   *
   * @param op
   *            the binary operator.
   * @return a Some containing the result of reduceLeft(op)
   */
  default Option<T> reduceLeftOption(BiFunction<? super T, ? super T, ? extends T> op) {
    return Option.of(toVector().reduceLeft(op));
  }

  /**
   * Applies a binary operator to all elements of this Every, going right to
   * left.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduceRight[U &gt;: T](op: (T, U) =&gt; U): U
   * </pre>
   *
   * @param op
   *            the binary operator
   * @return the result of inserting op between consecutive elements of this
   *         Every, going right to left.
   */
  default T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
    return toVector().reduceRight(op);
  }

  /**
   * Applies a binary operator to all elements of this Every, going right to
   * left, returning the result in a Some.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reduceRightOption[U &gt;: T](op: (T, U) =&gt; U): Option[U]
   * </pre>
   *
   * @param op
   *            the binary operator
   * @return a Some containing the result of reduceRight(op)
   */
  default Option<T> reduceRightOption(BiFunction<? super T, ? super T, ? extends T> op) {
    return Option.of(toVector().reduceRight(op));
  }

  /**
   * Returns new Every wih elements in reverse order.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reverse: Every[T]
   * </pre>
   *
   * @return a new Every with all elements of this Every in reversed order.
   */
  default Every<T> reverse() {
    return fromNonEmptySeq(toVector().reverse());
  }

  /**
   * An iterator yielding elements in reverse order.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reverseIterator: Iterator[T]
   * </pre>
   *
   * @return an iterator yielding the elements of this Every in reversed order
   */
  default Iterator<T> reverseIterator() {
    return toVector().reverseIterator();
  }

  /**
   * Builds a new Every by applying a function to all elements of this Every
   * and collecting the results in reverse order.
   *
   * <pre class="stHighlighted">
   * Scalactic: def reverseMap[U](f: (T) =&gt; U): Every[U]
   * </pre>
   *
   * @param <U>
   *            the element type of the returned Every.
   * @param f
   *            the function to apply to each element.
   * @return a new Every resulting from applying the given function f to each
   *         element of this Every and collecting the results in reverse
   *         order.
   */
  default <U> Every<U> reverseMap(Function<? super T, ? extends U> f) {
    return fromNonEmptySeq(reverseIterator().map(f).toVector());
  }

  /**
   * Checks if the given Iterable contains the same elements in the same order
   * as this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sameElements[U &gt;: T](that: GenIterable[U]): Boolean <br>
   * Scalactic: def sameElements[U &gt;: T](that: Every[U]): Boolean
   * </pre>
   *
   * @param that
   *            the Iterable with which to compare
   * @return true, if both this and the given Iterable contain the same
   *         elements in the same order, false otherwise.
   */
  default boolean sameElements(Iterable<? super T> that) {
    return toVector().eq(that);
  }

  /**
   * Indicates whether this Every ends with the given Iterable.
   *
   * <pre class="stHighlighted">
   * Scalactic: def endsWith[B](that: GenSeq[B]): Boolean <br>
   * Scalactic: def endsWith[B](that: Every[B]): Boolean
   * </pre>
   *
   * @param that
   *            the Seq to match.
   * @return true if this Every has that as a suffix, false otherwise.
   */
  default boolean endsWith(Seq<? extends T> that) {
    return toVector().endsWith(that);
  }

  /**
   * Builds a new Every by applying a function to all elements of this Every
   * and using the elements of the resulting Everys.
   *
   * <pre class="stHighlighted">
   * Scalactic: def flatMap[U](f: (T) =&gt; Every[U]): Every[U]
   * </pre>
   *
   * @param <U>
   *            the element type of the returned Every
   * @param function
   *            the function to apply to each element.
   * @return a new Every containing elements obtained by applying the given
   *         function f to each element of this Every and concatenating the
   *         elements of resulting Everys.
   */
  default <U> Every<U> flatMap(Function<? super T, Every<? extends U>> function) {
    Vector<U> buf = Vector.empty();
    for (T t : toVector()) {
      buf = buf.appendAll(function.apply(t).toVector());
    }
    return fromNonEmptySeq(buf);
  }

  /**
   * Iterates over distinct permutations.
   *
   * <pre class="stHighlighted">
   * Scalactic: def permutations: Iterator[Every[T]]
   * </pre>
   *
   * @return an iterator which traverses the distinct permutations of this
   *         Every.
   */
  default Iterator<Every<T>> permutations() {
    Vector<Vector<T>> vv = toVector().permutations();
    return vv.map(Helper::fromNonEmptySeq).iterator();
  }

  /**
   * Returns the length of the longest prefix whose elements all satisfy some
   * predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def prefixLength(p: (T) =&gt; Boolean): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @return the length of the longest prefix of this Every such that every
   *         element of the segment satisfies the predicate p.
   */
  default int prefixLength(Predicate<? super T> p) {
    return toVector().prefixLength(p);
  }

  /**
   * Computes a prefix scan of the elements of this
   * Every. Note: The neutral element z may be applied more than once.
   *
   * <pre class="stHighlighted">
   * Scalactic: def scan[U &gt;: T](z: U)(op: (U, U) =&gt; U): Every[U]
   * </pre>
   *
   * @param z
   *            a neutral element for the scan operation; may be added to the
   *            result an arbitrary number of times, and must not change the
   *            result (e.g., 0 for addition, or 1 for multiplication.)
   * @param op
   *            a binary operator that must be associative
   * @return a new Every containing the prefix scan of the elements in this
   *         Every
   */
  default Every<T> scan(T z, BiFunction<? super T, ? super T, ? extends T> op) {
    return fromNonEmptySeq(toVector().scan(z, op));
  }

  /**
   * Produces an Every containing cumulative results of applying the operator
   * going left to right.
   *
   * <pre class="stHighlighted">
   * Scalactic: def scanLeft[B](z: B)(op: (B, T) =&gt; B): Every[B]
   * </pre>
   *
   * @param <B>
   *            the result type of the binary operator and type of the
   *            resulting Every
   * @param z
   *            the start value.
   * @param op
   *            the binary operator.
   * @return a new Every containing the intermediate results of inserting op
   *         between consecutive elements of this Every, going left to right,
   *         with the start value, z, on the left.
   */
  default <B> Every<B> scanLeft(B z, BiFunction<? super B, ? super T, ? extends B> op) {
    return fromNonEmptySeq(toVector().scanLeft(z, op));
  }

  /**
   * Produces an Every containing cumulative results of
   * applying the operator going right to left.
   *
   * <pre class="stHighlighted">
   * Scalactic: def scanRight[B](z: B)(op: (T, B) =&gt; B): Every[B]
   * </pre>
   *
   * @param <B>
   *            the result type of the binary operator and type of the
   *            resulting Every
   * @param z
   *            the start value
   * @param op
   *            the binary operator
   * @return a new Every containing the intermediate results of inserting op
   *         between consecutive elements of this Every, going right to left,
   *         with the start value, z, on the right.
   */
  default <B> Every<B> scanRight(B z, BiFunction<? super T, ? super B, ? extends B> op) {
    return fromNonEmptySeq(toVector().scanRight(z, op));
  }

  /**
   * Computes length of longest segment whose elements
   * all satisfy some predicate.
   *
   * <pre class="stHighlighted">
   * Scalactic: def segmentLength(p: (T) =&gt; Boolean, from: Int): Int
   * </pre>
   *
   * @param p
   *            the predicate used to test elements.
   * @param from
   *            the index where the search starts.
   * @return the length of longest segment whose elements all satisfy some
   *         predicate.
   */
  default int segmentLength(Predicate<? super T> p, int from) {
    return toVector().segmentLength(p, from);
  }

  /**
   * Groups elements in fixed size blocks by passing a "sliding window" over
   * them (as opposed to partitioning them, as is done in grouped).
   *
   * <pre class="stHighlighted">
   * Scalactic: def sliding(size: Int): Iterator[Every[T]]
   * </pre>
   *
   * @param size
   *            the number of elements per group
   * @return an iterator producing Everys of size size, except the last and
   *         the only element will be truncated if there are fewer elements
   *         than size.
   */
  default Iterator<Every<T>> sliding(int size) {
    return toVector().sliding(size).map(Helper::fromNonEmptySeq);
  }

  /**
   * Groups elements in fixed size blocks by passing a "sliding window" over
   * them (as opposed to partitioning them, as is done in grouped.), moving
   * the sliding window by a given step each time.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sliding(size: Int, step: Int): Iterator[Every[T]]
   * </pre>
   *
   * @param size
   *            the number of elements per group
   * @param step
   *            the distance between the first elements of successive groups
   * @return an iterator producing Everys of size size, except the last and
   *         the only element will be truncated if there are fewer elements
   *         than size.
   */
  default Iterator<Every<T>> sliding(int size, int step) {
    return toVector().sliding(size, step).map(Helper::fromNonEmptySeq);
  }

  /**
   * Sorts this Every according to the Ordering of the result of applying the
   * given function to every element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sortBy[U](f: (T) =&gt; U)(implicit ord: Ordering[U]): Every[T]
   * </pre>
   *
   * @param <U>
   *            the target type of the transformation f, and the type where
   *            the Comparable c is defined.
   * @param f
   *            the transformation function mapping elements to some other
   *            domain U.
   * @return a Every consisting of the elements of this Every sorted
   */
  default <U extends Comparable<? super U>> Every<T> sortBy(Function<? super T, ? extends U> f) {
    return fromNonEmptySeq(toVector().sortBy(f));
  }

  /**
   * Sorts this Every according to the Ordering of the result of applying the
   * given function to every element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sortBy[U](f: (T) =&gt; U)(implicit ord: Ordering[U]): Every[T]
   * </pre>
   *
   * @param <U>
   *            the target type of the transformation f, and the type where
   *            the Comparator c is defined.
   * @param c
   *            the comparator to use
   * @param f
   *            the transformation function mapping elements to some other
   *            domain U.
   * @return a Every consisting of the elements of this Every sorted
   */
  default <U> Every<T> sortBy(Comparator<? super U> c, Function<? super T, ? extends U> f) {
    return fromNonEmptySeq(toVector().sortBy(c, f));
  }

  /**
   * Sorts this Every according to a comparison function. The sort is stable.
   * That is, elements that are equal (as determined by lt) appear in the same
   * order in the sorted Every as in the original.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sortWith(lt: (T, T) =&gt; Boolean): Every[T]
   * </pre>
   *
   * @param lt
   *            comparison function.
   * @return an Every consisting of the elements of this Every sorted
   *         according to the comparison function lt.
   */
  default Every<T> sortWith(BiPredicate<? super T, ? super T> lt) {
    return sortBy((T left, T right) -> {
      if (lt.test(left, right))
        return -1;
      else if (lt.test(right, left))
        return 1;
      else
        return 0;
    }, Function.identity());
  }

  /**
   * Sorts this Every according to their natural order. If this elements are
   * not Comparable, a java.lang.ClassCastException may be thrown.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sorted[U &gt;: T](implicit ord: Ordering[U]): Every[U]
   * </pre>
   *
   * @return an Every consisting of the elements of this Every sorted
   *         according to their natural order.
   * @throws ClassCastException
   *             if this elements are not Comparable
   */
  default Every<T> sorted() throws ClassCastException {
    return fromNonEmptySeq(toVector().sorted());
  }

  /**
   * Sorts this Every according to the provided comparator.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sorted[U &gt;: T](implicit ord: Ordering[U]): Every[U]
   * </pre>
   *
   * @param c
   *            the comparator to use
   * @return an Every consisting of the elements of this Every sorted
   *         according to the comparator
   */
  default Every<T> sorted(Comparator<? super T> c) {
    return fromNonEmptySeq(toVector().sorted(c));
  }

  /**
   * Indicates whether this Every starts with the given Iterable.
   *
   * <pre class="stHighlighted">
   * Scalactic: def startsWith[B](that: GenSeq[B]): Boolean <br>
   * Scalactic: def startsWith[B](that: Every[B]): Boolean
   * </pre>
   *
   * @param <B>
   *            the type of the iterable.
   * @param that
   *            the Iterable to test
   * @return true if this collection has that as a prefix, false otherwise.
   */
  default <B extends T> boolean startsWith(Iterable<? extends B> that) {
    return toVector().startsWith(that);
  }

  /**
   * Indicates whether this Every starts with the given Iterable at the given
   * index.
   *
   * <pre class="stHighlighted">
   * Scalactic: def startsWith[B](that: GenSeq[B], offset: Int): Boolean <br>
   * Scalactic: def startsWith[B](that: Every[B], offset: Int): Boolean
   * </pre>
   *
   * @param <B>
   *            the type of the iterable.
   * @param that
   *            the Iterable slice to look for in this Every
   * @param offset
   *            the index at which this Every is searched.
   * @return true if this Every has that as a slice at the index offset, false
   *         otherwise.
   */
  default <B extends T> boolean startsWith(Iterable<? extends B> that, int offset) {
    return toVector().startsWith(that, offset);
  }

  /**
   * Converts this Every to a Java array.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toArray[U &gt;: T](implicit classTag: ClassTag[U]): Array[U]
   * </pre>
   *
   * @param componentType
   *            component type of the array.
   * @return a new Java array.
   */
  default T[] toJavaArray(Class<T> componentType) {
    return toVector().toJavaArray(componentType);
  }

  /**
   * Converts this Every to an array.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toArray[U &gt;: T](implicit classTag: ClassTag[U]): Array[U]
   * </pre>
   *
   * @return an array containing all elements of this Every. A ClassTag must
   *         be available for the element type of this Every.
   */
  default Array<T> toArray() {
    return toVector().toArray();
  }

  /**
   * Converts this Every to a Java {@link java.util.List}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toList: List[T]
   * </pre>
   *
   * @return a {@link java.util.List}
   */
  default java.util.List<T> toJavaList() {
    return toVector().toJavaList();
  }

  /**
   * Converts this Every to a {@link List}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toList: List[T]
   * </pre>
   *
   * @return a new {@link List}.
   */
  default List<T> toList() {
    return toVector().toList();
  }

  /**
   * Converts this Every to a Java {@link java.util.Map}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toMap[K, V](implicit ev: &lt;:&lt;[T, (K, V)]): Map[K, V]
   * </pre>
   *
   * @param <K>
   *            type of the maps key
   * @param <V>
   *            type of the maps value
   * @param f
   *            a function that maps an element to a key/value pair
   *            represented by Tuple2
   * @return a new {@link java.util.Map}.
   */
  default <K, V> java.util.Map<K, V> toJavaMap(Function<? super T, Tuple2<? extends K, ? extends V>> f) {
    return toVector().toJavaMap(f);
  }

  /**
   * Converts this Every to a {@link Map}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toMap[K, V](implicit ev: &lt;:&lt;[T, (K, V)]): Map[K, V]
   * </pre>
   *
   * @param <K>
   *            type of the maps key
   * @param <V>
   *            type of the maps value
   * @param f
   *            a function that maps an element to a key/value pair
   *            represented by Tuple2
   * @return a new {@link Map}.
   */
  default <K, V> Map<K, V> toMap(Function<? super T, Tuple2<? extends K, ? extends V>> f) {
    return toVector().toMap(f);
  }

  /**
   * Converts this Every to a {@link java.util.Set}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toSet[U &gt;: T]: Set[U]
   * </pre>
   *
   * @return a new {@link java.util.Set}.
   */
  default java.util.Set<T> toJavaSet() {
    return toVector().toJavaSet();
  }

  /**
   * Converts this value to a {@link Set}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toSet[U &gt;: T]: Set[U]
   * </pre>
   *
   * @return a new {@link Set}.
   */
  default Set<T> toSet() {
    return toVector().toSet();
  }

  /**
   * Converts this Every to a {@link java.util.stream.Stream}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toStream: Stream[T]
   * </pre>
   *
   * @return a new {@link java.util.stream.Stream}.
   */
  default java.util.stream.Stream<T> toJavaStream() {
    return toVector().toJavaStream();
  }

  /**
   * Converts this value to a {@link Stream}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toStream: Stream[T]
   * </pre>
   *
   * @return a new {@link Stream}.
   */
  default Stream<T> toStream() {
    return toVector().toStream();
  }

  /**
   * Converts this value to a {@link Seq}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toSeq: Seq[T]
   * </pre>
   *
   * @return a new {@link Seq}.
   */
  default Seq<T> toSeq() {
    return toVector();
  }

  /**
   * Converts this value to a {@link Traversable}.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toTraversable: Traversable[T]
   * </pre>
   *
   * @return a new {@link Traversable}.
   */
  default Traversable<T> toTraversable() {
    return toVector();
  }

  /**
   * Produces a new Every that contains all elements of this Every and
   * all elements of a given Iterable.
   *
   * <pre class="stHighlighted">
   * Scalactic: def union[U &gt;: T](that: Every[U]): Every[U]
   * </pre>
   *
   * @param that
   *            the Iterable to add.
   * @return a new Every that contains all elements of this Every and all
   *         elements of that Iterable.
   */
  default Every<T> union(Iterable<? extends T> that) {
    return fromNonEmptySeq(toVector().appendAll(that));
  }

  /**
   * Converts this Every of triples into three Everys of the first, second,
   * and and third element of each triple.
   *
   * <pre class="stHighlighted">
   * Scalactic: def Every[U] unzip3[L, M, R](implicit asTriple: (T) =&gt; (L, M, R)): (Every[L], Every[M], Every[R])
   * </pre>
   *
   * @param <L>
   *            the type of the first third of the element triples
   * @param <M>
   *            the type of the second third of the element triples
   * @param <R>
   *            the type of the third third of the element triples
   * @param unzipper
   *            a function that makes triples of the elements of this Every.
   * @return a triple of Everys, containing the first, second, and third
   *         member, respectively, of each element triple of this Every.
   */
  default <L, M, R> Tuple3<Every<L>, Every<M>, Every<R>> unzip3(
    Function<? super T, Tuple3<? extends L, ? extends M, ? extends R>> unzipper) {
    Tuple3<Vector<L>, Vector<M>, Vector<R>> unzipped = toVector().unzip3(unzipper);
    return Tuple.of(fromNonEmptySeq(unzipped._1), fromNonEmptySeq(unzipped._2), fromNonEmptySeq(unzipped._3));
  }

  /**
   * Converts this Every of pairs into two Everys of the first and second half
   * of each pair.
   *
   * <pre class="stHighlighted">
   * Scalactic: def unzip[L, R](implicit asPair: (T) =&gt; (L, R)): (Every[L], Every[R])
   * </pre>
   *
   * @param <L>
   *            the type of the first half of the element pairs
   * @param <R>
   *            the type of the second half of the element pairs
   * @param unzipper
   *            a function that makes pairs of the elements of this Every.
   * @return a pair of Everys, containing the first and second half,
   *         respectively, of each element pair of this Every.
   */
  default <L, R> Tuple2<Every<L>, Every<R>> unzip(Function<? super T, Tuple2<? extends L, ? extends R>> unzipper) {
    Tuple2<Vector<L>, Vector<R>> unzipped = toVector().unzip(unzipper);
    return Tuple.of(fromNonEmptySeq(unzipped._1), fromNonEmptySeq(unzipped._2));
  }

  /**
   * A copy of this Every with one single replaced element.
   *
   * <pre class="stHighlighted">
   * Scalactic: def updated[U &gt;: T](idx: Int, elem: U): Every[U]
   * </pre>
   *
   * @param index
   *            the position of the replacement
   * @param elem
   *            the replacing element
   * @return a copy of this Every with the element at position index replaced
   *         by elem.
   */
  default Every<T> updated(int index, T elem) {
    return fromNonEmptySeq(toVector().update(index, elem));
  }

  /**
   * Returns an Every formed from this Every and an iterable collection by
   * combining corresponding elements in pairs. If one of the two collections
   * is shorter than the other, placeholder elements will be used to extend
   * the shorter collection to the length of the longer.
   *
   * <pre class="stHighlighted">
   * Scalactic: def zipAll[O, U &gt;: T](other: Iterable[O], thisElem: U, otherElem: O): Every[(U, O)]
   * </pre>
   *
   * @param <B>
   *            type of the iterable to zip with
   * @param other
   *            the Iterable providing the second half of each result pair
   * @param thisElem
   *            the element to be used to fill up the result if this Every is
   *            shorter than that Iterable.
   * @param otherElem
   *            the element to be used to fill up the result if that is
   *            shorter than this Every.
   * @return a new Every containing pairs consisting of corresponding elements
   *         of this Every and that. The length of the returned collection is
   *         the maximum of the lengths of this Every and that. If this Every
   *         is shorter than that, thisElem values are used to pad the result.
   *         If that is shorter than this Every, thatElem values are used to
   *         pad the result.
   */
  @SuppressWarnings("unchecked")
  default <B> Every<Tuple2<T, B>> zipAll(Iterable<? extends B> other, T thisElem, B otherElem) {
    return fromNonEmptySeq(toVector().zipAll(other, thisElem, otherElem));
  }

  /**
   * Zips this Every with its indices.
   *
   * <pre class="stHighlighted">
   * Scalactic: def zipWithIndex: Every[(T, Int)]
   * </pre>
   *
   * @return A new Every containing pairs consisting of all elements of this
   *         Every paired with their index. Indices start at 0.
   */
  default Every<Tuple2<T, Integer>> zipWithIndex() {
    return fromNonEmptySeq(toVector().zipWithIndex());
  }

  /**
   * The result of multiplying all the elements of this Every.
   *
   * <pre class="stHighlighted">
   * Scalactic: def product[U &gt;: T](implicit num: Numeric[U]): U
   * </pre>
   *
   * @return the product of all elements
   * @throws UnsupportedOperationException
   *             if this elements are not numeric
   */
  default Number product() {
    return toVector().product();
  }

  /**
   * The result of summing all the elements of this Every. Supported component
   * types are Byte, Double, Float, Integer, Long, Short, BigInteger and
   * BigDecimal.
   *
   * <pre class="stHighlighted">
   * Scalactic: def sum[U &gt;: T](implicit num: Numeric[U]): U
   * </pre>
   *
   * @return the sum of all elements
   * @throws UnsupportedOperationException
   *             if this elements are not numeric
   */
  default Number sum() {
    return toVector().sum();
  }

  /**
   * Displays all elements of this Every in a string.
   *
   * <pre class="stHighlighted">
   * Scalactic: def mkString: String
   * </pre>
   *
   * @return a string representation of this Every. In the resulting string,
   *         the result of invoking toString on all elements of this Every
   *         follow each other without any separator string.
   */
  default String mkString() {
    return toVector().mkString();
  }

  /**
   * Displays all elements of this Every in a string using a separator string.
   *
   * <pre class="stHighlighted">
   * Scalactic: def mkString(sep: String): String
   * </pre>
   *
   * @param separator
   *            the separator string
   * @return a string representation of this Every. In the resulting string,
   *         the result of invoking toString on all elements of this Every are
   *         separated by the string sep.
   */
  default String mkString(CharSequence separator) {
    return toVector().mkString(separator);
  }

  /**
   * Displays all elements of this Every in a string using start, end, and
   * separator strings.
   *
   * <pre class="stHighlighted">
   * Scalactic: def mkString(start: String, sep: String, end: String): String
   * </pre>
   *
   * @param prefix
   *            the starting string.
   * @param sep
   *            the separator string.
   * @param suffix
   *            the ending string.
   * @return a string representation of this Every. The resulting string
   *         begins with the string start and ends with the string end.
   *         Inside, In the resulting string, the result of invoking toString
   *         on all elements of this Every are separated by the string sep.
   */
  default String mkString(CharSequence prefix, CharSequence sep, CharSequence suffix) {
    return toVector().mkString(prefix, sep, suffix);
  }

  /**
   * Returns true to indicate this Every, like all Everys, is non-empty.
   *
   * <pre class="stHighlighted">
   * Scalactic: def nonEmpty: Boolean
   * </pre>
   *
   * @return true
   */
  default boolean nonEmpty() {
    return true;
  }

    /*
     * transpose[U](implicit ev: &lt;:&lt;[T, Every[U]]): Every[Every[U]]
     * union[U >: T](that: GenSeq[U])(implicit cbf: CanBuildFrom[Vector[T], U,
     * Vector[U]]):
     */

  // ----------------------------------------------------------------------------------
  // Partial Function methods
  // ----------------------------------------------------------------------------------

  /**
   * Turns this partial function into a plain function returning an Option
   * result.
   *
   * @return a function that takes an argument x to Some(this(x)) if this is
   *         defined for x, and to None otherwise.
   */
  default IntFunction<Option<T>> lift() {
    return (int a) -> isDefinedAt(a) ? Option.of(apply(a)) : Option.none();
  }

  /**
   * Composes this partial function with a fallback partial function which
   * gets applied where this partial function is not defined.
   *
   * <pre class="stHighlighted">
   * def orElse[A1 &lt;: A, B1 &gt;: B](that: PartialFunction[A1, B1]): PartialFunction[A1, B1]
   * </pre>
   *
   * @param that
   *            the fallback function
   * @return a partial function which has as domain the union of the domains
   *         of this partial function and that. The resulting partial function
   *         takes x to this(x) where this is defined, and to that(x) where it
   *         is not.
   */
  default IntFunction<T> orElse(IntFunction<? extends T> that) {
    return (index) -> {
      if (isDefinedAt(index)) {
        return this.apply(index);
      } else {
        return that.apply(index);
      }
    };
  }

  /**
   * Composes this partial function with an action function which gets applied
   * to results of this partial function. The action function is invoked only
   * for its side effects; its result is ignored.
   *
   * <pre class="stHighlighted">
   * def runWith[U](action: (T) ⇒ U): (Int) ⇒ Boolean
   * </pre>
   *
   * @param action
   *            the action function
   * @return a function which maps arguments x to isDefinedAt(x). The
   *         resulting function runs action(this(x)) where this is defined.
   */
  default IntPredicate runWith(Consumer<? super T> action) {
    return (int a) -> {
      if (isDefinedAt(a)) {
        action.accept(apply(a));
        return true;
      } else
        return false;
    };
  }

}
