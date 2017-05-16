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

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a value that is one of two possible types, with one type being
 * "success" and the other "failure."
 *
 * An Or will either be a "success" value wrapped in an instance of {@link Good} or
 * a "failure" value wrapped in an instance of {@link Bad}.
 *
 * <h1>Motivation for Or</h1>
 *
 * Please refer to <a href="http://javactic.github.io/javactic/">the
 * documentation</a> for more information. You can also check to the original
 * documentation for the <a href="http://www.scalactic.org/">Scalactic</a>
 * library or the corresponding
 * <a href="http://doc.scalatest.org/2.2.4/index.html#org.scalactic.Or">Scaladoc
 * </a>.
 *
 * @author mvh
 *
 * @param <G>
 *            the success type of the Or
 * @param <B>
 *            the failure type of the Or
 */
public interface Or<G, B> {

  /**
   * Transforms an {@link Option} into an Or.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param option the Option to transform
   * @param bad a Supplier used to get the failure value if the given Option is None
   * @return an Or
   */
  static <G, B> Or<G, B> from(Option<? extends G> option, Supplier<? extends B> bad) {
    return option.map(Or::<G, B>good).getOrElse(() -> Bad.of(bad.get()));
  }

  /**
   * Transforms an {@link Option} into an Or.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param option the Option to transform
   * @param bad the value to use for the Bad if the given option is None
   * @return an Or
   */
  static <G, B> Or<G, B> from(Option<? extends G> option, B bad) {
    return option.map(Or::<G, B>good).getOrElse(Bad.of(bad));
  }

  /**
   * Transforms an {@link Either} into an Or.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param either the Either to transform
   * @return an Or
   */
  static <B, G> Or<G, B> from(Either<? extends B, ? extends G> either) {
    if (either.isRight()) return Good.of(either.right().get());
    else return Bad.of(either.left().get());
  }

  /**
   * Transforms a {@link Try} into an Or.
   *
   * @param <G> the {@link Good} type
   * @param theTry the Try to transform into an Or
   * @return an Or
   */
  static <G> Or<G, Throwable> from(Try<? extends G> theTry) {
    return theTry.map(Or::<G, Throwable>good).recover(Bad::of).get();
  }

  /**
   * Builds a {@link Good} from the given {@link Optional} if it is defined, or a {@link Bad} if it is not using
   * the given argument.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param optional the {@link Optional} whose value to use to build a {@link Good} if it is defined
   * @param bad the value to use for a {@link Bad} if the given {@link Optional} is not defined
   * @return an instance of {@link Or}
   */
  static <G, B> Or<G, B> fromJavaOptional(Optional<? extends G> optional, B bad) {
    return optional.map(Or::<G, B>good).orElse(Bad.of(bad));
  }

  /**
   * Builds a {@link Good} from the given {@link Optional} if it is defined, or a {@link Bad} if it is not using
   * the given argument.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param optional the {@link Optional} whose value to use to build a {@link Good} if it is defined
   * @param bad the supplier to use to get the value for a {@link Bad} if the given {@link Optional} is not defined
   * @return an instance of {@link Or}
   */
  static <G, B> Or<G, B> fromJavaOptional(Optional<? extends G> optional, Supplier<? extends B> bad) {
    return optional.map(Or::<G, B>good).orElseGet(() -> Bad.of(bad.get()));
  }

  /**
   * Builds an {@link Or} from the given <code>source</code> using the provided <code>converter</code> function.
   *
   * @param <S> the source type
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param source the object to convert to an {@link Or}
   * @param converter the converter to use to make an {@link Good} from <code>source</code>
   * @return an instance of {@link Or}
   */
  @SuppressWarnings("unchecked")
  static <S, G, B> Or<G, B> fromAny(S source, Function<? super S, ? extends Or<? extends G, ? extends B>> converter) {
    return (Or<G, B>) converter.apply(source);
  }

  /**
   * Builds an instance of {@link Good} with the given value.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param value the success value
   * @return an instance of {@link Good}
   */
  static <G, B> Or<G, B> good(G value) {
    return Good.of(value);
  }

