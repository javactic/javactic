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

import com.github.javactic.Bad;
import com.github.javactic.One;
import com.github.javactic.Or;
import javaslang.Lazy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A factory for creating OrFutures that will transform unhandled exceptions
 * into instances of Bad using the provided converter.
 *
 * @param <B> the bad type
 */
public class FutureFactory<B> {

  private final Function<? super Throwable, ? extends B> converter;
  private final ExecutorService executor;
  private final Lazy<FutureFactory<One<B>>> accumulating;

  private FutureFactory(ExecutorService executor, Function<? super Throwable, ? extends B> exceptionConverter) {
    this.executor = executor;
    this.converter = exceptionConverter;
    this.accumulating = Lazy.of(() -> new FutureFactory<>(executor, exceptionConverter.andThen(One::of)));
  }

  /**
   * Creates a new FutureFactory that will create futures that run their tasks using the given
   * executor and transform unhandled exceptions into instances of Bad using the provided converter.
   *
   * @param executor the executor to run the tasks with
   * @param exceptionConverter a function to convert exceptions into instances of Bad
   * @param <B> the bad type
   * @return a new future factory
   */
  public static <B> FutureFactory<B> of(ExecutorService executor, Function<? super Throwable, ? extends B> exceptionConverter) {
    return new FutureFactory<>(executor, exceptionConverter);
  }

  /**
   * Creates a new FutureFactory that will return futures that run their tasks using a common
   * {@link Executors#newCachedThreadPool()} and transform unhandled exceptions
   * into instances of Bad using the provided converter.
   *
   * @param exceptionConverter a function to convert exceptions into instances of Bad
   * @param <B> the bad type
   * @return a new future factory
   */
  public static <B> FutureFactory<B> of(Function<? super Throwable, ? extends B> exceptionConverter) {
    return of(Helper.DEFAULT_EXECUTOR_SERVICE, exceptionConverter);
  }

  /**
   * Creates an OrFuture that will execute the given task using the executor of this FutureFactory.
   *
   * @param task asynchronous computation to execute
   * @param <G> the good type
   * @return a new future that completes with the result of the supplied task, or if the execution of the
   * task throws an exception, that exception will be handled with this factory's exception converter.
   */
  @SuppressWarnings("unchecked")
  public <G> OrFuture<G, B> newFuture(Supplier<? extends Or<? extends G, ? extends B>> task) {
    return newFuture(executor, task);
  }

  /**
   * Creates an OrFuture that will execute the given task using the provided executor.
   *
   * @param executor the executor to run the asynchronous computation on
   * @param task asynchronous computation to execute
   * @param <G> the good type
   * @return a new future that completes with the result of the supplied task, or if the execution of the
   * task throws an exception, that exception will be handled with this factory's exception converter.
   */
  @SuppressWarnings("unchecked")
  public <G> OrFuture<G, B> newFuture(ExecutorService executor, Supplier<? extends Or<? extends G, ? extends B>> task) {
    return OrFuture.of(executor, () -> {
      try {
        return task.get();
      } catch (Throwable t) {
        return Bad.of(converter.apply(t));
      }
    });
  }

  /**
   * Returns an accumulating version of this factory.
   *
   * @return an accumulating version of this factory
   */
  public FutureFactory<One<B>> accumulating() {
    return accumulating.get();
  }

  /**
   * A default FutureFactory that will execute its tasks with the {@link Executors#newCachedThreadPool()}
   * and that will transform any potential exceptions into Strings using {@link Throwable#getMessage()}
   */
  public static final FutureFactory<String> OF_EXCEPTION_MESSAGE = of(Throwable::getMessage);
}
