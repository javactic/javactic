package com.github.javactic.futures;

import java.time.Duration;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.javactic.Bad;
import com.github.javactic.Or;

import javaslang.control.Option;
import javaslang.control.Try;

class OrFutureImpl<G,B> implements OrFuture<G, B> {

    private final ExecutorService executor;
    private final AtomicReference<Or<G,B>> value = new AtomicReference<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final CountDownLatch finished = new CountDownLatch(1);
    private final Queue<Consumer<? super Or<G,B>>> actions = new ConcurrentLinkedQueue<>();
    @SuppressWarnings("unused")
    private volatile Future<Or<G,B>> job = null;

    public OrFutureImpl(ExecutorService executor) {
        this.executor = executor;
    }
    
    @SuppressWarnings("unchecked")
    boolean tryComplete(Or<? extends G, ? extends B>  value) {
        Objects.requireNonNull(value, "value is null");
        return complete((Or<G, B>) value) != null;
    }
    
    @Override
    public boolean isCompleted() {
        return value.get() != null;
    }
    
    @SuppressWarnings("unchecked")
    void run(Supplier<? extends Or<? extends G,? extends B>> orSupplier) {
        if (isCompleted()) {
            throw new IllegalStateException("the future is already completed.");
        } else if(started.compareAndSet(false, true)) {
            // we got the right to start the job
            job = executor.submit(() -> complete((Or<G, B>)orSupplier.get()));
        } else {
            throw new IllegalStateException("the future is already running.");
        }
    }

    Or<G,B> complete(Or<G,B> result) {
        Objects.requireNonNull(result, "cannot complete with null");
        // sync necessary so onComplete does not get called twice for an action
        synchronized (actions) {
            if(value.compareAndSet(null, result)) {
                finished.countDown();
                actions.forEach(this::perform);
                return result;
            } else {
                return null;
            }
        }
    }
    
    private void perform(Consumer<? super Or<G, B>> action) {
        Try.run(() -> executor.execute(() -> action.accept(value.get())));
    }
    
    @Override
    public void onComplete(Consumer<? super Or<G,B>> action) {
        Objects.requireNonNull(action, "action is null");
        // sync necessary so onComplete does not get called twice for an action
        synchronized (actions) {
            actions.add(action);
            if (isCompleted()) {
                perform(action);
            }
        }
    }

    @Override
    public Option<Or<G, B>> value() {
        return isCompleted() ? Option.some(value.get()) :  Option.none();
    }

    @Override
    public Or<G, B> result(Duration timeout) throws InterruptedException, TimeoutException {
        if(finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) return value.get();
        else throw new TimeoutException("timeout waiting for result");
    }
    
    @Override
    public Or<G, B> result(Duration timeout, B timeoutBad) throws InterruptedException {
        if(finished.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) return value.get();
        else return Bad.of(timeoutBad);
    }
    
}
