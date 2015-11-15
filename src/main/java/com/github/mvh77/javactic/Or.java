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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javaslang.control.Either;
import javaslang.control.Failure;
import javaslang.control.Left;
import javaslang.control.None;
import javaslang.control.Option;
import javaslang.control.Right;
import javaslang.control.Some;
import javaslang.control.Success;
import javaslang.control.Try;

public interface Or<G, B> {

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
    public static <G, B> Or<G, B> fromJavaOptional(Optional<G> optional, B bad) {
        return optional.<Or<G, B>> map(Or::good).orElse(Or.bad(bad));
    }

    /**
     * Builds an {@link Or} from the given <span class="jCode">source</span> using the provided <span class="jCode">converter</span> function.
     *
     * @param <S> the source type
     * @param <G> the {@link Good} type 
     * @param <B> the {@link Bad} type
     * @param source the object to convert to an {@link Or}
     * @param converter the converter to use to make an {@link Good} from <span class="jCode">source</span>
     * @return an instance of {@link Or}
     */
    public static <S, G, B> Or<G, B> fromAny(S source, Function<? super S, Or<G, B>> converter) {
        return converter.apply(source);
    }

    /**
     * Builds an instance of {@link Good} with the given value.
     * 
     * @param <G> the {@link Good} type 
     * @param <B> the {@link Bad} type
     * @param value the good value
     * @return an instance of {@link Good}
     */
    public static <G, B> Good<G, B> good(G value) {
        return new Good<G, B>(value);
    }

    /**
     * Builds an instance of {@link Bad} with the given value.
     * 
     * @param <G> the {@link Good} type 
     * @param <B> the {@link Bad} type
     * @param value the bad value
     * @return an instance of {@link Bad}
     */
    public static <G, B> Bad<G, B> bad(B value) {
        return new Bad<G, B>(value);
    }

    /**
     * Converts this {@link Or} to an {@link Or} with the same {@link Good} type and a {@link Bad}
     * type consisting of {@link One} parameterized by this {@link Or}'s {@link Bad} type.
     *
     * <p>
     * For example, invoking the <span class="jCode">accumulating</span> method on an <span class="jCode">Or&lt;Int,ErrorMessage&gt;</span> would convert
     * it to an <span class="jCode">Or&lt;Int,One&lt;ErrorMessage&gt;&gt;</span>. This result type, because the {@link Bad} type is an
     * {@link Every}, can be used with the mechanisms provided in class {@link Accumulation} to accumulate errors.
     * 
     * <p>
     * Note that if this {@link Or} is already an accumulating {@link Or}, the behavior of this
     * <span class="jCode">accumulating</span> method does not change. For example, if you invoke <span class="jCode">accumulating</span> on an
     * <span class="jCode">Or&lt;Int,One&lt;ErrorMessage&gt;&gt;</span> you will be rewarded with an 
     * <span class="jCode">Or&lt;Int,One&lt;One&lt;ErrorMessage&gt;&gt;&gt;</span>.
     * <p>
     *
     * <pre class="stHighlighted"> Scalactic: def accumulating: Or[G, One[B]] </pre>
     * 
     * @return this {@link Good}, if this {@link Or} is a {@link Good}; or this {@link Bad} value
     *         wrapped in a {@link One} if this {@link Or} is a {@link Bad}.
     */
    Or<G, Every<B>> accumulating();

    /**
     * Maps the given function to this {@link Or}'s value if it is a {@link Good} or returns <span class="jCode">this</span>
     * if it is a {@link Bad}.
     * <p>
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
     * Maps the given function to this {@link Or}'s value if it is a {@link Bad} or returns <span class="jCode">this</span>
     * if it is a {@link Good}.
     * <p>
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
     * Returns <span class="jCode">true</span> if this {@link Or} is a {@link Good} and the predicate <span class="jCode">p</span> returns
     * true when applied to this {@link Good}'s value.
     *
     * <p>
     * Note: The <span class="jCode">exists</span> method will return the same result as {@link #forAll} if this {@link Or}
     * is a {@link Good}, but the opposite result if this {@link Or} is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def exists(p: (G) =&gt; Boolean): Boolean </pre>
     *
     * @param p the predicate to apply to the {@link Good} value, if this is a {@link Good}
     * @return the result of applying the passed predicate <span class="jCode">p</span> to the {@link Good} value, if this is a
     *         {@link Good}, else <span class="jCode">false</span>
     */
    boolean exists(Predicate<? super G> p);

    /**
     * Returns this {@link Or} if either 1) it is a {@link Bad} or 2) it is a {@link Good} and applying
     * the validation function <span class="jCode">validation</span> to this {@link Good}'s value returns {@link Pass}; otherwise,
     * returns a new {@link Bad} containing the error value contained in the {@link Fail} resulting from
     * applying the validation function <span class="jCode">validation</span> to this {@link Good}'s value.
     *
     * <p>
     * For examples of <span class="jCode">filter</span> used in <span class="jCode">for</span> expressions, see the main documentation for interface
     * {@link Validation}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def filter[C &gt;: B](f: (G) =&gt; Validation[C]): Or[G, C] </pre>
     *
     * @param validator the validation function to apply
     * @return a {@link Good} if this {@link Or} is a {@link Good} that passes the validation function,
     *         else a {@link Bad}.
     */
    Or<G, B> filter(Function<G, Validation<B>> validator);

