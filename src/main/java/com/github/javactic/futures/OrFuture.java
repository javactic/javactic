package com.github.javactic.futures;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.github.javactic.Accumulation;
import com.github.javactic.Every;
import com.github.javactic.Or;
import com.github.javactic.Validation;

import javaslang.Function3;
import javaslang.Function4;
import javaslang.Function5;
import javaslang.Function6;
import javaslang.Function7;
import javaslang.Function8;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.Tuple5;
import javaslang.Tuple6;
import javaslang.Tuple7;
import javaslang.Tuple8;
import javaslang.collection.Vector;
import javaslang.control.Option;

public interface OrFuture<G,B> {

    // -- constructors --
    
    static <G,B> OrFuture<G,B> of(ExecutorService executor, Supplier<? extends Or<? extends G,? extends B>> orSupplier) {
        Objects.requireNonNull(orSupplier, "orSupplier is null");
        final OrFutureImpl<G,B> future = new OrFutureImpl<>(executor);
        future.run(orSupplier);
        return future;
    }
    
    static <G,B> OrFuture<G,B> of(Supplier<? extends Or<? extends G,? extends B>> orSupplier) {
        return of(ForkJoinPool.commonPool(), orSupplier);
    }

    static <G,B> OrFuture<G,B> failed(B bad) {
        return OrPromise.<G,B>make().failure(bad).future();
    }
    
    static <G,B> OrFuture<G,B> successful(G good) {
        return OrPromise.<G,B>make().success(good).future();
    }

