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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javaslang.Function3;
import javaslang.Function4;
import javaslang.Function5;
import javaslang.Function6;
import javaslang.Function7;
import javaslang.Function8;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.Tuple5;
import javaslang.Tuple6;
import javaslang.Tuple7;
import javaslang.Tuple8;
import javaslang.collection.Vector;

/**
 * Provides mechanisms that enable errors to be accumulated in "accumulating
 * Ors," {@link Or}s whose Bad type is an {@link Every}.
 * 
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
 *
 */
public final class Accumulation {
	
	private Accumulation() {}

    /**
     * Combines an Iterable of Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt; (where
     * EVERY is some subtype of Every) into a single Or of type
     * Or&lt;COLL&lt;G&gt;, Every&lt;ERR&gt;&gt; using a Collector to determine
     * the wanted collection type COLL.
     * 
     * @param <G> the good type of the resulting Or
     * @param <A> the mutable accumulation type of the reduction operation of the collector
     * @param <ERR> the bad type of the resulting Or
     * @param <I> the resulting good iterable type
     * @param iterable the iterable containing Ors to combine
     * @param collector the collector producing the resulting collection
     * @return an Or of all the good values or of all the errors 
     */
	public static <G,A,ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>> 
		combined(Iterable<? extends Or<G, ? extends Every<ERR>>> iterable,
				 Collector<? super G, A, I> collector) {
		A goods = collector.supplier().get();
		Vector<ERR> errs = Vector.empty();
		for(Or<G, ? extends Every<ERR>> or: iterable) {
			if(or.isGood())
				collector.accumulator().accept(goods, or.get());
			else
				errs = errs.appendAll(or.getBad().toVector());
		}
		I gds = collector.finisher().apply(goods);
		if(errs.isEmpty())
			return Good.of(gds);
		else
			return Bad.of(Every.of(errs.head(), errs.tail()));
	}
	
    /**
     * Maps a iterable of Fs into Ors of type Or&lt;G, EVERY&lt;ERR&gt;&gt;
     * (where EVERY is some subtype of Every) using the passed function f, then
     * combines the resulting Ors into a single Or of type Or&lt;COLL&lt;G&gt;,
     * Every&lt;ERR&gt;&gt; using a Collector to determine the wanted collection
     * type COLL.
     * <p>
     * Note: this process implemented by this method is sometimes called a
     * &quot;traverse&quot;.
     * </p>
     * 
     * @param <F> the type of the original iterable to validate
     * @param <G> the Good type of the resulting Or
     * @param <A> the mutable accumulation type of the reduction operation of the collector
     * @param <I> the result type of the reduction operation
     * @param <ERR>  the Bad type of the resulting Or
     * @param iterable the iterable to validate
     * @param f the validation function
     * @param collector the collector producing the resulting collection
     * @return an Or of all the good values or of all the errors
     */
	public static <F, G, A, I extends Iterable<? extends G>, ERR> 
	    Or<I, Every<ERR>> 
		validatedBy(Iterable<? extends F> iterable, 
					Function<? super F, ? extends Or<G, ? extends Every<ERR>>> f, 
					Collector<? super G, A, I> collector) {
			A goods = collector.supplier().get();
			Vector<ERR> errs = Vector.empty();
			for(F g : iterable) {
				Or<G, ? extends Every<ERR>> or = f.apply(g);
				if(or.isGood()) 
					collector.accumulator().accept(goods, or.get());
				else
					errs = errs.appendAll(or.getBad().toVector());
			}
			I gds = collector.finisher().apply(goods);
			if(errs.isEmpty()) 
				return Or.good(gds);
			else
				return Or.bad(Every.of(errs.head(), errs.tail()));
	}

	
	/**
	 * Enables further validation on an existing accumulating Or by passing validation functions.
	 * 
	 * @param <G> the Good type of the argument Or
	 * @param <ERR> the type of the error message contained in the accumulating bad
	 * @param or the accumulating or
	 * @param validations the validation functions
	 * @return the original or if it passed all validations or a Bad with all failures
	 */
	@SuppressWarnings("unchecked")
    @SafeVarargs
	public static <G, ERR> Or<G, Every<ERR>> 
		when(Or<G, ? extends Every<ERR>> or, Function<? super G, ? extends Validation<ERR>>... validations) {
			if(or.isGood()) {
				Vector<ERR>  result = Stream.of(validations).flatMap(f -> {
					Validation<ERR> v = f.apply(or.get());
					if(v.isPass()) return Stream.empty();
					else return Stream.of(v.getError());
				}).collect(Vector.collector());
				if(result.length() == 0) return Or.good(or.get());
				else return Or.bad(Every.of(result.head(), result.tail()));
			} 
			else return (Or<G, Every<ERR>>) or;
	}

