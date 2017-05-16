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
import com.github.javactic.One;
import com.github.javactic.Or;
import com.github.javactic.Validation;
import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.Vector;
import io.vavr.control.Try;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;

import static com.github.javactic.futures.Helper.accumulate;
import static com.github.javactic.futures.Helper.withPromise;

public class ExecutionContext<BAD> {

  private final Function<? super Throwable, ? extends BAD> converter;
  private final Executor executor;
  private final Lazy<ExecutionContext<One<BAD>>> accumulating;

  private ExecutionContext(Function<? super Throwable, ? extends BAD> exceptionConverter, Executor executor) {
    this.executor = executor;
    this.converter = exceptionConverter;
    this.accumulating = Lazy.of(() -> new ExecutionContext<>(exceptionConverter.andThen(One::of), executor));
  }

  /**
   * @param exceptionConverter a function to convert exceptions into instances of Bad
   * @param executor the default executor to use for all operations in this execution context
   * @param <B> the bad type
   * @return a new future factory
   */
  public static <B> ExecutionContext<B> of(Function<? super Throwable, ? extends B> exceptionConverter, Executor executor) {
    return new ExecutionContext<>(exceptionConverter, executor);
  }

  public Executor getExecutor() {
    return executor;
  }

  public <H,C> OrPromise<H, C> promise() {
    return new OrPromiseImpl<>(new OrFutureImpl<>(this));
  }

  /**
   * Creates an OrFuture that will execute the given task using this context's executor.
   *
   * @param task asynchronous computation to execute
   * @param <G> the good type
   * @return a new future that completes with the result of the supplied task, or if the execution of the
   * task throws an exception, that exception will be handled with this factory's exception converter.
   */
  @SuppressWarnings("unchecked")
  public <G> OrFuture<G, BAD> future(CheckedFunction0<? extends Or<? extends G, ? extends BAD>> task) {
    OrFutureImpl<G, BAD> future = new OrFutureImpl<>(this);
    executor.execute(() -> {
      try {
        future.complete((Or<G, BAD>) task.apply());
      } catch (Throwable t) {
        future.complete(Bad.of(converter.apply(t)));
      }
    });
    return future;
  }

  /**
   * Returns an accumulating version of this context.
   *
   * @return an accumulating version of this context
   */
  public ExecutionContext<One<BAD>> accumulating() {
    return accumulating.get();
  }

  public static final Function<Throwable, String> OF_EXCEPTION_MESSAGE =
    throwable -> String.valueOf(throwable.getMessage()); // null -> "null"

  /**
   * Creates an OrFuture that is already completed with a {@link Bad}.
   *
   * @param bad the failure value
   * @param <G> the success type
   * @return an instance of OrFuture
   */
  public <G> OrFuture<G, BAD> badFuture(BAD bad) {
    return this.<G, BAD>promise().failure(bad).future();
  }