    // -- functor stuff --
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     BiFunction<? super A, ? super B, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     Function3<? super A, ? super B, ? super C, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     OrFuture<? extends D, ? extends ERR> fd,
                     Function4<? super A, ? super B, ? super C, ? super D, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc, fd };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D, E, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     OrFuture<? extends D, ? extends ERR> fd,
                     OrFuture<? extends E, ? extends ERR> fe,
                     Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc, fd, fe };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D, E, F, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     OrFuture<? extends D, ? extends ERR> fd,
                     OrFuture<? extends E, ? extends ERR> fe,
                     OrFuture<? extends F, ? extends ERR> ff,
                     Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc, fd, fe, ff };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D, E, F, G, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     OrFuture<? extends D, ? extends ERR> fd,
                     OrFuture<? extends E, ? extends ERR> fe,
                     OrFuture<? extends F, ? extends ERR> ff,
                     OrFuture<? extends G, ? extends ERR> fg,
                     Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc, fd, fe, ff, fg };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], ors[6], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, C, D, E, F, G, H, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     OrFuture<? extends C, ? extends ERR> fc,
                     OrFuture<? extends D, ? extends ERR> fd,
                     OrFuture<? extends E, ? extends ERR> fe,
                     OrFuture<? extends F, ? extends ERR> ff,
                     OrFuture<? extends G, ? extends ERR> fg,
                     OrFuture<? extends H, ? extends ERR> fh,
                     Function8<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? super H, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb, fc, fd, fe, ff, fg, fh };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = Helper.asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], ors[6], ors[7], function);
            promise.complete(acc);
        };
        Helper.combiner(results, combiner, functions);
        return promise.future();
    }

    // ---------------------------------- COMBINED ------------------------------------------------

    public static <G, ERR> OrFuture<Vector<G>, Every<ERR>>
            combined(Iterable<? extends OrFuture<? extends G, ? extends ERR>> input) {
        return combined(input, Vector.collector());
    }

    public static <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
        combined(Iterable<? extends OrFuture<? extends G, ? extends ERR>> input, Collector<? super G, A, I> collector) {
        
        Queue<Or<? extends G, ? extends ERR>> results = new ConcurrentLinkedQueue<>();
        OrPromise<I, Every<ERR>> promise = OrPromise.make();
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean lastSet = new AtomicBoolean(false);
        for (Iterator<? extends OrFuture<? extends G, ? extends ERR>> iterator = input.iterator(); iterator.hasNext();) {
            count.incrementAndGet();
            OrFuture<? extends G, ? extends ERR> future = iterator.next();
            if(!iterator.hasNext()) {
                lastSet.set(true);
            }
            future.onComplete(or -> {
                int c = count.decrementAndGet();
                results.add(or);
                if(lastSet.get()) {
                    if(c == 0) {
                        promise.complete(Helper.accumulate(results, collector));
                    }
                }
            });
        }
        return promise.future();
    }
    
    // ----------------------------------- ZIPS ---------------------------------------------------
    
    public static <A, B, ERR> OrFuture<Tuple2<A, B>, Every<ERR>> zip(OrFuture<? extends A, ? extends ERR> a,
                                                                     OrFuture<? extends B, ? extends ERR> b) {
        return withGood(a, b, Tuple::of);
    }

    public static <A, B, C, ERR> OrFuture<Tuple3<A, B, C>, Every<ERR>> zip3(OrFuture<? extends A, ? extends ERR> a,
                                                                            OrFuture<? extends B, ? extends ERR> b,
                                                                            OrFuture<? extends C, ? extends ERR> c) {
        return withGood(a, b, c, Tuple::of);
    }

    public static <A, B, C, D, ERR> OrFuture<Tuple4<A, B, C, D>, Every<ERR>>
            zip4(OrFuture<? extends A, ? extends ERR> a,
                 OrFuture<? extends B, ? extends ERR> b,
                 OrFuture<? extends C, ? extends ERR> c,
                 OrFuture<? extends D, ? extends ERR> d) {
        return withGood(a, b, c, d, Tuple::of);
    }

    public static <A, B, C, D, E, ERR> OrFuture<Tuple5<A, B, C, D, E>, Every<ERR>>
            zip5(OrFuture<? extends A, ? extends ERR> a,
                 OrFuture<? extends B, ? extends ERR> b,
                 OrFuture<? extends C, ? extends ERR> c,
                 OrFuture<? extends D, ? extends ERR> d,
                 OrFuture<? extends E, ? extends ERR> e) {
        return withGood(a, b, c, d, e, Tuple::of);
    }

    public static <A, B, C, D, E, F, ERR> OrFuture<Tuple6<A, B, C, D, E, F>, Every<ERR>>
            zip6(OrFuture<? extends A, ? extends ERR> a,
                 OrFuture<? extends B, ? extends ERR> b,
                 OrFuture<? extends C, ? extends ERR> c,
                 OrFuture<? extends D, ? extends ERR> d,
                 OrFuture<? extends E, ? extends ERR> e,
                 OrFuture<? extends F, ? extends ERR> f) {
        return withGood(a, b, c, d, e, f, Tuple::of);
    }

    public static <A, B, C, D, E, F, G, ERR> OrFuture<Tuple7<A, B, C, D, E, F, G>, Every<ERR>>
            zip7(OrFuture<? extends A, ? extends ERR> a,
                 OrFuture<? extends B, ? extends ERR> b,
                 OrFuture<? extends C, ? extends ERR> c,
                 OrFuture<? extends D, ? extends ERR> d,
                 OrFuture<? extends E, ? extends ERR> e,
                 OrFuture<? extends F, ? extends ERR> f,
                 OrFuture<? extends G, ? extends ERR> g) {
        return withGood(a, b, c, d, e, f, g, Tuple::of);
    }

    public static <A, B, C, D, E, F, G, H, ERR> OrFuture<Tuple8<A, B, C, D, E, F, G, H>, Every<ERR>>
            zip8(OrFuture<? extends A, ? extends ERR> a,
                 OrFuture<? extends B, ? extends ERR> b,
                 OrFuture<? extends C, ? extends ERR> c,
                 OrFuture<? extends D, ? extends ERR> d,
                 OrFuture<? extends E, ? extends ERR> e,
                 OrFuture<? extends F, ? extends ERR> f,
                 OrFuture<? extends G, ? extends ERR> g,
                 OrFuture<? extends H, ? extends ERR> h) {
        return withGood(a, b, c, d, e, f, g, h, Tuple::of);
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
