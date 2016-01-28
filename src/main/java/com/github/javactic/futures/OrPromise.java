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
import com.github.javactic.Good;
import com.github.javactic.Or;

import static com.github.javactic.futures.Helper.DEFAULT_EXECUTOR;

/**
 * A write-once wrapper that can complete an underlying OrFuture.
 *
 * @param <G> the success type
 * @param <B> the failure type
 */
public interface OrPromise<G, B> {

  /**
   * Creates a new promise that can be fulfilled later.
   *
   * @param <G> the success type
   * @param <B> the failure type
   * @return an instance of OrPromise
   */
  static <G, B> OrPromise<G, B> create() {
    return new OrPromiseImpl<>(new OrFutureImpl<>(DEFAULT_EXECUTOR));
  }

  /**
   * @return a future that will complete when the promise is fulfilled
   */
  OrFuture<G, B> future();

  /**
   * Attempts to complete this promise with the given value.
   *
   * @param result the value to complete the promise with
   * @return true if the promise was completed with the given value,
   * false if the promise was already fulfilled
   */
  boolean tryComplete(Or<? extends G, ? extends B> result);

  /**
   * Completes the underlying future with the given value.
   *
   * @param result the value to complete the promise with
   * @return this promise
   * @throws IllegalStateException if the promise is already fulfilled
   */
  default OrPromise<G, B> complete(Or<? extends G, ? extends B> result) {
    if (tryComplete(result)) {
      return this;
    } else {
      throw new IllegalStateException("Promise already completed.");
    }
  }

  /**
   * Completes this promise with the specified future, once that future is completed.
   *
   * @param other the future to fulfill this promise with
   * @return this promise
   * @throws IllegalStateException if the promise is already fulfilled
   */
  default OrPromise<G, B> completeWith(OrFuture<? extends G, ? extends B> other) {
    return tryCompleteWith(other);
  }

  /**
   * Attempts to complete this promise with the specified future, once that future is completed.
   *
   * @param other the future to fulfill this promise with
   * @return this promise
   */
  default OrPromise<G, B> tryCompleteWith(OrFuture<? extends G, ? extends B> other) {
    other.onComplete(this::tryComplete);
    return this;
  }

  /**
   * Completes this promise with the given bad value.
   *
   * @param failure the bad value to complete this promise with
   * @return this promise
   * @throws IllegalStateException if the promise is already fulfilled
   */
  default OrPromise<G, B> failure(B failure) {
    return complete(Bad.of(failure));
  }

  /**
   * Completes this promise with the given good value.
   *
   * @param success the good value to complete this promise with
   * @return this promise
   * @throws IllegalStateException if the promise is already fulfilled
   */
  default OrPromise<G, B> success(G success) {
    return complete(Good.of(success));
  }

  /**
   * Tries to complete this promise with the given bad value.
   *
   * @param failure the bad value to complete this promise with
   * @return true if this promise was fulfilled with the given value, false
   * if this promise was already fulfilled
   */
  default boolean tryFailure(B failure) {
    return tryComplete(Bad.of(failure));
  }

  /**
   * Tries to complete this promise with the given good value.
   *
   * @param success the good value to complete this promise with
   * @return true if this promise was fulfilled with the given value, false
   * if this promise was already fulfilled
   */
  default boolean trySuccess(G success) {
    return tryComplete(Good.of(success));
  }
}

final class OrPromiseImpl<G, B> implements OrPromise<G, B> {

  private final OrFutureImpl<G, B> future;

  OrPromiseImpl(OrFutureImpl<G, B> future) {
    this.future = future;
  }

  @Override
  public OrFuture<G, B> future() {
    return future;
  }

  @Override
  public boolean tryComplete(Or<? extends G, ? extends B> result) {
    return future.tryComplete(result);
  }

}