    /**
     * Returns the given function applied to the value contained in this {@link Or} if it is a {@link Good},
     * or returns <span class="jCode">this</span> if it is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def flatMap[H, C &gt;: B](f: (G) =&gt; Or[H, C]): Or[H, C] </pre>
     *
     * @param <H> the element type of the new {@link Good}
     * @param func the function to apply
     * @return if this is a {@link Good}, the result of applying the given function to the contained value wrapped
     *         in a {@link Good}, else this {@link Bad} is returned
     */
    <H> Or<H, B> flatMap(Function<? super G, Or<H, B>> func);

    /**
     * Folds this {@link Or} into a value of type <span class="jCode">V</span> by applying the given <span class="jCode">gf</span> function if
     * this is a {@link Good} else the given <span class="jCode">bf</span> function if this is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def fold[V](gf: (G) =&gt; V, bf: (B) =&gt; V): V </pre>
     *
     * @param <V> the type of the fold's result
     * @param gf
     *            the function to apply to this {@link Or}'s {@link Good} value, if it is a {@link Good}
     * @param bf
     *            the function to apply to this {@link Or}'s {@link Bad} value, if it is a {@link Bad}
     * @return the result of applying the appropriate one of the two passed functions, <span class="jCode">gf</span> or
     *         <span class="jCode">bf</span>, to this {@link Or}'s value
     */
    <V> V fold(Function<? super G, V> gf, Function<? super B, V> bf);

    /**
     * Returns <span class="jCode">true</span> if either this {@link Or} is a {@link Bad} or if the predicate <span class="jCode">p</span>
     * returns <span class="jCode">true</span> when applied to this {@link Good}'s value.
     *
     * <p>
     * Note: The {@link #forAll} method will return the same result as {@link #exists} if this {@link Or}
     * is a {@link Good}, but the opposite result if this {@link Or} is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def forall(f: (G) =&gt; Boolean): Boolean </pre>
     *
     * @param p
     *            the predicate to apply to the {@link Good} value, if this is a {@link Good}
     * @return the result of applying the passed predicate <span class="jCode">p</span> to the {@link Good} value, if this is a
     *         {@link Good}, else <span class="jCode">true</span>
     */
    boolean forAll(Predicate<? super G> p);

    /**
     * Applies the given function <span class="jCode">action</span> to the contained value if this {@link Or} is a {@link Good}; does nothing
     * if this {@link Or} is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def foreach(f: (G) =&gt; Unit): Unit </pre>
     *
     * @param action the function to apply
     */
    void forEach(Consumer<? super G> action);

    /**
     * Returns the {@link Or}'s value if it is a {@link Good} or throws {@link NoSuchElementException} if
     * it is a {@link Bad}.
     * <p>
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
     * Returns, if this {@link Or} is {@link Good}, this {@link Good}'s value; otherwise returns <span class="jCode">default</span>.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def getOrElse[H &gt;: G](default: =&gt; H): H </pre>
     *
     * @param def the default expression to evaluate if this {@link Or} is a {@link Bad}
     * @return the contained value, if this {@link Or} is a {@link Good}, else given <span class="jCode">default</span>
     */
    G getOrElse(G def);

    /**
     * Returns, if this {@link Or} is {@link Good}, this {@link Good}'s value; otherwise returns the
     * result of evaluating <span class="jCode">default</span>.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def getOrElse[H &gt;: G](default: =&gt; H): H </pre>
     *
     * @param def the default expression to evaluate if this {@link Or} is a {@link Bad}
     * @return the contained value, if this {@link Or} is a {@link Good}, else the result of evaluating the
     *         given <span class="jCode">default</span>
     */
    G getOrElse(Supplier<? extends G> def);

    /**
     * Returns this {@link Or} if it is a {@link Good}, otherwise returns the result of evaluating the passed
     * <span class="jCode">alt</span>.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def orElse[H &gt;: G, C &gt;: B](alternative: =&gt; Or[H, C]): Or[H, C] </pre>
     *
     * @param alt the alternative by-name to evaluate if this {@link Or} is a {@link Bad}
     * @return this {@link Or}, if it is a {@link Good}, else the result of evaluating
     *         <span class="jCode">alt</span>
     */
    Or<G, B> orElse(Supplier<Or<G, B>> alt);

    /**
     * Maps the given function to this {@link Or}'s value if it is a {@link Bad}, transforming it into a
     * {@link Good}, or returns <span class="jCode">this</span> if it is already a {@link Good}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def recover[H &gt;: G](f: (B) =&gt; H): Or[H, B] </pre>
     *
     * @param func the function to apply
     * @return if this is a {@link Bad}, the result of applying the given function to the contained value wrapped
     *         in a {@link Good}, else this {@link Good} is returned
     */
    Or<G, B> recover(Function<B, G> func);

