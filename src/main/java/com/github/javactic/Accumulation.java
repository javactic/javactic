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

import javaslang.Function3;
import javaslang.Function4;
import javaslang.Function5;
import javaslang.Function6;
import javaslang.Function7;
import javaslang.Function8;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.collection.Iterator;
import javaslang.collection.Stream;
import javaslang.collection.Vector;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Provides mechanisms that enable errors to be accumulated in "accumulating
 * Ors," {@link Or}s whose Bad type is an {@link Every}.
 * <p>
 * The mechanisms are:
 * <ul>
 * <li>Passing accumulating Ors to withGood methods</li>
 * <li>Invoking combined on a container of accumulating Ors</li>
 * <li>Invoking validatedBy on a container of any type, passing in a function
 * from that type to an accumulating Or</li>
 * <li>Invoking zip on an accumulating Or</li>
 * <li>Invoking when on an accumulating Or</li>
 * </ul>
 *
 * @author mvh
 */
public final class Accumulation {

  private Accumulation() {}

  /**
   * Combines an Iterable of Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt; (where
   * EVERY is some subtype of Every) into a single Or of type
   * Or&lt;Vector&lt;G&gt;, Every&lt;ERR&gt;&gt;.
   *
   * @param <G>   the success type of the resulting Or
   * @param <ERR> the failure type of the resulting Or
   * @param input the iterable containing Ors to combine
   * @return an Or of all the success values or of all the errors
   */
  public static <G, ERR> Or<Vector<G>, Every<ERR>>
  combined(Iterable<? extends Or<? extends G, ? extends Every<? extends ERR>>> input) {
    return combined(input, Vector.<G>collector());
  }

  /**
   * Combines an Iterable of Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt; (where
   * EVERY is some subtype of Every) into a single Or of type
   * Or&lt;COLL&lt;G&gt;, Every&lt;ERR&gt;&gt; using a Collector to determine
   * the wanted collection type COLL.
   *
   * @param <G>       the success type of the resulting Or
   * @param <A>       the mutable accumulation type of the reduction operation of the collector
   * @param <ERR>     the failure type of the resulting Or
   * @param <I>       the resulting success iterable type
   * @param input     the iterable containing Ors to combine
   * @param collector the collector producing the resulting collection
   * @return an Or of all the success values or of all the errors
   */
  public static <G, A, ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>>
  combined(Iterable<? extends Or<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {
    A goods = collector.supplier().get();
    Vector<ERR> errs = Vector.empty();
    for (Or<? extends G, ? extends Every<? extends ERR>> or : input) {
      if (or.isGood())
        collector.accumulator().accept(goods, or.get());
      else
        errs = errs.appendAll(or.getBad().toVector());
    }
    if (errs.isEmpty()) {
      I gds = collector.finisher().apply(goods);
      return Good.of(gds);
    } else
      return Bad.of(Every.of(errs.head(), errs.tail()));
  }

  /**
   * Maps an iterable of Fs into an Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt;
   * (where EVERY is some subtype of Every) using the passed function f, then
   * combines the resulting Ors into a single Or of type Or&lt;Vector&lt;G&gt;,
   * Every&lt;ERR&gt;&gt;.
   * <p>
   * Note: the process implemented by this method is sometimes called a
   * &quot;traverse&quot;.
   * </p>
   *
   * @param <F>      the type of the original iterable to validate
   * @param <G>      the Good type of the resulting Or
   * @param <ERR>    the Bad type of the resulting Or
   * @param iterable the iterable to validate
   * @param f        the validation function
   * @return an Or of all the success values or of all the errors
   */
  public static <F, G, ERR> Or<Vector<G>, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends Or<? extends G, ? extends Every<? extends ERR>>> f) {
    return validatedBy(iterable, f, Vector.collector());
  }