  /**
   * Builds an instance of {@link Bad} with the given value.
   *
   * @param <G> the {@link Good} type
   * @param <B> the {@link Bad} type
   * @param value the failure value
   * @return an instance of {@link Bad}
   */
  static <G, B> Or<G, B> bad(B value) {
    return Bad.of(value);
  }

  /**
   * Converts this {@link Or} to an {@link Or} with the same {@link Good} type and a {@link Bad}
   * type consisting of {@link One} parameterized by this {@link Or}'s {@link Bad} type.
   *
   * <p>
   * For example, invoking the <code>accumulating</code> method on an <code>Or&lt;Int,ErrorMessage&gt;</code> would convert
   * it to an <code>Or&lt;Int,One&lt;ErrorMessage&gt;&gt;</code>. This result type, because the {@link Bad} type is an
   * {@link Every}, can be used with the mechanisms provided in class {@link Accumulation} to accumulate errors.
   *
   * <p>
   * Note that if this {@link Or} is already an accumulating {@link Or}, the behavior of this
   * <code>accumulating</code> method does not change. For example, if you invoke <code>accumulating</code> on an
   * <code>Or&lt;Int,One&lt;ErrorMessage&gt;&gt;</code> you will be rewarded with an
   * <code>Or&lt;Int,One&lt;One&lt;ErrorMessage&gt;&gt;&gt;</code>.
   *
   * <pre class="stHighlighted"> Scalactic: def accumulating: Or[G, One[B]] </pre>
   *
   * @return this {@link Good}, if this {@link Or} is a {@link Good}; or this {@link Bad} value
   *         wrapped in a {@link One} if this {@link Or} is a {@link Bad}.
   */
  Or<G, One<B>> accumulating();

  /**
   * Maps the given function to this {@link Or}'s value if it is a {@link Good} or returns <code>this</code>
   * if it is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def map[H](f: (G) =&gt; H): Or[H, B] </pre>
   *
   * @param <H> The element type of the new {@link Good}
   * @param mapper the function to apply
   * @return if this is a {@link Good}, the result of applying the given function to the contained value wrapped
   *         in a {@link Good}, else this {@link Bad} is returned
   */
  <H> Or<H, B> map(Function<? super G, ? extends H> mapper);

  /**
   * Maps the given function to this {@link Or}'s value if it is a {@link Bad} or returns <code>this</code>
   * if it is a {@link Good}.
   *
   * <pre class="stHighlighted">Scalactic: def badMap[C](f: (B) =&gt; C): Or[G, C] </pre>
   *
   * @param <C> The element type of the new {@link Bad}
   * @param mapper the function to apply
   * @return if this is a {@link Bad}, the result of applying the given function to the contained value wrapped
   *         in a {@link Bad}, else this {@link Good} is returned
   */
  <C> Or<G, C> badMap(Function<? super B, ? extends C> mapper);

  /**
   * Returns true if this Or is a Good and its value is deeply equals to the given argument
   * according to {@link Objects#deepEquals(Object, Object)}.
   *
   * @param good the value to compare to this Or's value if it is a Good
   * @return true if this Or is a Good and its value is deeply equals to the given argument.
   */
  boolean contains(G good);

  /**
   * Returns true if this Or is a Bad and its value is deeply equals to the given argument
   * according to {@link Objects#deepEquals(Object, Object)}.
   *
   * @param bad the value to compare to this Or's value if it is a Bad
   * @return true if this Or is a Bad and its value is deeply equals to the given argument.
   */
  boolean containsBad(B bad);

  /**
   * Returns <code>true</code> if this {@link Or} is a {@link Good} and the predicate <code>p</code> returns
   * true when applied to this {@link Good}'s value.
   *
   * <p>
   * Note: The <code>exists</code> method will return the same result as {@link #forAll} if this {@link Or}
   * is a {@link Good}, but the opposite result if this {@link Or} is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def exists(p: (G) =&gt; Boolean): Boolean </pre>
   *
   * @param p the predicate to apply to the {@link Good} value, if this is a {@link Good}
   * @return the result of applying the passed predicate <code>p</code> to the {@link Good} value, if this is a
   *         {@link Good}, else <code>false</code>
   */
  boolean exists(Predicate<? super G> p);