    /**
     * Maps the given function to this {@link Or}'s value if it is a {@link Bad}, returning the result, or
     * returns <span class="jCode">this</span> if it is already a {@link Good}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def recoverWith[H &gt;: G, C](f: (B) =&gt; Or[H, C]): Or[H, C] </pre>
     *
     * @param <C> the recovered value of the {@link Bad}
     * @param func the function to apply
     * @return if this is a {@link Bad}, the result of applying the given function to the contained value, else
     *         this {@link Good} is returned
     */
    <C> Or<G, C> recoverWith(Function<B, Or<G, C>> func);

    /**
     * Returns an {@link Or} with the {@link Good} and {@link Bad} types swapped: {@link Bad}
     * becomes {@link Good} and {@link Good} becomes {@link Bad}.
     * <p>
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
     * Returns a {@link Some} containing the {@link Good} value, if this {@link Or} is a
     * {@link Good}, else {@link None}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def toOption: Option[G] </pre>
     *
     * @return the contained &ldquo;good&rdquo; value wrapped in a {@link Some}, if this {@link Or} is a
     *         {@link Good}; {@link None} if this {@link Or} is a {@link Bad}.
     */
    Option<G> toOption();

    /**
     * Returns an {@link Either}: a {@link Right} containing the {@link Good} value, if this is a
     * {@link Good}; a {@link Left} containing the {@link Bad} value, if this is a {@link Bad}.
     *
     * <p>
     * Note that values effectively <span class="jCode">switch sides</span> when converting an {@link Or} to an
     * {@link Either}. If the type of the {@link Or} on which you invoke {@link #toEither()} is
     * <span class="jCode">Or&lt;Int,ErrorMessage&gt;</span> for example, the result will be an <span class="jCode">Either&lt;ErrorMessage,Int&gt;</span>. 
     * The reason is that the convention for {@link Either} is that {@link Left} is used for <span class="jCode">bad</span>
     * values and {@link Right} is used for <span class="jCode">good</span> ones.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def toEither: Either[B, G] </pre>
     *
     * @return this {@link Good} value, wrapped in a {@link Right}, or this {@link Bad} value, wrapped in
     *         a {@link Left}.
     */
    Either<B, G> toEither();

    /**
     * Returns a {@link Try}: a {@link Success} containing the {@link Good} value, if this is a
     * {@link Good}; a {@link Failure} containing the {@link Bad} value, if this is a {@link Bad}.
     *
     * <p>
     * Note: If the {@link Bad} type of this {@link Or} is not a subclass of {@link Throwable} 
     * (or {@link Throwable} itself) the cause of the {@link Failure} will be a {@link IllegalArgumentException}
     * containing as message the string value of the {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def toTry(implicit ev: &lt;:&lt;[B, Throwable]): Try[G] </pre>
     *
     * @return this {@link Good} value, wrapped in a {@link Success}, or this {@link Bad} value, wrapped in
     *         a {@link Failure}.
     */
    Try<G> toTry();

    /**
     * Converts this {@link Or} into anything produced by the given converter.
     * 
     * @param <T> the type of the converted object
     * @param converter a function to map an {@link Or} into anything.
     * @return an instance of T
     */
    default <T> T toAny(Function<Or<G, B>, T> converter) {
        return converter.apply(this);
    }

    /**
     * Transforms this {@link Or} by applying the function <span class="jCode">gf</span> to this {@link Or}'s
     * {@link Good} value if it is a {@link Good}, or by applying <span class="jCode">bf</span> to this {@link Or}'s
     * {@link Bad} value if it is a {@link Bad}.
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def transform[H, C](gf: (G) =&gt; Or[H, C], bf: (B) =&gt; Or[H, C]): Or[H, C] </pre>
     *
     * @param <H> the type of the transformed {@link Good}
     * @param <C> the type of the transformed {@link Bad} 
     * @param gf the function to apply to this {@link Or}'s {@link Good} value, if it is a {@link Good}
     * @param bf the function to apply to this {@link Or}'s {@link Bad} value, if it is a {@link Bad}
     * @return the result of applying the appropriate one of the two passed functions, <span class="jCode">gf</span> or
     *         <span class="jCode">bf</span>, to this {@link Or}'s value
     */
    <H, C> Or<H, C> transform(Function<G, H> gf, Function<B, C> bf);

    /**
     * A terminal operation to handle both good and bad cases.
     * @param gc a function to be executed if this is {@link Or} is a {@link Good}
     * @param bc a function to be executed if this is {@link Or} is a {@link Bad}
     */
    void forEach(Consumer<G> gc, Consumer<B> bc);

    /**
     * Indicates whether this {@link Or} is a {@link Good}
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def isGood: Boolean </pre>
     *
     * @return true if this {@link Or} is a {@link Good}, <span class="jCode">false</span> if it is a {@link Bad}.
     */
    boolean isGood();

    /**
     * Indicates whether this {@link Or} is a {@link Bad}
     * <p>
     * 
     * <pre class="stHighlighted">Scalactic: def isBad: Boolean </pre>
     *
     * @return true if this {@link Or} is a {@link Bad}, <span class="jCode">false</span> if it is a {@link Good}.
     */
    boolean isBad();

}
