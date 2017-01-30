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

import com.github.javactic.One;
import com.github.javactic.Or;
import com.github.javactic.Validation;
import javaslang.control.Option;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>
 * The interface for disjunction futures. OrFutures do not succeed or fail, they
 * complete with an instance of Or.
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
//  static <G, B> OrFuture<G, B> of(Supplier<? extends Or<? extends G, ? extends B>> task, Executor... executor) {
//    final OrFutureImpl<G, B> future = new OrFutureImpl<>(Helper.getExecutor(executor));
//    future.run(task);
//    return future;
//  }

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
//  static <G, B> OrFuture<G, B> of(Supplier<? extends Or<? extends G, ? extends B>> task) {
//    return of(DEFAULT_EXECUTOR, task);
//  }

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
   * <p>
   * While occasionally useful, e.g. for testing, it is recommended that you avoid this method when possible in favor
   * of callbacks and combinators like onComplete. This method will block the thread on which it runs, and could
   * cause performance and deadlock issues.
   *
   * @param timeout the duration to wait for the result
   * @return the result of this future
   * @throws TimeoutException     if the result was not available within the given timeout
   * @throws InterruptedException if the current thread was interrupted while waiting
   * @throws ArithmeticException if arithmetic overflow occurs when converting the given duration to milliseconds
   */
  Or<G, B> get(Duration timeout) throws TimeoutException, InterruptedException, ArithmeticException;

  /**
   * Returns the result of this future, waiting at most the given duration, or returns
   * the a Bad containing the given timeoutBad.
   * <p>
   * While occasionally useful, e.g. for testing, it is recommended that you avoid this method when possible in favor
   * of callbacks and combinators like onComplete. This method will block the thread on which it runs, and could
   * cause performance and deadlock issues.
   *
   * @param timeout    the duration to wait for the result
   * @param timeoutBad the failure to return if the timeout expired
   * @return the result of this future or a Bad containing the given value
   * @throws InterruptedException if the current thread was interrupted while waiting
   * @throws ArithmeticException if arithmetic overflow occurs when converting the given duration to milliseconds
   */
  Or<G, B> get(Duration timeout, B timeoutBad) throws InterruptedException, ArithmeticException;

  /**
   * Returns the result of this future, waiting forever. This method is meant to be used in test cases only or
   * in the rare cases when there are other means of knowing that the future has already completed. This method will
   * wrap any thrown exception in a {@link CompletionException}.
   * <p>
   * It is recommended that you avoid this method when possible in favor
   * of callbacks and combinators like onComplete. This method will block the thread on which it runs, and could
   * cause performance and deadlock issues.
   *
   * @return the result of this future
   * @throws CompletionException containing the real cause of failure
   */
  Or<G, B> getUnsafe() throws CompletionException;

  /**
   * Returns an accumulating version of this future.
   *
   * @return an accumulating version of this future
   */
  default OrFuture<G, One<B>> accumulating() {
    return transform(Function.identity(), One::of);
  }

  /**
   * Applies the side-effecting function to the result of this future, and returns a new future with the result of
   * this future. The returned future will complete only once the given consumer has completed.
   * <p>
   * This method allows one to enforce that the callbacks are executed in a specified order.
   * <p>
   * Note that if one of the chained andThen callbacks throws an exception, that exception is not propagated to
   * the subsequent andThen callbacks. Instead, the subsequent andThen callbacks are given the original value
   * of this future.
   *
   * @param consumer the side-effecting function to execute
   * @return a new future with the result of this future
   */
  OrFuture<G, B> andThen(Consumer<? super Or<G, B>> consumer);

  /**
   * Creates a new future by filtering this future's result according to
   * the {@link Or#filter(Function)} method.
   *
   * @param validator the validation function to apply
   * @return a filtered future
   */
  OrFuture<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator);

  /**
   * Creates a new future by applying a function to the result of this future if it is a Good,
   * otherwise returns a future containing the original Bad.
   *
   * @param mapper the mapping function
   * @param <H>    the type of the mapped Good
   * @return a new future whose success result is mapped
   */
  <H> OrFuture<H, B> map(Function<? super G, ? extends H> mapper);

  <C> OrFuture<G, C> badMap(Function<? super B, ? extends C> mapper);

  /**
   * Creates a new future by applying a function to the result of this future
   * if it is a Good, and returns the result of the function as the new future.
   * If this future completes with a Bad, the new future returns the Bad.
   *
   * @param mapper the mapping function
   * @param <H>    the type of the mapped Good
   * @return a new future whose success result is flatMapped
   */
  <H> OrFuture<H, B> flatMap(Function<? super G, ? extends OrFuture<? extends H, ? extends B>> mapper);

  /**
   * Creates a new future that will recover any potential Bad into a Good using
   * the supplied function.
   *
   * @param fn recovery function
   * @return a new future that completes with a Good,
   * possibly as a result of a recovery
   */
  OrFuture<G, B> recover(Function<? super B, ? extends G> fn);

  /**
   * Creates a new future that will recover any potential Bad into the a new future.
   *
   * @param fn  the recovery function
   * @param <C> the failure type of the new future
   * @return a new future whose value is the result of a recovery if this future completes with a Bad
   */
  <C> OrFuture<G, C> recoverWith(Function<? super B, ? extends OrFuture<? extends G, ? extends C>> fn);

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
  <H, C> OrFuture<H, C> transform(Function<? super G, ? extends H> s, Function<? super B, ? extends C> f);

  /**
   * Switches the context to the one given as argument. The returned future will execute its operation
   * with the given context. This can be useful when the result of the future has to be executed on a
   * specific context such as a UI thread.
   *
   * @param context the new {@link ExecutionContext} to use
   * @return a new future whose operations will be executed on the given context
   */
  default OrFuture<G, B> with(ExecutionContext<? extends B> context) {
    OrPromise<G, B> promise = context.promise();
    onComplete(promise::complete);
    return promise.future();
  }
}
