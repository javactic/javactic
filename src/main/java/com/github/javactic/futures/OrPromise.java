package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;

import java.util.concurrent.ForkJoinPool;

public interface OrPromise<G, B> {

  static <G, B> OrPromise<G, B> create() {
    return new OrPromiseImpl<>(new OrFutureImpl<>(ForkJoinPool.commonPool()));
  }

  OrFuture<G, B> future();

  boolean tryComplete(Or<? extends G, ? extends B> result);

  default OrPromise<G, B> complete(Or<? extends G, ? extends B> result) {
    if (tryComplete(result)) {
      return this;
    } else {
      throw new IllegalStateException("Promise already completed.");
    }
  }

  default OrPromise<G, B> completeWith(OrFuture<? extends G, ? extends B> other) {
    return tryCompleteWith(other);
  }

  default OrPromise<G, B> tryCompleteWith(OrFuture<? extends G, ? extends B> other) {
    other.onComplete(this::tryComplete);
    return this;
  }

  default OrPromise<G, B> bad(B failure) {
    return complete(Bad.of(failure));
  }

  default OrPromise<G, B> good(G success) {
    return complete(Good.of(success));
  }

  default boolean tryBad(B failure) {
    return tryComplete(Bad.of(failure));
  }

  default boolean tryGood(G success) {
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
