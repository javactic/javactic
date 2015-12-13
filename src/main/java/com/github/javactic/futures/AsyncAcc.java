package com.github.javactic.futures;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiFunction;
import java.util.stream.Collector;

import com.github.javactic.Accumulation;
import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;

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
import javaslang.collection.Seq;
import javaslang.collection.Vector;

public class AsyncAcc {
    
    private AsyncAcc() {}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <A, B, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
            withGood(OrFuture<? extends A, ? extends ERR> fa,
                     OrFuture<? extends B, ? extends ERR> fb,
                     BiFunction<? super A, ? super B, ? extends RESULT> function) {
        OrFuture[] functions = { fa, fb };
        AtomicReferenceArray<Or<?, ? extends ERR>> results = new AtomicReferenceArray<>(functions.length);
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> {
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc = Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], ors[6], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
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
            Or[] ors = asAcc(results);
            Or<RESULT, Every<ERR>> acc =
                    Accumulation.withGood(ors[0], ors[1], ors[2], ors[3], ors[4], ors[5], ors[6], ors[7], function);
            promise.complete(acc);
        };
        combiner(results, combiner, functions);
        return promise.future();
    }

    // ---------------------------------- COMBINED ------------------------------------------------
    
    public static <G, A, ERR, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
            combined(Seq<? extends OrFuture<? extends G, ? extends ERR>> input, Collector<? super G, A, I> collector) {
        int length = input.length();
        AtomicReferenceArray<Or<? extends G, ? extends ERR>> results = new AtomicReferenceArray<>(length);
        OrPromise<I, Every<ERR>> promise = OrPromise.make();
        Runnable combiner = () -> promise.complete(accumulate(results, collector));
        AtomicInteger counter = new AtomicInteger(length);
        input.foldLeft(0, (z, f) -> {
            combine(f, results, counter, z, combiner);
            return z + 1;
        });
        return promise.future();
    }

    public static <G, B> OrFuture<Vector<G>, Every<B>>
            combined(Seq<? extends OrFuture<? extends G, ? extends B>> input) {
        return combined(input, Vector.collector());
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

    // ---------------------------------- HELPERS -------------------------------------------------

    @SuppressWarnings("rawtypes")
    private static <ERR> Or[] asAcc(AtomicReferenceArray<Or<?, ? extends ERR>> results) {
        Or[] result = new Or[results.length()];
        for (int i = 0; i < results.length(); i++) {
            result[i] = results.get(i).accumulating();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <ERR> void combiner(AtomicReferenceArray<Or<?, ? extends ERR>> results,
                                       Runnable combiner,
                                       OrFuture<?, ? extends ERR>... fs) {
        AtomicInteger counter = new AtomicInteger(fs.length);
        for (int i = 0; i < fs.length; i++) {
            combine(fs[i], results, counter, i, combiner);
        }
    }
    
    private static <G, B> void combine(OrFuture<? extends G, ? extends B> future,
                                       AtomicReferenceArray<Or<? extends G, ? extends B>> results,
                                       AtomicInteger counter,
                                       int myIndex,
                                       Runnable combiner) {
        future.onComplete(or -> {
            results.set(myIndex, or);
            if (counter.decrementAndGet() == 0) {
                combiner.run();
            }
        });
    }

    private static <G, A, ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>>
            accumulate(AtomicReferenceArray<Or<? extends G, ? extends ERR>> ors, Collector<? super G, A, I> collector) {
        A goods = collector.supplier().get();
        Vector<ERR> bads = Vector.empty();
        for (int i = 0; i < ors.length(); i++) {
            Or<? extends G, ? extends ERR> or = ors.get(i);
            if (or.isGood() && bads.isEmpty())
                collector.accumulator().accept(goods, or.get());
            if (or.isBad())
                bads = bads.append(or.getBad());
        }

        if (bads.isEmpty()) {
            I gds = collector.finisher().apply(goods);
            return Good.of(gds);
        } else
            return Bad.of(Every.of(bads.head(), bads.tail()));
    }

}