  /**
   * Creates an OrFuture that is already completed with a {@link Good}.
   *
   * @param good the success value
   * @param <G>  the success type
   * @return an instance of OrFuture
   */
  public <G> OrFuture<G, BAD> goodFuture(G good) {
    return this.<G, BAD>promise().success(good).future();
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
  public <F, G, ERR> OrFuture<Vector<G>, Every<ERR>>
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
  public <F, G, A, I extends Iterable<? extends G>, ERR> OrFuture<I, Every<ERR>>
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
  public final <G, ERR> OrFuture<G, Every<ERR>>
  when(OrFuture<? extends G, ? extends Every<? extends ERR>> or,
       Function<? super G, ? extends Validation<ERR>>... validations) {

    OrPromise<G, Every<ERR>> promise = promise();
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
  public <A, B, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           BiFunction<? super A, ? super B, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              promise.complete(Accumulation.withGood(ora, orb, function)))));
  }

  public <A, B, C, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           Function3<? super A, ? super B, ? super C, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  promise.complete(Accumulation.withGood(ora, orb, orc, function))))));
  }

  public <A, B, C, D, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           Function4<? super A, ? super B, ? super C, ? super D, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  fd.onComplete(ord ->
                      promise.complete(Accumulation.withGood(ora, orb, orc, ord, function)))))));
  }

  public <A, B, C, D, E, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  fd.onComplete(ord ->
                      fe.onComplete(ore ->
                          promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, function))))))));
  }

  public <A, B, C, D, E, F, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  fd.onComplete(ord ->
                      fe.onComplete(ore ->
                          ff.onComplete(orf ->
                              promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, function)))))))));
  }

  public <A, B, C, D, E, F, G, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends RESULT> function) {
    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  fd.onComplete(ord ->
                      fe.onComplete(ore ->
                          ff.onComplete(orf ->
                              fg.onComplete(org ->
                                  promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, function))))))))));
  }

  public <A, B, C, D, E, F, G, H, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           OrFuture<? extends H, ? extends Every<? extends ERR>> fh,
           Function8<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? super H, ? extends RESULT> function) {

    return withPromise(this, promise ->
      fa.onComplete(ora ->
          fb.onComplete(orb ->
              fc.onComplete(orc ->
                  fd.onComplete(ord ->
                      fe.onComplete(ore ->
                          ff.onComplete(orf ->
                              fg.onComplete(org ->
                                  fh.onComplete(orh ->
                                      promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, orh, function))))))))))
    );
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
  public <G, ERR> OrFuture<G, ERR>
  firstCompletedOf(Iterable<? extends OrFuture<? extends G, ? extends ERR>> input) {
    OrPromise<G, ERR> promise = promise();
    input.forEach(future -> future.onComplete(promise::tryComplete));
    return promise.future();
  }

  // ----------------------------------------------------------------------------------------------
  // SEQUENCE
  // ----------------------------------------------------------------------------------------------

  /**
   * Transforms an Iterable of OrFutures&lt;G, ERR&gt; into an OrFuture&lt;Vector&lt;G, ERR&gt;&gt;.
   * <p>
   * This method differs from combined in that the returned future will fail fast and complete as soon
   * as one of the given futures fails.
   *
   * @param input iterable of OrFutures
   * @param <G> the good type of the future
   * @param <ERR> the bad type of the future
   * @return a single OrFuture
   */
  public <G, ERR> OrFuture<Vector<G>, Every<ERR>>
  sequence(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return sequence(input, Vector.collector());
  }

  /**
   * Transforms an Iterable of OrFutures&lt;G, ERR&gt; into an OrFuture&lt;COLL&lt;G, ERR&gt;&gt;
   * where COLL is a collection created with the collector given as argument.
   * <p>
   * This method differs from combined in that the returned future will fail fast and complete as soon
   * as one of the given futures fails.
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
  public <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
  sequence(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {
    OrPromise<I, Every<ERR>> promise = promise();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    // this is necessary as nothing guarantees the iterable can be iterated multiple times
    Queue<OrFuture<? extends G, ? extends Every<? extends ERR>>> copy = new ConcurrentLinkedQueue<>();
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    while (iterator.hasNext()) {
      count.incrementAndGet();
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      copy.add(future);
      if (!iterator.hasNext()) finished.set(true);
      future.onComplete(or -> {
        if (or.isBad()) {
          promise.tryFailure((Every<ERR>) or.getBad());
        } else if (count.decrementAndGet() == 0 && finished.get()) {
          promise.complete(accumulate(copy, collector));
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
   * <p>
   * This method differs from sequence in that it will accumulate every error in the returned
   * future before completing.
   *
   * @param <G>   the success type
   * @param <ERR> the failure type
   * @param input the iterable containing OrFutures to combine
   * @return an OrFuture that completes with all the success values or with all the errors
   */
  public <G, ERR> OrFuture<Vector<G>, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return combined(input, Vector.collector());
  }

  /**
   * Combines an Iterable of OrFutures of type OrFuture&lt;G, EVERY&lt;ERR&gt;&gt; (where
   * EVERY is some subtype of Every) into a single OrFuture of type
   * OrFuture&lt;COLL&lt;G&gt;, Every&lt;ERR&gt;&gt; using a Collector to determine
   * the wanted collection type COLL.
   * <p>
   * This method differs from sequence in that it will accumulate every error in the returned
   * future before completing.
   *
   * @param <G>       the success type
   * @param <A>       the mutable accumulation type of the reduction operation of the collector
   * @param <ERR>     the failure type
   * @param <I>       the resulting success iterable type
   * @param input     the iterable containing OrFutures to combine
   * @param collector the collector producing the resulting collection
   * @return an OrFuture that completes with all the success values or with all the errors
   */
  public <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {
    OrPromise<I, Every<ERR>> promise = promise();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    // this is necessary as nothing guarantees the iterable can be iterated multiple times
    Queue<OrFuture<? extends G, ? extends Every<? extends ERR>>> copy = new ConcurrentLinkedQueue<>();
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    while (iterator.hasNext()) {
      count.incrementAndGet();
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      copy.add(future);
      if (!iterator.hasNext()) finished.set(true);
      future.onComplete(or -> {
        if (count.decrementAndGet() == 0 && finished.get()) {
          promise.complete(accumulate(copy, collector));
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
  public <A, B, ERR> OrFuture<Tuple2<A, B>, Every<ERR>>
  zip(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
      OrFuture<? extends B, ? extends Every<? extends ERR>> b) {
    return withGood(a, b, Tuple::of);
  }

  public <A, B, C, ERR> OrFuture<Tuple3<A, B, C>, Every<ERR>>
  zip3(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c) {
    return withGood(a, b, c, Tuple::of);
  }

}