  /**
   * Returns this {@link Or} if either 1) it is a {@link Bad} or 2) it is a {@link Good} and applying
   * the validation function <code>validation</code> to this {@link Good}'s value returns {@link Pass}; otherwise,
   * returns a new {@link Bad} containing the error value contained in the {@link Fail} resulting from
   * applying the validation function <code>validation</code> to this {@link Good}'s value.
   *
   * <p>
   * For examples of <code>filter</code> used in <code>for</code> expressions, see the main documentation for interface
   * {@link Validation}.
   *
   * <pre class="stHighlighted">Scalactic: def filter[C &gt;: B](f: (G) =&gt; Validation[C]): Or[G, C] </pre>
   *
   * @param validator the validation function to apply
   * @return a {@link Good} if this {@link Or} is a {@link Good} that passes the validation function,
   *         else a {@link Bad}.
   */
  Or<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator);

  /**
   * Returns the given function applied to the value contained in this {@link Or} if it is a {@link Good},
   * or returns <code>this</code> if it is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def flatMap[H, C &gt;: B](f: (G) =&gt; Or[H, C]): Or[H, C] </pre>
   *
   * @param <H> the element type of the new {@link Good}
   * @param func the function to apply
   * @return if this is a {@link Good}, the result of applying the given function to the contained value wrapped
   *         in a {@link Good}, else this {@link Bad} is returned
   */
  <H> Or<H, B> flatMap(Function<? super G, ? extends Or<? extends H, ? extends B>> func);

  /**
   * Folds this {@link Or} into a value of type <code>V</code> by applying the given <code>gf</code> function if
   * this is a {@link Good} else the given <code>bf</code> function if this is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def fold[V](gf: (G) =&gt; V, bf: (B) =&gt; V): V </pre>
   *
   * @param <V> the type of the fold's result
   * @param gf
   *            the function to apply to this {@link Or}'s {@link Good} value, if it is a {@link Good}
   * @param bf
   *            the function to apply to this {@link Or}'s {@link Bad} value, if it is a {@link Bad}
   * @return the result of applying the appropriate one of the two passed functions, <code>gf</code> or
   *         <code>bf</code>, to this {@link Or}'s value
   */
  <V> V fold(Function<? super G, ? extends V> gf, Function<? super B, ? extends V> bf);

  /**
   * Returns <code>true</code> if either this {@link Or} is a {@link Bad} or if the predicate <code>p</code>
   * returns <code>true</code> when applied to this {@link Good}'s value.
   *
   * <p>
   * Note: This method will return the same result as {@link #exists} if this {@link Or}
   * is a {@link Good}, but the opposite result if this {@link Or} is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def forall(f: (G) =&gt; Boolean): Boolean </pre>
   *
   * @param p
   *            the predicate to apply to the {@link Good} value, if this is a {@link Good}
   * @return the result of applying the passed predicate <code>p</code> to the {@link Good} value, if this is a
   *         {@link Good}, else <code>true</code>
   */
  boolean forAll(Predicate<? super G> p);

  /**
   * Applies the given function <code>action</code> to the contained value if this {@link Or} is a {@link Good}; does nothing
   * if this {@link Or} is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def foreach(f: (G) =&gt; Unit): Unit </pre>
   *
   * @param action the function to apply
   */
  void forEach(Consumer<? super G> action);

  /**
   * Returns the {@link Or}'s value if it is a {@link Good} or throws {@link NoSuchElementException} if
   * it is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def get: G </pre>
   *
   * @return the contained value if this is a {@link Good}
   * @throws NoSuchElementException if this is a {@link Bad}
   */
  G get();

  /**
   * Returns the {@link Or}'s value if it is a {@link Bad} or throws {@link NoSuchElementException} if
   * it is a {@link Good}.
   *
   * @return the contained value if this is a {@link Bad}
   * @throws NoSuchElementException if this is a {@link Good}
   */
  B getBad();

  /**
   * Returns, if this {@link Or} is {@link Good}, this {@link Good}'s value; otherwise returns <code>default</code>.
   *
   * <pre class="stHighlighted">Scalactic: def getOrElse[H &gt;: G](default: =&gt; H): H </pre>
   *
   * @param def the default expression to evaluate if this {@link Or} is a {@link Bad}
   * @return the contained value, if this {@link Or} is a {@link Good}, else given <code>default</code>
   */
  G getOrElse(G def);

  /**
   * Returns, if this {@link Or} is {@link Good}, this {@link Good}'s value; otherwise returns the
   * result of evaluating <code>default</code>.
   *
   * <pre class="stHighlighted">Scalactic: def getOrElse[H &gt;: G](default: =&gt; H): H </pre>
   *
   * @param def the default expression to evaluate if this {@link Or} is a {@link Bad}
   * @return the contained value, if this {@link Or} is a {@link Good}, else the result of evaluating the
   *         given <code>default</code>
   */
  G getOrElse(Function<? super B, ? extends G> def);

  /**
   * Returns this {@link Or} if it is a {@link Good}, otherwise returns the result of evaluating the passed
   * <code>alt</code>.
   *
   * <pre class="stHighlighted">Scalactic: def orElse[H &gt;: G, C &gt;: B](alternative: =&gt; Or[H, C]): Or[H, C] </pre>
   *
   * @param alt the alternative supplier to evaluate if this {@link Or} is a {@link Bad}
   * @return this {@link Or}, if it is a {@link Good}, else the result of evaluating
   *         <code>alt</code>
   */
  Or<G, B> orElse(Supplier<? extends Or<? extends G, ? extends B>> alt);


  /**
   * Returns this {@link Or} if it is a {@link Good}, otherwise returns the passed
   * <code>alt</code>.
   *
   * <pre class="stHighlighted">Scalactic: def orElse[H &gt;: G, C &gt;: B](alternative: =&gt; Or[H, C]): Or[H, C] </pre>
   *
   * @param alt the alternative to return if this {@link Or} is a {@link Bad}
   * @return this {@link Or}, if it is a {@link Good}, else the result of evaluating
   *         <code>alt</code>
   */
  Or<G, B> orElse(Or<? extends G, ? extends B> alt);

  /**
   * Maps the given function to this {@link Or}'s value if it is a {@link Bad}, transforming it into a
   * {@link Good}, or returns <code>this</code> if it is already a {@link Good}.
   *
   * <pre class="stHighlighted">Scalactic: def recover[H &gt;: G](f: (B) =&gt; H): Or[H, B] </pre>
   *
   * @param func the function to apply
   * @return if this is a {@link Bad}, the result of applying the given function to the contained value wrapped
   *         in a {@link Good}, else this {@link Good} is returned
   */
  Or<G, B> recover(Function<? super B, ? extends G> func);

  /**
   * Maps the given function to this {@link Or}'s value if it is a {@link Bad}, returning the result, or
   * returns <code>this</code> if it is already a {@link Good}.
   *
   * <pre class="stHighlighted">Scalactic: def recoverWith[H &gt;: G, C](f: (B) =&gt; Or[H, C]): Or[H, C] </pre>
   *
   * @param <C> the recovered value of the {@link Bad}
   * @param func the function to apply
   * @return if this is a {@link Bad}, the result of applying the given function to the contained value, else
   *         this {@link Good} is returned
   */
  <C> Or<G, C> recoverWith(Function<? super B, ? extends Or<? extends G, ? extends C>> func);

  /**
   * Returns an {@link Or} with the {@link Good} and {@link Bad} types swapped: {@link Bad}
   * becomes {@link Good} and {@link Good} becomes {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def swap: Or[B, G] </pre>
   *
   * @return if this {@link Or} is a {@link Good}, its {@link Good} value wrapped in a {@link Bad};
   *  if this {@link Or} is a {@link Bad}, its {@link Bad} value wrapped in a {@link Good}.
   */
  Or<B, G> swap();

  /**
   * Returns an {@link Optional} that is defined if this is a {@link Good} and not if this is a {@link Bad}.
   * @return an {@link Optional}
   */
  Optional<G> toJavaOptional();

  /**
   * Returns a {@link Option} containing the {@link Good} value, if this {@link Or} is a
   * {@link Good}, else none.
   *
   * <pre class="stHighlighted">Scalactic: def toOption: Option[G] </pre>
   *
   * @return the contained &ldquo;success&rdquo; value wrapped in an {@link Option}.
   */
  Option<G> toOption();

  /**
   * Returns an {@link Either}: a right {@link Either} containing the {@link Good} value, if this is a
   * {@link Good}; a left {@link Either} containing the {@link Bad} value, if this is a {@link Bad}.
   *
   * <p>
   * Note that values effectively <code>switch sides</code> when converting an {@link Or} to an
   * {@link Either}. If the type of the {@link Or} on which you invoke this method is
   * <code>Or&lt;Int,ErrorMessage&gt;</code> for example, the result will be an <code>Either&lt;ErrorMessage,Int&gt;</code>.
   * The reason is that the convention for {@link Either} is that left {@link Either} is used for <code>failure</code>
   * values and right {@link Either} is used for <code>success</code> ones.
   *
   * <pre class="stHighlighted">Scalactic: def toEither: Either[B, G] </pre>
   *
   * @return this {@link Good} value, wrapped in a right {@link Either}, or this {@link Bad} value, wrapped in
   *         a left {@link Either}.
   */
  Either<B, G> toEither();

  /**
   * Returns a {@link Try}: a success containing the {@link Good}
   * value if the given Or is a {@link Good}; a failure containing
   * the {@link Bad} value if it's a {@link Bad}.
   *
   * <p>
   * Note: This is a static method because there is no way in Java to require
   * implicit evidence about the type of Bad.
   *
   * <pre class="stHighlighted">
   * Scalactic: def toTry(implicit ev: &lt;:&lt;[B, Throwable]): Try[G]
   * </pre>
   *
   * @param <G> the success type of the Or
   * @param or an instance of {@link Or}
   * @return this {@link Good} value, wrapped in a success {@link Try}, or this
   *         {@link Bad} value, wrapped in a failure {@link Try}.
   */
  static <G> Try<G> toTry(Or<? extends G, ? extends Throwable> or) {
    if (or.isGood()) return Try.success(or.get());
    else return Try.failure(or.getBad());
  }

  /**
   * Converts this {@link Or} into anything produced by the given converter.
   *
   * @param <T> the type of the converted object
   * @param converter a function to map an {@link Or} into anything.
   * @return an instance of T
   */
  default <T> T toAny(Function<? super Or<? extends G, ? extends B>, T> converter) {
    return converter.apply(this);
  }

  /**
   * Transforms this {@link Or} by applying the function <code>gf</code> to this {@link Or}'s
   * {@link Good} value if it is a {@link Good}, or by applying <code>bf</code> to this {@link Or}'s
   * {@link Bad} value if it is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def transform[H, C](gf: (G) =&gt; Or[H, C], bf: (B) =&gt; Or[H, C]): Or[H, C] </pre>
   *
   * @param <H> the type of the transformed {@link Good}
   * @param <C> the type of the transformed {@link Bad}
   * @param gf the function to apply to this {@link Or}'s {@link Good} value, if it is a {@link Good}
   * @param bf the function to apply to this {@link Or}'s {@link Bad} value, if it is a {@link Bad}
   * @return the result of applying the appropriate one of the two passed functions, <code>gf</code> or
   *         <code>bf</code>, to this {@link Or}'s value
   */
  <H, C> Or<H, C> transform(Function<? super G, ? extends H> gf, Function<? super B, ? extends C> bf);

  /**
   * A terminal operation to handle both success and failure cases.
   * @param gc a function to be executed if this {@link Or} is a {@link Good}
   * @param bc a function to be executed if this {@link Or} is a {@link Bad}
   */
  void forEach(Consumer<? super G> gc, Consumer<? super B> bc);

  /**
   * Indicates whether this {@link Or} is a {@link Good}.
   *
   * <pre class="stHighlighted">Scalactic: def isGood: Boolean </pre>
   *
   * @return true if this {@link Or} is a {@link Good}, <code>false</code> if it is a {@link Bad}.
   */
  boolean isGood();

  /**
   * Indicates whether this {@link Or} is a {@link Bad}.
   *
   * <pre class="stHighlighted">Scalactic: def isBad: Boolean </pre>
   *
   * @return true if this {@link Or} is a {@link Bad}, <code>false</code> if it is a {@link Good}.
   */
  boolean isBad();

}
