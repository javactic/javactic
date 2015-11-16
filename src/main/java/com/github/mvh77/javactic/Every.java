/*
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
 */
package com.github.mvh77.javactic;

import static com.github.mvh77.javactic.Helper.fromNonEmptySeq;
import static com.github.mvh77.javactic.Helper.niy;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Array;
import javaslang.collection.Iterator;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import javaslang.collection.Stream;
import javaslang.collection.Traversable;
import javaslang.collection.Vector;
import javaslang.control.Option;

public interface Every<T> extends IntFunction<T>, Iterable<T> {

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toVector: Vector[T] </pre>
     * 
     * @return
     */
    Vector<T> toVector();

    default boolean isDefinedAt(Integer a) {
        return a > 0 && a < length();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toIterator: Iterator[T] </pre>
     * 
     * @return
     */
    @Override
    default Iterator<T> iterator() {
        return toVector().iterator();
    }

	@SafeVarargs
    public static <T> Every<T> of(T first, T... rest) {
        return of(first, Vector.ofAll(rest));
    }

    public static <T> Every<T> of(T first, Seq<? extends T> rest) {
        if (rest.length() == 0)
            return One.of(first);
        else
            return new Many<T>(first, rest);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def ++[U &gt;: T](other: GenTraversableOnce[U]): Every[U] </pre>
     * @param iterable
     * @return
     */
    default Every<T> appendAll(Iterable<? extends T> iterable) {
        if (!iterable.iterator().hasNext())
            return this;
        else {
            return fromNonEmptySeq(toVector().appendAll(iterable));
        }
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def ++[U &gt;: T](other: Every[U]): Many[U] </pre>
     * @param other
     * @return
     */
    default Many<T> appendAll(Every<? extends T> other) {
        Vector<T> all = toVector().appendAll(other.toVector());
        return new Many<>(all.head(), all.tail());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def :+[U &gt;: T](element: U): Many[U] </pre>
     * @param element
     * @return
     */
    default Many<T> append(T element) {
        Vector<T> all = toVector().append(element);
        return new Many<>(all.head(), all.tail());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def +:[U &gt;: T](element: U): Many[U] </pre>
     * @param element
     * @return
     */
    default Many<T> prepend(T element) {
        Vector<T> all = toVector().prepend(element);
        return new Many<>(all.head(), all.tail());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def addString(sb: StringBuilder, start: String, sep: String, end: String): StringBuilder </pre>
     * @param sb
     * @param start
     * @param sep
     * @param end
     * @return
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
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def addString(sb: StringBuilder, sep: String): StringBuilder </pre>
     * @param sb
     * @param sep
     * @return
     */
    default StringBuilder addString(StringBuilder sb, String sep) {
        return addString(sb, "", sep, "");
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def addString(sb: StringBuilder): StringBuilder </pre>
     * @param sb
     * @return
     */
    default StringBuilder addString(StringBuilder sb) {
        return addString(sb, "");
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def contains(elem: Any): Boolean </pre>
     * @param elem
     * @return
     */
    default boolean contains(T elem) {
        return toVector().contains(elem);
    }
    
    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def containsSlice[B](that: GenSeq[B]): Boolean <br>
     * Scalactic: def containsSlice[B](that: Every[B]): Boolean</pre>
     * 
     * @param that
     * @return
     */
    default boolean containsSlice(Iterable<? extends T> that) {
        return toVector().containsSlice(that);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def copyToArray[U &gt;: T](arr: Array[U], start: Int, len: Int): Unit </pre>
     * @param target
     * @param start
     * @param length
     */
    default void copyToArray(T[] target, int start, int length) {
        int i = start;
        int end = Math.min((start + length), target.length);
        Iterator<T> it = iterator();
        while (i < end && it.hasNext()) {
            target[i] = it.next();
            i += 1;
        }
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def copyToArray[U &gt;: T](arr: Array[U], start: Int): Unit </pre>
     * @param target
     * @param start
     */
    default void copyToArray(T[] target, int start) {
        copyToArray(target, start, length());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def copyToArray[U &gt;: T](arr: Array[U]): Unit </pre>
     * @param target
     */
    default void copyToArray(T[] target) {
        copyToArray(target, 0);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def corresponds[B](that: GenSeq[B])(p: (T, B) =&gt; Boolean): Boolean <br>
     * Scalactic: def corresponds[B](that: Every[B])(p: (T, B) =&gt; Boolean): Boolean </pre>
     * @param that
     * @param predicate
     * @return
     */
    default <B> boolean corresponds(Iterable<B> that, BiPredicate<T, B> predicate) {
        return toVector().corresponds(that, predicate);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def count(p: (T) =&gt; Boolean): Int </pre>
     * @param predicate
     * @return
     */
    default long count(Predicate<T> predicate) {
        return toVector().filter(predicate).length();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def exists(p: (T) =&gt; Boolean): Boolean </pre>
     * @param predicate
     * @return
     */
    default boolean exists(Predicate<? super T> predicate) {
        return toVector().exists(predicate);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def find(p: (T) =&gt; Boolean): Option[T] </pre>
     * @param predicate
     * @return
     */
    default Option<T> find(Predicate<? super T> predicate) {
        return toVector().findFirst(predicate);
    }

    default T get(int index) {
        return apply(index);
    }

    default T getOrElse(int index, IntFunction<? extends T> def) {
        return applyOrElse(index, def);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def apply(idx: Int): T </pre>
     */
    default T apply(int index) {
        return toVector().get(index);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def applyOrElse[A1 &lt;: Int, B1 &gt;: T](x: A1, default: (A1) =&gt; B1): B1 </pre>
     * @param index
     * @param def
     * @return
     */
    default T applyOrElse(int index, IntFunction<? extends T> def) {
        return length() > index ? apply(index) : def.apply(index);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def compose[A](g: (A) =&gt; Int): (A) =&gt; T </pre>
     * @param g
     * @return
     */
    default <A> Function<A, T> compose(ToIntFunction<A> g) {
        return (A a) -> apply(g.applyAsInt(a));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def distinct: Every[T] </pre>
     * @return
     */
    default Every<T> distinct() {
        return fromNonEmptySeq(toVector().distinct());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def forall(p: (T) =&gt; Boolean): Boolean </pre>
     * @param p
     * @return
     */
    default boolean forAll(Predicate<? super T> p) {
        return toVector().forAll(p);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def foreach(f: (T) =&gt; Unit): Unit </pre>
     */
    default void forEach(Consumer<? super T> action) {
        toVector().forEach(action);
    }

    default <B> Every<B> flatten() {
        return fromNonEmptySeq(toVector().<B>flatten());
        // TODO test
    }
    
    
    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def fold[U &gt;: T](z: U)(op: (U, U) =&gt; U): U </pre>
     * @param z
     * @param op
     * @return
     */
    default T fold(T z, BiFunction<? super T, ? super T, ? extends T> op) {
        return toVector().fold(z, op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def foldLeft[B](z: B)(op: (B, T) =&gt; B): B </pre>
     * @param z
     * @param op
     * @return
     */
    default <B> B foldLeft(B z, BiFunction<? super B, ? super T, ? extends B> op) {
        return toVector().foldLeft(z, op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def foldRight[B](z: B)(op: (T, B) =&gt; B): B </pre>
     * @param z
     * @param op
     * @return
     */
    default <B> B foldRight(B z, BiFunction<? super T, ? super B, ? extends B> op) {
        return toVector().foldRight(z, op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def groupBy[K](f: (T) =&gt; K): Map[K, Every[T]] </pre>
     * @param f
     * @return
     */
    default <K> Map<K, Every<T>> groupBy(Function<? super T, ? extends K> f) {
        return toVector().groupBy(f).map((k, v) -> Tuple.of(k, fromNonEmptySeq(v)));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def grouped(size: Int): Iterator[Every[T]] </pre>
     * @param size
     * @return
     */
    default Iterator<Every<T>> grouped(int size) {
        return toVector().grouped(size).map(Helper::fromNonEmptySeq);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def head: T </pre>
     * @return
     */
    default T head() {
        return toVector().head();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def headOption: Option[T] </pre>
     * @return
     */
    default Option<T> headOption() {
        return toVector().headOption();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def indexOf[U &gt;: T](elem: U): Int </pre>
     * @param elem
     * @return
     */
    default <U extends T> int indexOf(U elem) {
        return toVector().indexOf(elem);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def indexOf[U &gt;: T](elem: U, from: Int): Int </pre>
     * @param elem
     * @param from
     * @return
     */
    default <U extends T> int indexOf(U elem, int from) {
        return toVector().indexOf(elem, from);
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def indexOfSlice[U &gt;: T](that: GenSeq[U]): Int <br> 
     * Scalactic: def indexOfSlice[U &gt;: T](that: Every[U]): Int </pre>
     * 
     * @param that
     * @return
     */
    default int indexOfSlice(Iterable<T> that) {
        return toVector().indexOfSlice(that);
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def indexOfSlice[U &gt;: T](that: GenSeq[U], end: Int): Int <br>
     * Scalactic: def indexOfSlice[U &gt;: T](that: Every[U], end: Int): Int</pre>
     * @param that
     * @param from
     * @return
     */
    default int indexOfSlice(Iterable<T> that, int from) {
        return toVector().indexOfSlice(that, from);
    }
    
    
    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def indexWhere(p: (T) =&gt; Boolean): Int </pre>
     * @param p
     * @return
     */
    default int indexWhere(Predicate<? super T> p) {
        return indexWhere(p, 0);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def indexWhere(p: (T) =&gt; Boolean, from: Int): Int </pre>
     * @param p
     * @param from
     * @return
     */
    default int indexWhere(Predicate<? super T> p, int from) {
        for (int i = from; i < length(); i++) {
            if (p.test(toVector().get(i)))
                return i;
        }
        return -1;
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def last: T </pre>
     * @return
     */
    default T last() {
        return toVector().last();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastOption: Option[T] </pre>
     * @return
     */
    default Option<T> lastOption() {
        return Option.of(last());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastIndexOf[U &gt;: T](elem: U): Int </pre>
     * @param elem
     * @return
     */
    default int lastIndexOf(T elem) {
        return toVector().lastIndexOf(elem);
    }

    /**
     * --.
     *  
     * <pre class="stHighlighted">Scalactic: def lastIndexOf[U &gt;: T](elem: U, end: Int): Int </pre>
     * @param elem
     * @param end
     * @return
     */
    default int lastIndexOf(T elem, int end) {
        return toVector().lastIndexOf(elem, end);
    }
    
    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastIndexOfSlice[U &gt;: T](that: GenSeq[U]): Int <br>
     * Scalactic: def lastIndexOfSlice[U &gt;: T](that: Every[U]): Int </pre>
     * @param that
     * @return
     */
    default int lastIndexOfSlice(Iterable<T> that) {
        return toVector().lastIndexOfSlice(that);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastIndexOfSlice[U &gt;: T](that: GenSeq[U], end: Int): Int <br>
     * Scalactic: def lastIndexOfSlice[U &gt;: T](that: Every[U], end: Int): Int</pre>
     * @param that
     * @param end
     * @return
     */
    default int lastIndexOfSlice(Iterable<T> that, int end) {
        return toVector().lastIndexOfSlice(that, end);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastIndexWhere(p: (T) =&gt; Boolean): Int </pre>
     * @param p
     * @return
     */
    default int lastIndexWhere(Predicate<? super T> p) {
        return lastIndexWhere(p, 0);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lastIndexWhere(p: (T) =&gt; Boolean, end: Int): Int </pre>
     * @param p
     * @param end
     * @return
     */
    default int lastIndexWhere(Predicate<? super T> p, int end) {
        int e = Math.min(end, length());
        for (int i = e; i >= 0; i--) {
            if (p.test(toVector().get(i)))
                return i;
        }
        return -1;
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def size: Int </pre>
     * @return
     */
    default int size() {
        return length();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def length: Int </pre>
     * @return
     */
    default int length() {
        return toVector().length();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def lengthCompare(len: Int): Int </pre>
     * @param len
     * @return
     */
    default int lengthCompare(int len) {
        return length() - len;
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def map[U](f: (T) =&gt; U): Every[U] </pre>
     * @param f
     * @return
     */
    default <U> Every<U> map(Function<? super T, U> f) {
        return fromNonEmptySeq(toVector().map(f));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def max[U &gt;: T](implicit cmp: Ordering[U]): T </pre>
     * @return
     * TODO: should this method exist?
     */
    default T max() {
        return toVector().max().get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def maxBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T </pre>
     * @param c
     * @return
     */
    default T maxBy(Comparator<? super T> c) {
        return toVector().maxBy(c).get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def maxBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T </pre>
     * @param f
     * @param c
     * @return
     */
    default <U extends Comparable<? super U>> T maxBy(Function<? super T, ? extends U> f) {
        return toVector().maxBy(f).get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def min[U &gt;: T](implicit cmp: Ordering[U]): T </pre>
     * @return
     * TODO: should this method exist?
     */
    default T min() {
        return toVector().min().get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def minBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T </pre>
     * @param c
     * @return
     */
    default T minBy(Comparator<? super T> c) {
        return toVector().minBy(c).get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def minBy[U](f: (T) =&gt; U)(implicit cmp: Ordering[U]): T </pre>
     * @param f
     * @param c
     * @return
     */
    default <U extends Comparable<? super U>> T minBy(Function<? super T, ? extends U> f) {
        return toVector().minBy(f).get();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def padTo[U &gt;: T](len: Int, elem: U): Every[U] </pre>
     * @param len
     * @param elem
     * @return
     */
    default Every<T> padTo(int len, T elem) {
        return fromNonEmptySeq(toVector().padTo(len, elem));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def patch[U &gt;: T](from: Int, that: Every[U], replaced: Int): Every[U] </pre>
     * @param from
     * @param that
     * @param replaced
     * @return
     */
    default Every<T> patch(int from, Every<? extends T> that, int replaced) {
        return fromNonEmptySeq(toVector().patch(from, that.toVector(), replaced));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduce[U &gt;: T](op: (U, U) =&gt; U): U </pre>
     * @param op
     * @return
     */
    default T reduce(BiFunction<? super T, ? super T, ? extends T> op) {
        return toVector().reduce(op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduceOption[U &gt;: T](op: (U, U) =&gt; U): Option[U] </pre>
     * @param op
     * @return
     */
    default Option<T> reduceOption(BiFunction<? super T, ? super T, ? extends T> op) {
        return Option.of(toVector().reduce(op));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduceLeft[U &gt;: T](op: (U, T) =&gt; U): U </pre>
     * @param op
     * @return
     */
    default T reduceLeft(BiFunction<? super T, ? super T, ? extends T> op) {
        return toVector().reduceLeft(op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduceLeftOption[U &gt;: T](op: (U, T) =&gt; U): Option[U] </pre>
     * @param op
     * @return
     */
    default Option<T> reduceLeftOption(BiFunction<? super T, ? super T, ? extends T> op) {
        return Option.of(toVector().reduceLeft(op));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduceRight[U &gt;: T](op: (T, U) =&gt; U): U </pre>
     * @param op
     * @return
     */
    default T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
        return toVector().reduceRight(op);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reduceRightOption[U &gt;: T](op: (T, U) =&gt; U): Option[U] </pre>
     * @param op
     * @return
     */
    default Option<T> reduceRightOption(BiFunction<? super T, ? super T, ? extends T> op) {
        return Option.of(toVector().reduceRight(op));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reverse: Every[T] </pre>
     * @return
     */
    default Every<T> reverse() {
        return fromNonEmptySeq(toVector().reverse());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reverseIterator: Iterator[T] </pre>
     * @return
     */
    default Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            private int i = length();

            @Override
            public boolean hasNext() {
                return i > 0;
            }

            @Override
            public T next() {
                return toVector().get(--i);
            }
        };
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def reverseMap[U](f: (T) =&gt; U): Every[U] </pre>
     * @param f
     * @return
     */
    default <U> Every<U> reverseMap(Function<T, U> f) {
        Vector<U> v = reverseIterator().map(f).toVector();
        return fromNonEmptySeq(v);
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def sameElements[U &gt;: T](that: GenIterable[U]): Boolean <br>
     * Scalactic: def sameElements[U &gt;: T](that: Every[U]): Boolean </pre>
     * @param that
     * @return
     */
    default boolean sameElements(Iterable<? super T> that) {
        return toVector().eq(that);
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def endsWith[B](that: GenSeq[B]): Boolean <br>
     * Scalactic: def endsWith[B](that: Every[B]): Boolean </pre>
     * @param that
     * @return
     */
    default <B> boolean endsWith(Iterable<B> that) {
        return niy(); // TODO
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def flatMap[U](f: (T) =&gt; Every[U]): Every[U] </pre>
     * 
     * @param function
     * @return
     */
    default <U> Every<U> flatMap(Function<? super T, Every<U>> function) {
        Vector<U> buf = Vector.empty();
        for (T t : toVector()) {
            buf = buf.appendAll(function.apply(t).toVector());
        }
        return fromNonEmptySeq(buf);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def permutations: Iterator[Every[T]] </pre>
     * 
     * @return
     */
    default Iterator<Every<T>> permutations() {
        Vector<Vector<T>> vv = toVector().permutations();
        return vv.map(v -> fromNonEmptySeq(v)).iterator();
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def prefixLength(p: (T) =&gt; Boolean): Int </pre>
     * 
     * @param p
     * @return
     */
    default int prefixLength(Predicate<? super T> p) {
        return niy(); // TODO
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def scan[U &gt;: T](z: U)(op: (U, U) =&gt; U): Every[U] </pre>
     * 
     * @param z
     * @param op
     * @return
     */
    default Every<T> scan(T z, BiFunction<? super T, ? super T, ? extends T> op) {
        return niy(); // TODO
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def scanLeft[B](z: B)(op: (B, T) =&gt; B): Every[B] </pre>
     * 
     * @param z
     * @param op
     * @return
     */
    default <B> Every<B> scanLeft(B z, BiFunction<? super B, ? super T, ? extends B> op) {
        return niy(); // TODO
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def scanRight[B](z: B)(op: (T, B) =&gt; B): Every[B] </pre>
     * 
     * @param z
     * @param op
     * @return
     */
    default <B> Every<B> scanRight(B z, BiFunction<? super T, ? super B, ? extends B> op) {
        return niy(); // TODO
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def segmentLength(p: (T) =&gt; Boolean, from: Int): Int </pre>
     * 
     * @param p
     * @param from
     * @return
     */
    default int segmentLength(Predicate<? super T> p, int from) {
        return niy(); // TODO
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sliding(size: Int): Iterator[Every[T]] </pre>
     * 
     * @param size
     * @return
     */
    default Iterator<Every<T>> sliding(int size) {
        return toVector().sliding(size).map(Helper::fromNonEmptySeq);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sliding(size: Int, step: Int): Iterator[Every[T]] </pre>
     * 
     * @param size
     * @param step
     * @return
     */
    default Iterator<Every<T>> sliding(int size, int step) {
        return toVector().sliding(size, step).map(Helper::fromNonEmptySeq);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sortBy[U](f: (T) =&gt; U)(implicit ord: Ordering[U]): Every[T] </pre>
     * 
     * @param f
     * @return
     */
    default <U extends Comparable<? super U>> Every<T> sortBy(Function<? super T, ? extends U> f) {
        return fromNonEmptySeq(toVector().sortBy(f));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sortBy[U](f: (T) =&gt; U)(implicit ord: Ordering[U]): Every[T] </pre>
     * 
     * @param c
     * @param f
     * @return
     */
    default <U> Every<T> sortBy(Comparator<? super U> c, Function<? super T, ? extends U> f) {
        return fromNonEmptySeq(toVector().sortBy(c, f));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sortWith(lt: (T, T) =&gt; Boolean): Every[T] </pre>
     * 
     * @param lt
     * @return
     */
    default Every<T> sortWith(BiPredicate<T, T> lt) {
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
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sorted[U &gt;: T](implicit ord: Ordering[U]): Every[U] </pre>
     * 
     * @return
     */
    default Every<T> sorted() {
        return fromNonEmptySeq(toVector().sort());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sorted[U &gt;: T](implicit ord: Ordering[U]): Every[U] </pre>
     * 
     * @param c
     * @return
     */
    default Every<T> sorted(Comparator<? super T> c) {
        return fromNonEmptySeq(toVector().sort(c));
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def startsWith[B](that: GenSeq[B]): Boolean <br>
     * Scalactic: def startsWith[B](that: Every[B]): Boolean</pre>
     * 
     * @param that
     * @return
     */
    default <B extends T> boolean startsWith(Iterable<B> that) {
        return toVector().startsWith(that);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def startsWith[B](that: GenSeq[B], offset: Int): Boolean <br>
     * Scalactic: def startsWith[B](that: Every[B], offset: Int): Boolean</pre>
     * 
     * @param that
     * @param offset
     * @return
     */
    default <B extends T> boolean startsWith(Iterable<B> that, int offset) {
        return toVector().startsWith(that, offset);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toArray[U &gt;: T](implicit classTag: ClassTag[U]): Array[U] </pre>
     * 
     * @param componentType
     * @return
     */
    default T[] toJavaArray(Class<T> componentType) {
        return toVector().toJavaArray(componentType);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toArray[U &gt;: T](implicit classTag: ClassTag[U]): Array[U] </pre>
     * 
     * @return
     */
    default Array<T> toArray() {
        return toVector().toArray();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toList: List[T] </pre>
     * 
     * @return
     */
    default java.util.List<T> toJavaList() {
        return toVector().toJavaList();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toList: List[T] </pre>
     * 
     * @return
     */
    default List<T> toList() {
        return toVector().toList();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toMap[K, V](implicit ev: &lt;:&lt;[T, (K, V)]): Map[K, V] </pre>
     * 
     * @param f
     * @return
     */
    default <K, V> java.util.Map<K, V> toJavaMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        return toVector().toJavaMap(f);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toMap[K, V](implicit ev: &lt;:&lt;[T, (K, V)]): Map[K, V] </pre>
     * 
     * @param f
     * @return
     */
    default <K, V> Map<K, V> toMap(Function<? super T, ? extends Tuple2<? extends K, ? extends V>> f) {
        return toVector().toMap(f);
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toSet[U &gt;: T]: Set[U] </pre>
     * 
     * @return
     */
    default java.util.Set<T> toJavaSet() {
        return toVector().toJavaSet();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toSet[U &gt;: T]: Set[U] </pre>
     * 
     * @return
     */
    default Set<T> toSet() {
        return toVector().toSet();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toStream: Stream[T] </pre>
     * 
     * @return
     */
    default java.util.stream.Stream<T> toJavaStream() {
        return toVector().toJavaStream();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toStream: Stream[T] </pre>
     * 
     * @return
     */
    default Stream<T> toStream() {
        return toVector().toStream();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toSeq: Seq[T] </pre>
     * 
     * @return
     */
    default Seq<T> toSeq() {
        return toVector();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def toTraversable: Traversable[T] </pre>
     * 
     * @return
     */
    default Traversable<T> toTraversable() {
        return toVector();
    }

    /**
     * Not implemented yet.
     * 
     * <pre class="stHighlighted">Scalactic: def union[U &gt;: T](that: Every[U]): Every[U] </pre>
     * 
     * @param that
     * @return
     */
    default Every<T> union(Every<T> that) {
        return niy(); // TODO
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def Every[U] unzip3[L, M, R](implicit asTriple: (T) =&gt; (L, M, R)): (Every[L], Every[M], Every[R])</pre>
     * 
     * @param unzipper
     * @return
     */
    default <L, M, R> Tuple3<Every<L>, Every<M>, Every<R>> unzip3(Function<? super T, Tuple3<? extends L, ? extends M, ? extends R>> unzipper) {
        Tuple3<Vector<L>, Vector<M>, Vector<R>> unzipped = toVector().unzip3(unzipper);
        return Tuple.of(fromNonEmptySeq(unzipped._1),
                        fromNonEmptySeq(unzipped._2), 
                        fromNonEmptySeq(unzipped._3));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def unzip[L, R](implicit asPair: (T) =&gt; (L, R)): (Every[L], Every[R]) </pre>
     * 
     * @param unzipper
     * @return
     */
    default <L, R> Tuple2<Every<L>, Every<R>> unzip(Function<? super T, Tuple2<? extends L, ? extends R>> unzipper) {
        Tuple2<Vector<L>, Vector<R>> unzipped = toVector().unzip(unzipper);
        return Tuple.of(fromNonEmptySeq(unzipped._1), fromNonEmptySeq(unzipped._2));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def updated[U &gt;: T](idx: Int, elem: U): Every[U] </pre>
     * 
     * @param index
     * @param elem
     * @return
     */
    default Every<T> updated(int index, T elem) {
        return fromNonEmptySeq(toVector().update(index, elem));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def zipAll[O, U &gt;: T](other: Iterable[O], thisElem: U, otherElem: O): Every[(U, O)]</pre>
     * 
     * 
     * @param other
     * @param thisElem
     * @param otherElem
     * @return
     */
    default <B> Every<Tuple2<T, B>> zipAll(Iterable<B> other, T thisElem, B otherElem) {
        return fromNonEmptySeq(toVector().zipAll(other, thisElem, otherElem));
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def zipWithIndex: Every[(T, Int)] </pre>
     * 
     * @return
     */
    default Every<Tuple2<T, Integer>> zipWithIndex() {
        return fromNonEmptySeq(toVector().zipWithIndex());
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def product[U &gt;: T](implicit num: Numeric[U]): U </pre>
     * 
     * @return
     */
    default Number product() {
        return toVector().product();
    }

    /**
     * --. 
     * 
     * <pre class="stHighlighted">Scalactic: def sum[U &gt;: T](implicit num: Numeric[U]): U </pre>
     * 
     * @return
     */
    default Number sum() {
        return toVector().sum();
    }

    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def mkString: String</pre>
     * 
     * @return
     */
    default String mkString(){
        return toVector().mkString();
    }
    
    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def mkString(sep: String): String</pre>
     * 
     * @param delimiter
     * @return
     */
    default String mkString(CharSequence delimiter){
        return toVector().mkString(delimiter);
    }
    
    /**
     * --.
     * 
     * <pre class="stHighlighted">Scalactic: def mkString(start: String, sep: String, end: String): String</pre>
     * 
     * @param prefix
     * @param delimiter
     * @param suffix
     * @return
     */
    default String mkString(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return toVector().mkString(prefix, delimiter, suffix);
    }
    
    /*
     * transpose[U](implicit ev: <:<[T, Every[U]]): Every[Every[U]] 
     * union[U >: T](that: GenSeq[U])(implicit cbf: CanBuildFrom[Vector[T], U, Vector[U]]):
     */

    // ----------------------------------------------------------------------------------
    // Partial Function methods
    // ----------------------------------------------------------------------------------

    default IntFunction<Option<T>> lift() {
        return (int a) -> isDefinedAt(a) ? Option.of(apply(a)) : Option.none();
    }

    default IntPredicate runWith(Consumer<T> action) {
        return (int a) -> {
            if (isDefinedAt(a)) {
                action.accept(apply(a));
                return true;
            } else
                return false;
        };
    }

}
