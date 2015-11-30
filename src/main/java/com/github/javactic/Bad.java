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
import javaslang.control.Failure;
import javaslang.control.Left;
import javaslang.control.Option;
import javaslang.control.Try;

/**
 * Contains a "bad" value.
 * 
 * You can decide what "bad" means, but it is expected Bad will be commonly used
 * to hold descriptions of an error (or several, accumulated errors). Some
 * examples of possible error descriptions are String error messages, Int error
 * codes, Throwable exceptions, or enums designed to describe errors.
 * 
 * @author mvh
 *
 * @param <G> the good type of the Or
 * @param <B> the bad type of the Or
 */
public class Bad<G,B> implements Or<G,B> {
	
	final B value;
	
	Bad(B bad) {
		value = bad;
	}
	
	/**
	 * Creates a Bad of type B.
	 * 
	 * @param <G> the good type of the Or
     * @param <B> the bad type of the Or
	 * @param value the value of the Bad
	 * @return an instance of Bad
	 */
	public static <G,B> Bad<G,B> of(B value) {
		return new Bad<>(value);
	}
	
    public static <G> Bad<G,String> of(String value, Object... args) {
        return new Bad<>(Helper.parse(value, args));
    }

    /**
     * Helper method to get a {@link One} wrapped in a {@link Bad} directly.
     * Equivalent to <code>Bad.of(One.of(value))</code>
     * 
     * @param <G> the good type of the Or
     * @param <B> the bad type of the Or
     * @param value
     *            the value to put in the One
     * @return a One inside a Bad
     */
    public static <G,B> Bad<G,One<B>> ofOne(B value) {
        return new Bad<>(One.of(value));
    }

    public static <G> Bad<G,One<String>> ofOneString(String value, Object... args) {
        return new Bad<>(One.of(Helper.parse(value, args)));
    }

	@Override
	public Or<G, One<B>> accumulating() {
		return Bad.ofOne(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <H> Or<H, B> map(Function<? super G, ? extends H> mapper) {
		return (Or<H, B>) this;
	}

	@Override
	public <C> Or<G, C> badMap(Function<? super B, ? extends C> mapper) {
		return Or.bad(mapper.apply(value));
	}

	@Override
	public boolean exists(Predicate<? super G> predicate) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <H> Or<H, B> flatMap(Function<? super G, Or<H, B>> func) {
		return (Or<H, B>) this;
	}

	@Override
	public <V> V fold(Function<? super G, V> good, Function<? super B, V> bad) {
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
	public G getOrElse(Supplier<? extends G> alt) {
		return alt.get();
	}

	@Override
	public Or<G,B> orElse(Supplier<Or<G,B>> alt) {
		return alt.get();
	}

	@Override
	public Or<G, B> recover(Function<B,G> func) {
		return Or.good(func.apply(value));
	}

	@Override
	public <C> Or<G, C> recoverWith(Function<B, Or<G, C>> func) {
		return func.apply(value);
	}

	@Override
	public Or<B, G> swap() {
		return Or.good(value);
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
		return new Left<>(value);
	}

	@Override
	public Try<G> toTry() {
	    if(value instanceof Throwable) 
	        return new Failure<>((Throwable) value);
	    else 
	        return new Failure<>(new IllegalArgumentException(value.toString()));
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
	public <H, C> Or<H, C> transform(Function<G, H> gf, Function<B, C> bf) {
		return Or.bad(bf.apply(value));
	}

	@Override
	public void forEach(Consumer<G> gc, Consumer<B> bc) {
		bc.accept(value);
	}

	@Override
	public String toString() {
		return "Bad(" + value + ")";
	}

	@Override
	public Or<G, B> filter(Function<G, Validation<B>> validator) {
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
