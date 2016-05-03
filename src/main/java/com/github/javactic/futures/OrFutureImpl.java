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
import com.github.javactic.Or;
import javaslang.control.Option;
import javaslang.control.Try;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

class OrFutureImpl<G, B> implements OrFuture<G, B> {

  private final Executor executor;
  private final AtomicReference<Or<G, B>> value = new AtomicReference<>();
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final CountDownLatch finished = new CountDownLatch(1);
  private final Queue<Consumer<? super Or<G, B>>> actions = new ArrayDeque<>();

  public OrFutureImpl(Executor executor) {
    this.executor = executor;
  }

  @SuppressWarnings("unchecked")
  boolean tryComplete(Or<? extends G, ? extends B> value) {
    return complete((Or<G, B>) value) != null;
  }

  @Override
  public boolean isCompleted() {
    return value.get() != null;
  }

  @SuppressWarnings("unchecked")
  void run(Supplier<? extends Or<? extends G, ? extends B>> orSupplier) {
    if (isCompleted()) {
      throw new IllegalStateException("the future is already completed.");
    } else if (started.compareAndSet(false, true)) {
      // we got the right to start the job
      executor.execute(() -> complete((Or<G,B>) orSupplier.get()));
    } else {
      throw new IllegalStateException("the future is already running.");
    }
  }

  Or<G, B> complete(Or<G, B> result) {
    Objects.requireNonNull(result, "cannot complete with null");
    if (value.compareAndSet(null, result)) {
      finished.countDown();
      // sync necessary so onComplete does not get called twice for an action
      synchronized (actions) {
        actions.forEach(this::perform);
      }
      return result;
    } else {
      return null;
    }
  }

  @Override
  public void onComplete(Consumer<? super Or<G, B>> action) {
    Objects.requireNonNull(action, "action is null");
    if (isCompleted()) {
      perform(action);
    } else {
      // sync necessary so onComplete does not get called twice for an action
      synchronized (actions) {
        if (isCompleted()) {
          perform(action);
        } else {
          actions.add(action);
        }
      }
    }
  }

  private void perform(Consumer<? super Or<G, B>> action) {
    Try.run(() -> executor.execute(() -> action.accept(value.get())));
  }

  @Override
  public Option<Or<G, B>> getOption() {
    return isCompleted() ? Option.some(value.get()) : Option.none();
  }

  @Override
  public Or<G, B> get(Duration timeout) throws InterruptedException, TimeoutException {
    if (finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) return value.get();
    else throw new TimeoutException("timeout waiting for result");
  }

  @Override
  public Or<G, B> get(Duration timeout, B timeoutBad) throws InterruptedException {
    if (finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) return value.get();
    else return Bad.of(timeoutBad);
  }

  @Override
  public Or<G, B> getUnsafe() throws CompletionException {
    try {
      return get(Duration.ofMillis(Long.MAX_VALUE));
    } catch (InterruptedException | TimeoutException e) {
      throw new CompletionException(e);
    }
  }

  @Override
  public String toString() {
    return "OrFuture(" + getOption().map(Object::toString).getOrElse("N/A") + ")";
  }

}