  /**
   * Maps an iterable of Fs into Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt;
   * (where EVERY is some subtype of Every) using the passed function f, then
   * combines the resulting Ors into a single Or of type Or&lt;COLL&lt;G&gt;,
   * Every&lt;ERR&gt;&gt; using a Collector to determine the wanted collection
   * type COLL.
   * <p>
   * Note: the process implemented by this method is sometimes called a
   * &quot;traverse&quot;.
   * </p>
   *
   * @param <F>       the type of the original iterable to validate
   * @param <G>       the Good type of the resulting Or
   * @param <A>       the mutable accumulation type of the reduction operation of the collector
   * @param <I>       the result type of the reduction operation
   * @param <ERR>     the Bad type of the resulting Or
   * @param iterable  the iterable to validate
   * @param f         the validation function
   * @param collector the collector producing the resulting collection
   * @return an Or of all the success values or of all the errors
   */
  public static <F, G, A, I extends Iterable<? extends G>, ERR>
  Or<I, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends Or<? extends G, ? extends Every<? extends ERR>>> f,
              Collector<? super G, A, I> collector) {

    Vector<Or<? extends G, ? extends Every<? extends ERR>>> ors =
      Iterator
        .ofAll(iterable)
        .foldLeft(Vector.empty(), (vec, elem) -> vec.append(f.apply(elem)));
    return combined(ors, collector);
  }


  /**
   * Enables further validation on an existing accumulating Or by passing validation functions.
   *
   * @param <G>         the Good type of the argument Or
   * @param <ERR>       the type of the error message contained in the accumulating failure
   * @param or          the accumulating or
   * @param validations the validation functions
   * @return the original or if it passed all validations or a Bad with all failures
   */
  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <G, ERR> Or<G, Every<ERR>>
  when(Or<? extends G, ? extends Every<? extends ERR>> or, Function<? super G, ? extends Validation<ERR>>... validations) {
    return when(or, Stream.of(validations));
  }

  public static <G, ERR> Or<G, Every<ERR>>
  when(Or<? extends G, ? extends Every<? extends ERR>> or, Iterable<? extends Function<? super G, ? extends Validation<ERR>>> validations) {
    return when(or, Stream.ofAll(validations));
  }

  private static <G, ERR> Or<G, Every<ERR>>
  when(Or<? extends G, ? extends Every<? extends ERR>> or, Stream<? extends Function<? super G, ? extends Validation<ERR>>> validations) {
    if (or.isGood()) {
      Vector<ERR> result = validations
        .foldLeft(Vector.empty(), (vec, f) -> {
          Validation<ERR> v = f.apply(or.get());
          if (v.isPass()) return vec;
          else return vec.append(v.getError());
        });
      if (result.length() == 0) return Good.of(or.get());
      else return Bad.of(Every.of(result.head(), result.tail()));
    } else return (Or<G, Every<ERR>>) or;
  }



  // ------------------------------------------------------------------------
  // ZIP
  // ------------------------------------------------------------------------

  /**
   * Zips two accumulating Ors together. If both are Good, returns a Good
   * tuple containing both original Good values. Otherwise, returns a Bad
   * containing every error message.
   *
   * @param <A>   the success type of the first Or
   * @param <B>   the success type of the second Or
   * @param <ERR> the error type of the accumulating Ors
   * @param a     the first Or to zip
   * @param b     the second Or to zip
   * @return an Or with a success type of Tuple2
   */
  public static <A, B, ERR> Or<Tuple2<A, B>, Every<ERR>> zip(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b) {
    return withGood(a, b, Tuple::of);
  }

  public static <A, B, C, ERR> Or<Tuple3<A, B, C>, Every<ERR>> zip3(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c) {
    return withGood(a, b, c, Tuple::of);
  }

  // ------------------------------------------------------------------------
  // WITHGOOD
  // ------------------------------------------------------------------------

  /**
   * Combines two accumulating Or into a single one using the given function.
   * The resulting Or will be a Good if both Ors are Goods, otherwise it will
   * be a Bad containing every error in the Bads.
   *
   * @param <A>      the success type of the first Or
   * @param <B>      the success type of the second Or
   * @param <ERR>    the error type of the Ors
   * @param <RESULT> the success type of the resulting Or
   * @param a        the first Or to combine
   * @param b        the second Or to combine
   * @param function the function combining the Ors
   * @return a Good if both Ors were Goods, otherwise a Bad with every error.
   */
  public static <A, B, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    BiFunction<? super A, ? super B, ? extends RESULT> function) {
    if (allGood(a, b))
      return Good.of(function.apply(a.get(), b.get()));
    else {
      return getBads(a, b);
    }
  }

  public static <A, B, C, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Function3<? super A, ? super B, C, ? extends RESULT> function) {
    if (allGood(a, b, c))
      return Good.of(function.apply(a.get(), b.get(), c.get()));
    else {
      return getBads(a, b, c);
    }
  }

  public static <A, B, C, D, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Or<? extends D, ? extends Every<? extends ERR>> d,
    Function4<? super A, ? super B, ? super C,
      ? super D, ? extends RESULT> function) {
    if (allGood(a, b, c, d))
      return Good.of(function.apply(a.get(), b.get(), c.get(), d.get()));
    else {
      return getBads(a, b, c, d);
    }
  }

  public static <A, B, C, D, E, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Or<? extends D, ? extends Every<? extends ERR>> d,
    Or<? extends E, ? extends Every<? extends ERR>> e,
    Function5<? super A, ? super B, ? super C,
      ? super D, ? super E, ? extends RESULT> function) {
    if (allGood(a, b, c, d, e))
      return Good.of(function.apply(a.get(), b.get(), c.get(), d.get(), e.get()));
    else {
      return getBads(a, b, c, d, e);
    }
  }

  public static <A, B, C, D, E, F, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Or<? extends D, ? extends Every<? extends ERR>> d,
    Or<? extends E, ? extends Every<? extends ERR>> e,
    Or<? extends F, ? extends Every<? extends ERR>> f,
    Function6<? super A, ? super B, ? super C,
      ? super D, ? super E, ? super F, ? extends RESULT> function) {
    if (allGood(a, b, c, d, e, f))
      return Good.of(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get()));
    else {
      return getBads(a, b, c, d, e, f);
    }
  }

  public static <A, B, C, D, E, F, G, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Or<? extends D, ? extends Every<? extends ERR>> d,
    Or<? extends E, ? extends Every<? extends ERR>> e,
    Or<? extends F, ? extends Every<? extends ERR>> f,
    Or<? extends G, ? extends Every<? extends ERR>> g,
    Function7<? super A, ? super B, ? super C, ? super D,
      ? super E, ? super F, ? super G, ? extends RESULT> function) {
    if (allGood(a, b, c, d, e, f, g))
      return Good.of(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get(), g.get()));
    else {
      return getBads(a, b, c, d, e, f, g);
    }
  }

  public static <A, B, C, D, E, F, G, H, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
    Or<? extends A, ? extends Every<? extends ERR>> a,
    Or<? extends B, ? extends Every<? extends ERR>> b,
    Or<? extends C, ? extends Every<? extends ERR>> c,
    Or<? extends D, ? extends Every<? extends ERR>> d,
    Or<? extends E, ? extends Every<? extends ERR>> e,
    Or<? extends F, ? extends Every<? extends ERR>> f,
    Or<? extends G, ? extends Every<? extends ERR>> g,
    Or<? extends H, ? extends Every<? extends ERR>> h,
    Function8<? super A, ? super B, ? super C, ? super D,
      ? super E, ? super F, ? super G, ? super H, ? extends RESULT> function) {
    if (allGood(a, b, c, d, e, f, g, h))
      return Good.of(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get(), g.get(), h.get()));
    else {
      return getBads(a, b, c, d, e, f, g, h);
    }
  }

  // ------------------------------------------------------------------------

  private static boolean allGood(Or<?, ?>... ors) {
    return Stream.of(ors).forAll(Or::isGood);
  }

  @SafeVarargs
  private static <G, ERR> Bad<G, Every<ERR>> getBads(Or<?, ? extends Every<? extends ERR>>... ors) {
    Vector<ERR> errs = Vector.empty();
    for (Or<?, ? extends Every<? extends ERR>> or : ors) {
      if (or.isBad()) errs = errs.appendAll(or.getBad().toVector());
    }
    return Bad.of(Every.of(errs.head(), errs.tail()));
  }
}
