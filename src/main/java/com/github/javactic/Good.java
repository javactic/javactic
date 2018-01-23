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
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;

/**
 * Contains a "success" value.
 *
 * You can decide what "success" means, but it is expected Good will be commonly
 * used to hold valid results for processes that may fail with an error instead
 * of producing a valid result.
 *
 * @author mvh
 *
 * @param <G> the success type of the Or
 * @param <B> the failure type of the Or
 */
public final class Good<G, B> implements Or<G, B>, Serializable {

  private static final long serialVersionUID = 1L;

  private final G value;

  Good(G good) {
    value = good;
  }

  public static <G, B> Good<G, B> of(G value) {
    return new Good<>(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Or<G, One<B>> accumulating() {
    // not a Bad, so we can cast the Bad side to anything.
    return (Or<G, One<B>>) this;
  }

  /**
   * Returns this Good with the type widened to Or.
   *
   * <pre class="stHighlighted"> Scalactic: def asOr: Or[G, B] </pre>
   *
   * @return this Good with the type widened to Or
   */
  public Or<G, B> asOr() {
    return this;
  }

  @Override
  public <H> Or<H, B> map(Function<? super G, ? extends H> mapper) {
    return Good.of(mapper.apply(value));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> Or<G, C> badMap(Function<? super B, ? extends C> mapper) {
    return (Or<G, C>) this;
  }

  @Override
  public boolean contains(G good) {
    return Objects.deepEquals(good, value);
  }

  @Override
  public boolean containsBad(B bad) {
    return false;
  }

  @Override
  public boolean exists(Predicate<? super G> predicate) {
    return predicate.test(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <H> Or<H, B> flatMap(Function<? super G, ? extends Or<? extends H, ? extends B>> func) {
    return (Or<H, B>) func.apply(value);
  }

  @Override
  public <V> V fold(Function<? super G, ? extends V> good, Function<? super B, ? extends V> bad) {
    return good.apply(value);
  }

  @Override
  public boolean forAll(Predicate<? super G> predicate) {
    return predicate.test(value);
  }

  @Override
  public void forEach(Consumer<? super G> action) {
    action.accept(value);
  }

  @Override
  public G get() {
    return value;
  }

  @Override
  public B getBad() {
    throw new NoSuchElementException();
  }

  @Override
  public G getOrElse(G alt) {
    return value;
  }

  @Override
  public G getOrElse(Function<? super B, ? extends G> def) {
    return value;
  }

  @Override
  public Or<G, B> orElse(Supplier<? extends Or<? extends G, ? extends B>> alt) {
    return this;
  }

  @Override
  public Or<G, B> orElse(Or<? extends G, ? extends B> alt) {
    return this;
  }

  @Override
  public Or<G, B> recover(Function<? super B, ? extends G> func) {
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> Or<G, C> recoverWith(Function<? super B, ? extends Or<? extends G, ? extends C>> func) {
    return (Or<G, C>) this;
  }

  @Override
  public <H> Or<Tuple2<G,H>, Every<B>> zip(Or<? extends H, ? extends B> that) {
    return zipWith(that, Tuple::of);
  }

  @Override
  public <H,X> Or<X, Every<B>> zipWith(Or<? extends H, ? extends B> that, BiFunction<? super G, ? super H, ? extends X> f){
    return that
        .map(h -> (X)f.apply(value, h))
        .recoverWith(b -> Bad.of(Every.of(b)));
  }

  @Override
  public Or<B, G> swap() {
    return Bad.of(value);
  }

  @Override
  public Optional<G> toJavaOptional() {
    return Optional.of(value);
  }

  @Override
  public Option<G> toOption() {
    return Option.of(value);
  }

  @Override
  public Either<B, G> toEither() {
    return Either.right(value);
  }

  @Override
  public boolean isGood() {
    return true;
  }

  @Override
  public boolean isBad() {
    return false;
  }

  @Override
  public <H, C> Or<H, C> transform(Function<? super G, ? extends H> gf, Function<? super B, ? extends C> bf) {
    return Good.of(gf.apply(value));
  }

  @Override
  public void forEach(Consumer<? super G> gc, Consumer<? super B> bc) {
    gc.accept(value);
  }

  @Override
  public String toString() {
    return "Good(" + value + ")";
  }

  @Override
  public Or<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
    Validation<? extends B> result = validator.apply(value);
    if (result.isPass()) return this;
    else return Bad.of(result.getError());
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
    Good other = (Good) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
