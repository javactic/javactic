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

import javaslang.control.Either;
import javaslang.control.Option;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains a "failure" value.
 *
 * You can decide what "failure" means, but it is expected Bad will be commonly used
 * to hold descriptions of an error (or several, accumulated errors). Some
 * examples of possible error descriptions are String error messages, Int error
 * codes, Throwable exceptions, or enums designed to describe errors.
 *
 * @author mvh
 *
 * @param <G> the success type of the Or
 * @param <B> the failure type of the Or
 */
public final class Bad<G, B> implements Or<G, B>, Serializable {

  private static final long serialVersionUID = 1L;

  final B value;

  Bad(B bad) {
    value = bad;
  }

  /**
   * Creates a Bad of type B.
   *
   * @param <G> the success type of the Or
   * @param <B> the failure type of the Or
   * @param value the value of the Bad
   * @return an instance of Bad
   */
  public static <G, B> Bad<G, B> of(B value) {
    return new Bad<>(value);
  }

  /**
   * Creates a Bad with a failure type of String based on the given message. The
   * message can be a slf4j string with {} placeholders that will be replaced
   * by the given optional arguments.
   *
   * @param <G>
   *            the success type of the Or
   * @param msg the message string with possible placeholders
   * @param args the values to replace the placeholders with
   * @return a Bad
   */
  public static <G> Bad<G, String> ofString(String msg, Object... args) {
    return new Bad<>(Helper.parse(msg, args));
  }

  /**
   * Helper method to get an {@link Every} wrapped in a {@link Bad} directly.
   * Equivalent to <code>Bad.of(One.of(value))</code>
   *
   * @param <G> the success type of the Or
   * @param <B> the failure type of the Or
   * @param value
   *            the value to put in the One
   * @return a One inside a Bad
   */
  public static <G, B> Bad<G, Every<B>> ofOne(B value) {
    return new Bad<>(Every.of(value));
  }

  /**
   * Creates a Bad with a failure type of One&lt;String&gt; based on the given
   * message. The message can be a slf4j string with {} placeholders that will
   * be replaced by the given optional arguments.
   *
   * @param <G>
   *            the success type of the Or
   * @param msg
   *            the message string with possible placeholders
   * @param args
   *            the values to replace the placeholders with
   * @return a Bad
   */
  public static <G> Bad<G, Every<String>> ofOneString(String msg, Object... args) {
    return new Bad<>(Every.of(Helper.parse(msg, args)));
  }

  @Override
  public Or<G, Every<B>> accumulating() {
    return Bad.ofOne(value);
  }

  /**
   * Returns this Bad with the type widened to Or.
   *
   * <pre class="stHighlighted"> Scalactic: def asOr: Or[G, B] </pre>
   *
   * @return this Bad with the type widened to Or
   */
  public Or<G, B> asOr() {
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <H> Or<H, B> map(Function<? super G, ? extends H> mapper) {
    return (Or<H, B>) this;
  }

  @Override
  public <C> Or<G, C> badMap(Function<? super B, ? extends C> mapper) {
    return Bad.of(mapper.apply(value));
  }

  @Override
  public boolean contains(G good) {
    return false;
  }

  @Override
  public boolean containsBad(B bad) {
    return Objects.deepEquals(bad, value);
  }

  @Override
  public boolean exists(Predicate<? super G> predicate) {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <H> Or<H, B> flatMap(Function<? super G, ? extends Or<? extends H, ? extends B>> func) {
    return (Or<H, B>) this;
  }

  @Override
  public <V> V fold(Function<? super G, ? extends V> good, Function<? super B, ? extends V> bad) {
    return bad.apply(value);
  }

  @Override
  public boolean forAll(Predicate<? super G> predicate) {
    return true;
  }

  @Override
  public void forEach(Consumer<? super G> action) {
    // does nothing
  }

  @Override
  public G get() {
    throw new NoSuchElementException();
  }

  @Override
  public B getBad() {
    return value;
  }

  @Override
  public G getOrElse(G alt) {
    return alt;
  }

  @Override
  public G getOrElse(Function<? super B, ? extends G> alt) {
    return alt.apply(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Or<G, B> orElse(Supplier<? extends Or<? extends G, ? extends B>> alt) {
    return (Or<G, B>) alt.get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Or<G, B> orElse(Or<? extends G, ? extends B> alt) {
    return (Or<G, B>) alt;
  }

  @Override
  public Or<G, B> recover(Function<? super B, ? extends G> func) {
    return Good.of(func.apply(value));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> Or<G, C> recoverWith(Function<? super B, ? extends Or<? extends G, ? extends C>> func) {
    return (Or<G, C>) func.apply(value);
  }

  @Override
  public Or<B, G> swap() {
    return Good.of(value);
  }

  @Override
  public Optional<G> toJavaOptional() {
    return Optional.empty();
  }

  @Override
  public Option<G> toOption() {
    return Option.none();
  }

  @Override
  public Either<B, G> toEither() {
    return Either.left(value);
  }

  @Override
  public boolean isGood() {
    return false;
  }

  @Override
  public boolean isBad() {
    return true;
  }

  @Override
  public <H, C> Or<H, C> transform(Function<? super G, ? extends H> gf, Function<? super B, ? extends C> bf) {
    return Bad.of(bf.apply(value));
  }

  @Override
  public void forEach(Consumer<? super G> gc, Consumer<? super B> bc) {
    bc.accept(value);
  }

  @Override
  public String toString() {
    return "Bad(" + value + ")";
  }

  @Override
  public Or<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Bad other = (Bad) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }


}