	// ------------------------------------------------------------------------
	// ZIP
	// ------------------------------------------------------------------------
	
	public static <A,B,ERR> Or<Tuple2<A,B>, Every<ERR>> zip(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b) {
		return withGood(a, b, Tuple::of);
	}
	
	public static <A,B,C,ERR> Or<Tuple3<A,B,C>, Every<ERR>> zip3(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c) {
		return withGood(a, b, c, Tuple::of);
	}
	
	public static <A,B,C,D,ERR> Or<Tuple4<A,B,C,D>, Every<ERR>> zip4(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d) {
		return withGood(a, b, c, d, Tuple::of);
	}
	
	public static <A,B,C,D,E,ERR> Or<Tuple5<A,B,C,D,E>, Every<ERR>> zip5(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e) {
		return withGood(a, b, c, d, e, Tuple::of);
	}
	
	public static <A,B,C,D,E,F,ERR> Or<Tuple6<A,B,C,D,E,F>, Every<ERR>> zip6(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f) {
		return withGood(a, b, c, d, e, f, Tuple::of);
	}
	
	public static <A,B,C,D,E,F,G,ERR> Or<Tuple7<A,B,C,D,E,F,G>, Every<ERR>> zip7(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f,
			Or<G, ? extends Every<ERR>> g) {
		return withGood(a, b, c, d, e, f, g, Tuple::of);
	}
	
	public static <A,B,C,D,E,F,G,H,ERR> Or<Tuple8<A,B,C,D,E,F,G,H>, Every<ERR>> zip8(
			Or<A, ? extends Every<ERR>> a,
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f,
			Or<G, ? extends Every<ERR>> g,
			Or<H, ? extends Every<ERR>> h) {
		return withGood(a, b, c, d, e, f, g, h, Tuple::of);
	}

	// ------------------------------------------------------------------------
	// WITHGOOD
	// ------------------------------------------------------------------------
	
	public static <A, B, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			BiFunction<A, B, RESULT> function) {
		if(allGood(a,b))
			return Or.good(function.apply(a.get(), b.get()));
		else {
			Vector<ERR> bads = getBads(a, b);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c, 
			Function3<A, B, C, RESULT> function) {
		if(allGood(a,b,c))
			return Or.good(function.apply(a.get(), b.get(), c.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, D, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Function4<A, B, C, D, RESULT> function) {
		if(allGood(a,b,c,d))
			return Or.good(function.apply(a.get(), b.get(), c.get(), d.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c, d);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, D, E, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Function5<A, B, C, D, E, RESULT> function) {
		if(allGood(a,b,c,d,e))
			return Or.good(function.apply(a.get(), b.get(), c.get(), d.get(), e.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c, d, e);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, D, E, F, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f,
			Function6<A, B, C, D, E, F, RESULT> function) {
		if(allGood(a,b,c,d,e,f))
			return Or.good(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c, d, e, f);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, D, E, F, G, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f,
			Or<G, ? extends Every<ERR>> g,
			Function7<A, B, C, D, E, F, G, RESULT> function) {
		if(allGood(a,b,c,d,e,f,g))
			return Or.good(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get(), g.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c, d, e, f, g);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	public static <A, B, C, D, E, F, G, H, ERR, RESULT> Or<RESULT, Every<ERR>> withGood(
			Or<A, ? extends Every<ERR>> a, 
			Or<B, ? extends Every<ERR>> b,
			Or<C, ? extends Every<ERR>> c,
			Or<D, ? extends Every<ERR>> d,
			Or<E, ? extends Every<ERR>> e,
			Or<F, ? extends Every<ERR>> f,
			Or<G, ? extends Every<ERR>> g,
			Or<H, ? extends Every<ERR>> h,
			Function8<A, B, C, D, E, F, G, H, RESULT> function) {
		if(allGood(a,b,c,d,e,f,g,h))
			return Or.good(function.apply(a.get(), b.get(), c.get(), d.get(), e.get(), f.get(), g.get(), h.get()));
		else {
			Vector<ERR> bads = getBads(a, b, c, d, e, f, g, h);
			return Or.bad(Every.of(bads.head(), bads.tail()));
		}
	}

	// ------------------------------------------------------------------------

	private static boolean allGood(Or<?,?>... ors) {
		return Stream.of(ors).allMatch(or -> or.isGood());
	}
	
	@SafeVarargs
	private static <ERR> Vector<ERR> getBads(Or<?, ? extends Every<ERR>>... ors) {
		Vector<ERR> errs = Vector.empty();
		for(Or<?, ? extends Every<ERR>> or: ors) {
			if(or.isBad()) errs = errs.appendAll(or.getBad().toVector());
		}
		return errs;
	}
}
