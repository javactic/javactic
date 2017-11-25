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
import com.github.javactic.Validation;
import io.vavr.control.Option;

import java.time.Duration;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

class OrFutureImpl<G, B> implements OrFuture<G, B> {

  private final ExecutionContext<?> executionContext;
  private final AtomicReference<Or<G, B>> value = new AtomicReference<>();
  private final CountDownLatch finished = new CountDownLatch(1);
  private final Queue<Consumer<? super Or<G, B>>> actions = new ConcurrentLinkedQueue<>();

  OrFutureImpl(ExecutionContext<?> executionContext) {
    this.executionContext = executionContext;
  }

  @SuppressWarnings("unchecked")
  boolean tryComplete(Or<? extends G, ? extends B> value) {
    return complete((Or<G, B>) value);
  }

  @Override
  public boolean isCompleted() {
    return value.get() != null;
  }

  boolean complete(Or<G, B> result) {
    Objects.requireNonNull(result, "cannot complete with null");
    if (value.compareAndSet(null, result)) {
      finished.countDown();
      // sync necessary so onComplete does not get called twice for an action
      synchronized (actions) {
        actions.forEach(this::perform);
      }
      return true;
    } else {
      return false;
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
    executionContext.getExecutor().execute(() -> {
      try {
        action.accept(value.get());
      } catch (Throwable t) {
        handleUncaughtThrowable(t);
      }
    });
  }

  private void handleUncaughtThrowable(Throwable t) {
    Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
    if(handler != null) handler.uncaughtException(Thread.currentThread(), t);
    else {
      handler = Thread.getDefaultUncaughtExceptionHandler();
      if(handler != null) handler.uncaughtException(Thread.currentThread(), t);
      else System.err.println("no default or other UncaughtExceptionHandler found for Throwable " + t.toString());
    }
  }

  @Override
  public Option<Or<G, B>> getOption() {
    return Option.of(value.get());
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


  public OrFuture<G, B> andThen(Consumer<? super Or<G, B>> consumer) {
    OrPromise<G, B> p = executionContext.promise();
    onComplete(or -> {
      try {
        consumer.accept(or);
      } catch (Throwable t) {
        handleUncaughtThrowable(t);
      } finally {
        p.complete(or);
      }
    });
    return p.future();
  }

  public OrFuture<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
    OrPromise<G, B> promise = executionContext.promise();
    onComplete(or -> promise.complete(or.filter(validator)));
    return promise.future();
  }

  public <H> OrFuture<H, B> flatMap(Function<? super G, ? extends OrFuture<? extends H, ? extends B>> mapper) {
    OrPromise<H, B> promise = executionContext.promise();
    onComplete(or ->
        or.forEach(
          g -> promise.completeWith(mapper.apply(g)),
          promise::failure)
    );
    return promise.future();
  }

  public OrFuture<G, B> recover(Function<? super B, ? extends G> fn) {
    OrPromise<G, B> promise = executionContext.promise();
    onComplete(or -> promise.complete(or.recover(fn)));
    return promise.future();
  }

  @SuppressWarnings("unchecked")
  public <C> OrFuture<G, C> recoverWith(Function<? super B, ? extends OrFuture<? extends G, ? extends C>> fn) {
    return transformWith(or -> {
      if(or.isGood()) return (OrFuture<G,C>)this;
      else return fn.apply(or.getBad());
    });
  }

  public <H, C> OrFuture<H, C> transform(Function<? super Or<? extends G, ? extends B>, ? extends Or<? extends H, ? extends C>> f) {
    OrPromise<H, C> promise = executionContext.promise();
    onComplete(or -> promise.complete(f.apply(or)));
    return promise.future();
  }

  public <H, C> OrFuture<H, C> transformWith(Function<? super Or<? extends G, ? extends B>, ? extends OrFuture<? extends H, ? extends C>> f) {
    OrPromise<H, C> promise = executionContext.promise();
    onComplete(or -> promise.completeWith(f.apply(or)));
    return promise.future();
  }
}
