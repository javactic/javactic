package com.github.javactic.futures;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.javactic.Or;
import com.github.javactic.Validation;

import javaslang.control.Option;

public interface OrFuture<G,B> {

    static <G,B> OrFuture<G,B> of(Supplier<? extends Or<? extends G,? extends B>> orSupplier) {
        Objects.requireNonNull(orSupplier, "orSupplier is null");
        final OrFutureImpl<G,B> future = new OrFutureImpl<>();
        future.run(orSupplier);
        return future;
    }
    
    static <G,B> OrFuture<G,B> failed(B bad) {
        return OrPromise.<G,B>make().failure(bad).future();
    }
    
    static <G,B> OrFuture<G,B> successful(G good) {
        return OrPromise.<G,B>make().success(good).future();
    }

    // -- instance --
    
    boolean isCompleted();
    
    void onComplete(Consumer<? super Or<G,B>> result);
    
    Option<Or<G,B>> value();
    
    Or<G,B> result(Duration timeout) throws TimeoutException, InterruptedException, ExecutionException;
    
    Or<G,B> result(Duration timeout, B timeoutBad) throws InterruptedException;
    
    default OrFuture<G,B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
        Objects.requireNonNull(validator, "validator is null");
        OrPromise<G,B> promise = OrPromise.make();
        onComplete(result -> promise.complete(result.filter(validator)));
        return promise.future();
    }
    
    default <H> OrFuture<H,B> map(Function<? super G,? extends H> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        OrPromise<H,B> promise = OrPromise.make();
        onComplete(result -> promise.complete(result.map(mapper)));
        return promise.future();
    }
    
    default <H> OrFuture<H,B> flatMap(Function<? super G,? extends OrFuture<? extends H, ? extends B>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        OrPromise<H,B> promise = OrPromise.make();
        onComplete(orResult -> orResult.forEach(
                g -> promise.completeWith(mapper.apply(g)), 
                b -> promise.failure(b)));
        return promise.future();        
    }
    
    default OrFuture<G,B> recover(Function<? super B, ? extends G> fn) {
        Objects.requireNonNull(fn, "fn is null");
        OrPromise<G,B> promise = OrPromise.make();
        onComplete(orResult -> promise.complete(orResult.recover(fn)));
        return promise.future();
    }
    
    default <C> OrFuture<G,C> recoverWith(Function<? super B, ? extends OrFuture<? extends G, ? extends C>> fn) {
        Objects.requireNonNull(fn, "fn is null");
        OrPromise<G,C> promise = OrPromise.make();
        onComplete(orResult -> orResult.forEach(
                promise::success, 
                b -> promise.completeWith(fn.apply(b))));
        return promise.future();
    }
    
    default <H,C> OrFuture<H,C> transform(Function<? super G, ? extends H> s, Function<? super B, ? extends C> f) {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(f, "f is null");
        OrPromise<H,C> promise = OrPromise.make();
        onComplete(orResult -> promise.complete(orResult.transform(s, f)));
        return promise.future();
    }
    
}
