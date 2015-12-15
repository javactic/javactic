package com.github.javactic.futures;

import java.util.concurrent.ForkJoinPool;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;

public interface OrPromise<G,B> {

    static <G,B> OrPromise<G,B> make() {
        return new OrPromiseImpl<>(new OrFutureImpl<>(ForkJoinPool.commonPool()));
    }
    
//    static <G,B> OrPromise<G,B> failed(B bad) {
//        return OrPromise.<G,B>make().failure(bad);
//    }
//    
//    static <G,B> OrPromise<G,B> successful(G good) {
//        return OrPromise.<G,B>make().success(good);
//    }
    
    OrFuture<G,B> future();
    
    boolean tryComplete(Or<? extends G, ? extends B> result);
    
    default OrPromise<G,B> complete(Or<? extends G, ? extends B> result) {
        if (tryComplete(result)) {
            return this;
        } else {
            throw new IllegalStateException("Promise already completed.");
        }        
    }
        
    default OrPromise<G,B> completeWith(OrFuture<? extends G, ? extends B> other) {
        return tryCompleteWith(other);
    }
    
    default OrPromise<G,B> tryCompleteWith(OrFuture<? extends G,? extends B> other) {
        other.onComplete(this::tryComplete);
        return this;
    }

    default OrPromise<G,B> failure(B failure) {
        return complete(Bad.of(failure));
    }
    
    default OrPromise<G,B> success(G success) {
        return complete(Good.of(success));
    }
    
    default boolean tryFailure(B failure) {
        return tryComplete(Bad.of(failure));
    }
    
    default boolean trySuccess(G success) {
        return tryComplete(Good.of(success));
    }
}

final class OrPromiseImpl<G,B> implements OrPromise<G,B> {

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
