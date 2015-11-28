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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javaslang.control.Either;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Success;
import javaslang.control.Try;

/**
 * Contains a "good" value.
 * 
 * You can decide what "good" means, but it is expected Good will be commonly
 * used to hold valid results for processes that may fail with an error instead
 * of producing a valid result.
 * 
 * @author mvh
 *
 * @param <G> the good type of the Or
 * @param <B> the bad type of the Or
 */
public class Good<G,B> implements Or<G,B> {
	
	private final G value;

	Good(G good) {
		value = good;
	}
	
	public static <G,B> Good<G,B> of(G value) {
		return new Good<>(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Or<G, Every<B>> accumulating() {
		return (Or<G, Every<B>>) this;
	}
	@Override
	public <H> Or<H, B> map(Function<? super G, ? extends H> mapper) {
		return Or.good(mapper.apply(value));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C> Or<G, C> badMap(Function<? super B, ? extends C> mapper) {
		return (Or<G, C>) this;
	}

	@Override
	public boolean exists(Predicate<? super G> predicate) {
		return predicate.test(value);
	}

	@Override
	public <H> Or<H, B> flatMap(Function<? super G, Or<H, B>> func) {
		return func.apply(value);
	}

	@Override
	public <V> V fold(Function<? super G, V> good, Function<? super B, V> bad) {
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
	public G getOrElse(Supplier<? extends G> alt) {
		return value;
	}

	@Override
	public Or<G,B> orElse(Supplier<Or<G,B>> alt) {
		return this;
	}

	@Override
	public Or<G, B> recover(Function<B,G> func) {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C> Or<G, C> recoverWith(Function<B, Or<G, C>> func) {
		return (Or<G, C>) this;
	}

	@Override
	public Or<B, G> swap() {
		return Or.bad(value);
	}
	
	@Override
	public Optional<G> toJavaOptional(){
		return Optional.of(value);
	}

	@Override
	public Option<G> toOption() {
		return Option.of(value);
	}
	
	@Override
	public Either<B, G> toEither() {
		return new Right<>(value);
	}

	@Override
	public Try<G> toTry() {
		return new Success<>(value);
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
	public <H, C> Or<H, C> transform(Function<G, H> gf, Function<B, C> bf) {
		return Or.good(gf.apply(value));
	}
	
	@Override
	public void forEach(Consumer<G> gc, Consumer<B> bc) {
		gc.accept(value);
	}

	@Override
	public String toString() {
		return String.format("Good(%s)", value);
	}

	@Override
	public Or<G, B> filter(Function<G, Validation<B>> validator) {
		Validation<B> result = validator.apply(value);
		if(result.isPass()) return this;
		else return Or.bad(result.getError());
	}

}
