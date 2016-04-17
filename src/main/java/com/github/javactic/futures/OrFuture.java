package com.github.javactic.futures;
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

import com.github.javactic.Accumulation;
import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Validation;
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
import javaslang.collection.Vector;
import javaslang.concurrent.Future;
import javaslang.concurrent.Promise;
import javaslang.control.Option;
import javaslang.control.Try;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.github.javactic.futures.Helper.*;

/**
 * <p>
 * The interface for disjunction futures. OrFutures do not succeed or fail, they
 * complete with an instance of Or.
 * </p>
 * <p>
 * OrFutures created with the factory methods on this interface will not handle
 * exceptions thrown by the provided suppliers. Depending on the {@link Executor}
 * used these exceptions might be handled by the {@link java.lang.Thread.UncaughtExceptionHandler}
 * or printed to the standard error output by the {@link ThreadGroup#uncaughtException(Thread, Throwable)}
 * method. To avoid problems with uncaught exceptions, use a {@link FutureFactory} to create
 * OrFuture instances that will transform uncaught exceptions into instances of {@link Bad}.
 * </p>
 *
 * @param <G> the success type
 * @param <B> the failure type
 */
public interface OrFuture<G, B> {

  // ----------------------------------------------------------------------------------------------
  // FACTORY METHODS
  // ----------------------------------------------------------------------------------------------

  /**
   * Creates an OrFuture that will execute the supplied task with the given executor. Handling of
   * uncaught exceptions will be specific to the provided {@link Executor}.
   *
   * @param executor the executor to run the given supplier with
   * @param task     asynchronous computation to execute
   * @param <G>      the success type
   * @param <B>      the failure type
   * @return an instance of OrFuture
   * @see FutureFactory
   */
  static <G, B> OrFuture<G, B> of(Executor executor,
                                  Supplier<? extends Or<? extends G, ? extends B>> task) {
    final OrFutureImpl<G, B> future = new OrFutureImpl<>(executor);
    future.run(task);
    return future;
  }

  /**
   * Creates an OrFuture that will execute the supplied task with the common
   * {@link Executors#newCachedThreadPool()}. Handling of uncaught exceptions
   * will be specific to this pool.
   *
   * @param task asynchronous computation to execute
   * @param <G>  the success type
   * @param <B>  the failure type
   * @return an instance of OrFuture
   * @see FutureFactory
   **/
  static <G, B> OrFuture<G, B> of(Supplier<? extends Or<? extends G, ? extends B>> task) {
    return of(DEFAULT_EXECUTOR, task);
  }

  /**
   * Creates an OrFuture that is already completed with a {@link Bad}.
   *
   * @param bad the failure value
   * @param <G> the success type
   * @param <B> the failure type
   * @return an instance of OrFuture
   */
  static <G, B> OrFuture<G, B> ofBad(B bad) {
    return OrPromise.<G, B>create().failure(bad).future();
  }

  /**
   * Creates an OrFuture that is already completed with an accumulating {@link Bad}.
   *
   * @param bad the failure value
   * @param <G> the success type
   * @param <B> the failure type
   * @return an instance of accumulating OrFuture
   */
  static <G, B> OrFuture<G, Every<B>> ofOneBad(B bad) {
    return OrPromise.<G, Every<B>>create().failure(Every.of(bad)).future();
  }

  /**
   * Creates an OrFuture that is already completed with a {@link Good}.
   *
   * @param good the success value
   * @param <G>  the success type
   * @param <B>  the failure type
   * @return an instance of OrFuture
   */
  static <G, B> OrFuture<G, B> ofGood(G good) {
    return OrPromise.<G, B>create().success(good).future();
  }

  // ----------------------------------------------------------------------------------------------
  // INSTANCE METHODS
  // ----------------------------------------------------------------------------------------------

  /**
   * @return true if this future is completed
   */
  boolean isCompleted();

  /**
   * Executes the given callback when this future completes. If this future is already completed,
   * the action is called immediately.
   *
   * @param action the callback to execute when the future finishes.
   */
  void onComplete(Consumer<? super Or<G, B>> action);

  /**
   * Returns a some {@link Option} with an Or representing the value of this future if
   * the future is completed, none {@link Option} otherwise.
   *
   * @return an {@link Option} representing this future's value at this point.
   */
  Option<Or<G, B>> getOption();

  /**
   * Returns the result of this future, waiting at most the given duration.
   *
   * @param timeout the duration to wait for the result
   * @return the result of this future
   * @throws TimeoutException     if the result was not available within the given timeout
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  Or<G, B> get(Duration timeout) throws TimeoutException, InterruptedException;

  /**
   * Returns the result of this future, waiting at most the given duration, or returns
   * the a Bad containing the given timeoutBad.
   *
   * @param timeout    the duration to wait for the result
   * @param timeoutBad the failure to return if the timeout expired
   * @return the result of this future or a Bad containing the given value
   * @throws InterruptedException if the current thread was interrupted while waiting
   */
  Or<G, B> get(Duration timeout, B timeoutBad) throws InterruptedException;

  /**
   * Returns an accumulating version of this future.
   *
   * @return an accumulating version of this future
   */
  default OrFuture<G, Every<B>> accumulating() {
    return transform(Function.identity(), Every::of);
  }

  /**
   * Creates a new future by filtering this future's result according to
   * the {@link Or#filter(Function)} method.
   *
   * @param validator the validation function to apply
   * @return a filtered future
   */
  default OrFuture<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
    OrPromise<G, B> promise = OrPromise.create();
    onComplete(or -> promise.complete(or.filter(validator)));
    return promise.future();
  }

  /**
   * Creates a new future by applying a function to the result of this future if it is a Good,
   * otherwise returns a future containing the original Bad.
   *
   * @param mapper the mapping function
   * @param <H>    the type of the mapped Good
   * @return a new future whose success result is mapped
   */
  default <H> OrFuture<H, B> map(Function<? super G, ? extends H> mapper) {
    OrPromise<H, B> promise = OrPromise.create();
    onComplete(or -> promise.complete(or.map(mapper)));
    return promise.future();
  }

  default <C> OrFuture<G, C> badMap(Function<? super B, ? extends C> mapper) {
    OrPromise<G, C> promise = OrPromise.create();
    onComplete(or -> promise.complete(or.badMap(mapper)));
    return promise.future();
  }

  /**
   * Creates a new future by applying a function to the result of this future
   * if it is a Good, and returns the result of the function as the new future.
   * If this future completes with a Bad, the new future returns the Bad.
   *
   * @param mapper the mapping function
   * @param <H>    the type of the mapped Good
   * @return a new future whose success result is flatMapped
   */
  default <H> OrFuture<H, B> flatMap(Function<? super G, ? extends OrFuture<? extends H, ? extends B>> mapper) {
    OrPromise<H, B> promise = OrPromise.create();
    onComplete(or -> or.forEach(
      g -> promise.completeWith(mapper.apply(g)),
      promise::failure));
    return promise.future();
  }

  /**
   * Creates a new future that will recover any potential Bad into a Good using
   * the supplied function.
   *
   * @param fn recovery function
   * @return a new future that completes with a Good,
   * possibly as a result of a recovery
   */
  default OrFuture<G, B> recover(Function<? super B, ? extends G> fn) {
    OrPromise<G, B> promise = OrPromise.create();
    onComplete(or -> promise.complete(or.recover(fn)));
    return promise.future();
  }

  /**
   * Creates a new future that will recover any potential Bad into the a new future.
   *
   * @param fn  the recovery function
   * @param <C> the failure type of the new future
   * @return a new future whose value is the result of a recovery if this future completes with a Bad
   */
  default <C> OrFuture<G, C> recoverWith(Function<? super B, ? extends OrFuture<? extends G, ? extends C>> fn) {
    OrPromise<G, C> promise = OrPromise.create();
    onComplete(or -> or.forEach(
      promise::success,
      b -> promise.completeWith(fn.apply(b))));
    return promise.future();
  }

  /**
   * Creates a new future by <br>
   * - applying the 's' function to the Good value if this future completes with a Good<br>
   * - applying the 'f' function to the Bad value if this future completes with a Bad
   *
   * @param s   the function to use to transform the success value of this future
   * @param f   the function to use to transform the failure value of this future
   * @param <H> the success type of the new future
   * @param <C> the failure type of the new future
   * @return a new future with a transformed value
   */
  default <H, C> OrFuture<H, C> transform(Function<? super G, ? extends H> s, Function<? super B, ? extends C> f) {
    OrPromise<H, C> promise = OrPromise.create();
    onComplete(or -> promise.complete(or.transform(s, f)));
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // VALIDATED BY
  // ----------------------------------------------------------------------------------------------

  /**
   * Maps an iterable of Fs into OrFutures of type OrFuture&lt;G, EVERY&lt;ERR&gt;&gt;
   * (where EVERY is some subtype of Every) using the passed function f, then
   * combines the resulting OrFutures into a single OrFuture of type OrFuture&lt;Vector&lt;G&gt;,
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
  static <F, G, ERR> OrFuture<Vector<G>, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> f) {
    return validatedBy(iterable, f, Vector.collector());
  }

  /**
   * Maps an iterable of Fs into OrFutures of type OrFuture&lt;G, EVERY&lt;ERR&gt;&gt;
   * (where EVERY is some subtype of Every) using the passed function f, then
   * combines the resulting OrFutures into a single OrFuture of type OrFuture&lt;COLL&lt;G&gt;,
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
  static <F, G, A, I extends Iterable<? extends G>, ERR> OrFuture<I, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> f,
              Collector<? super G, A, I> collector) {

    Vector<OrFuture<? extends G, ? extends Every<? extends ERR>>> futures =
      Iterator
        .ofAll(iterable)
        .foldLeft(Vector.empty(), (vec, elem) -> vec.append(f.apply(elem)));
    return combined(futures, collector);
  }

  // ----------------------------------------------------------------------------------------------
  // WHEN
  // ----------------------------------------------------------------------------------------------

  /**
   * Enables further validation on an existing accumulating OrFuture by passing validation functions.
   *
   * @param <G>         the Good type of the argument OrFuture
   * @param <ERR>       the type of the error message contained in the accumulating failure
   * @param or          the accumulating OrFuture
   * @param validations the validation functions
   * @return the original or if it passed all validations or a Bad with all failures
   */
  @SafeVarargs
  static <G, ERR> OrFuture<G, Every<ERR>>
  when(OrFuture<? extends G, ? extends Every<? extends ERR>> or,
       Function<? super G, ? extends Validation<ERR>>... validations) {

    OrPromise<G, Every<ERR>> promise = OrPromise.create();
    or.onComplete(o -> promise.complete(Accumulation.when(o, validations)));
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // WITHGOOD
  // ----------------------------------------------------------------------------------------------

  /**
   * Combines two accumulating OrFutures into a single one using the given function.
   * The resulting OrFuture will complete with a Good if both OrFutures complete with
   * Goods, otherwise it will complete with a Bad containing every error in the Bads.
   *
   * @param <A>      the success type
   * @param <B>      the success type
   * @param <ERR>    the error type of the OrFutures
   * @param <RESULT> the success type of the resulting OrFuture
   * @param fa       the first OrFuture to combine
   * @param fb       the second OrFuture to combine
   * @param function the function combining the OrFutures
   * @return an OrFuture that completes with a Good if both OrFutures completed with
   * Goods, otherwise completes with a Bad containing every error.
   */
  static <A, B, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           BiFunction<? super A, ? super B, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          promise.complete(Accumulation.withGood(ora, orb, function))))
    );
  }

  static <A, B, C, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           Function3<? super A, ? super B, ? super C, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            promise.complete(Accumulation.withGood(ora, orb, orc, function)))))
    );
  }

  static <A, B, C, D, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           Function4<? super A, ? super B, ? super C, ? super D, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              promise.complete(Accumulation.withGood(ora, orb, orc, ord, function))))))
    );
  }

  static <A, B, C, D, E, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, function)))))))
    );
  }

  static <A, B, C, D, E, F, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, function))))))))
    );
  }

  static <A, B, C, D, E, F, G, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends RESULT> function) {
    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  fg.onComplete(org ->
                    promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, function)))))))))
    );
  }

  static <A, B, C, D, E, F, G, H, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           OrFuture<? extends H, ? extends Every<? extends ERR>> fh,
           Function8<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? super H, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  fg.onComplete(org ->
                    fh.onComplete(orh ->
                      promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, orh, function)))))))))));
  }

  // ----------------------------------------------------------------------------------------------
  // FIRST COMPLETED OF
  // ----------------------------------------------------------------------------------------------

  /**
   * Returns an OrFuture that will complete as soon as the first OrFuture from the given Iterable
   * completes.
   *
   * @param input an Iterable of OrFutures
   * @param <G> the good type of the future
   * @param <ERR> the bad type of the future
   * @return the first future to complete
   */
  static <G, ERR> OrFuture<G, ERR>
  firstCompletedOf(Iterable<? extends OrFuture<? extends G, ? extends ERR>> input) {
    OrPromise<G, ERR> promise = OrPromise.create();
    input.forEach(future -> future.onComplete(promise::tryComplete));
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // SEQUENCE
  // ----------------------------------------------------------------------------------------------

  /**
   * Transforms an Iterable of OrFutures&lt;G, ERR&gt; into an OrFuture&lt;Vector&lt;G, ERR&gt;&gt;.
   *
   * @param input iterable of OrFutures
   * @param <G> the good type of the future
   * @param <ERR> the bad type of the future
   * @return a single OrFuture
   */
  static <G, ERR> OrFuture<Vector<G>, Every<ERR>>
  sequence(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return sequence(input, Vector.collector());
  }

  /**
   * Transforms an Iterable of OrFutures&lt;G, ERR&gt; into an OrFuture&lt;COLL&lt;G, ERR&gt;&gt;
   * where COLL is a collection created with the collector given as argument.
   *
   * @param input iterable of OrFutures
   * @param collector a collector to collect the results of the transformation
   * @param <G> the good type of the future
   * @param <ERR> the bad type of the future
   * @param <A> the mutable accumulation type of the reduction operation
   * @param <I> the result type of the reduction operation
   * @return a single OrFuture
   */
  @SuppressWarnings("unchecked")
  static <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
  sequence(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {

    OrPromise<I, Every<ERR>> promise = OrPromise.create();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    AtomicBoolean failed = new AtomicBoolean(false);
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    while (iterator.hasNext()) {
      count.incrementAndGet();
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      if (!iterator.hasNext()) finished.set(true);
      future.onComplete(or -> {
        if (or.isBad() && !failed.getAndSet(true)) {
          promise.failure((Every<ERR>) or.getBad());
        } else if (!failed.get() && count.decrementAndGet() == 0 && finished.get()) {
          promise.complete(accumulate(input, collector));
        }
      });
    }
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // COMBINED
  // ----------------------------------------------------------------------------------------------

  /**
   * Combines an Iterable of OrFutures of type OrFuture&lt;G, EVERY&lt;ERR&gt;&gt; (where
   * EVERY is some subtype of Every) into a single OrFuture of type
   * OrFuture&lt;Vector&lt;G&gt;, Every&lt;ERR&gt;&gt;.
   *
   * @param <G>   the success type
   * @param <ERR> the failure type
   * @param input the iterable containing OrFutures to combine
   * @return an OrFuture that completes with all the success values or with all the errors
   */
  static <G, ERR> OrFuture<Vector<G>, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return combined(input, Vector.collector());
  }

  /**
   * Combines an Iterable of OrFutures of type OrFuture&lt;G, EVERY&lt;ERR&gt;&gt; (where
   * EVERY is some subtype of Every) into a single OrFuture of type
   * OrFuture&lt;COLL&lt;G&gt;, Every&lt;ERR&gt;&gt; using a Collector to determine
   * the wanted collection type COLL.
   *
   * @param <G>       the success type
   * @param <A>       the mutable accumulation type of the reduction operation of the collector
   * @param <ERR>     the failure type
   * @param <I>       the resulting success iterable type
   * @param input     the iterable containing OrFutures to combine
   * @param collector the collector producing the resulting collection
   * @return an OrFuture that completes with all the success values or with all the errors
   */
  static <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {

    OrPromise<I, Every<ERR>> promise = OrPromise.create();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    while (iterator.hasNext()) {
      count.incrementAndGet();
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      if (!iterator.hasNext()) finished.set(true);
      future.onComplete(or -> {
        if (count.decrementAndGet() == 0 && finished.get()) {
          promise.complete(accumulate(input, collector));
        }
      });
    }
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // COLLECT
  // ----------------------------------------------------------------------------------------------

  static <G, ERR> Future<Vector<Or<G, Every<ERR>>>>
  collect(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return collect(input, Vector.collector());
  }

  @SuppressWarnings("unchecked")
  static <G, ERR, A, I extends Iterable<? extends Or<? extends G, ? extends Every<? extends ERR>>>> Future<I>
  collect(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
          Collector<? super Or<G, Every<ERR>>, A, I> collector) {

    Promise<I> promise = Promise.make();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    Stream.Builder<AtomicReference<Or<G, Every<ERR>>>> builder = Stream.builder();
    while(iterator.hasNext()) {
      count.incrementAndGet();
      AtomicReference<Or<G, Every<ERR>>> container = new AtomicReference<>();
      builder.add(container);
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      if(!iterator.hasNext()) finished.set(true);
      future.onComplete(or -> {
        container.set((Or<G, Every<ERR>>) or);
        if(count.decrementAndGet() == 0 && finished.get()) {
          I collected = builder.build().map(AtomicReference::get).collect(collector);
          promise.complete(Try.success(collected));
        }
      });
    }

    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // ZIPS
  // ----------------------------------------------------------------------------------------------

  /**
   * Zips two accumulating OrFutures together. If both complete with Goods, returns an OrFuture
   * that completes with a Good tuple containing both original Good values. Otherwise returns an
   * OrFuture that completes with a Bad containing every error message.
   *
   * @param <A>   the success type
   * @param <B>   the success type
   * @param <ERR> the error type of the accumulating OrFutures
   * @param a     the first OrFuture to zip
   * @param b     the second OrFuture to zip
   * @return an OrFuture that completes with a Good of type Tuple2 if both OrFutures completed
   * with Goods, otherwise returns an OrFuture that completes with a Bad containing every error.
   */
  static <A, B, ERR> OrFuture<Tuple2<A, B>, Every<ERR>>
  zip(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
      OrFuture<? extends B, ? extends Every<? extends ERR>> b) {
    return withGood(a, b, Tuple::of);
  }

  static <A, B, C, ERR> OrFuture<Tuple3<A, B, C>, Every<ERR>>
  zip3(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c) {
    return withGood(a, b, c, Tuple::of);
  }

}
