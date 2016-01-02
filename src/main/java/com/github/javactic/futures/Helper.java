package com.github.javactic.futures;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collector;

import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;

import javaslang.collection.Vector;

class Helper {
    
    private Helper() {}

    // ---------------------------------- HELPERS -------------------------------------------------

    @SuppressWarnings("rawtypes")
    static <ERR> Or[] asAcc(AtomicReferenceArray<Or<?, ? extends ERR>> results) {
        Or[] result = new Or[results.length()];
        for (int i = 0; i < results.length(); i++) {
            result[i] = results.get(i).accumulating();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static <ERR> void combiner(AtomicReferenceArray<Or<?, ? extends ERR>> results,
                                       Runnable combiner,
                                       OrFuture<?, ? extends ERR>... fs) {
        AtomicInteger counter = new AtomicInteger(fs.length);
        for (int i = 0; i < fs.length; i++) {
            combine(fs[i], results, counter, i, combiner);
        }
    }
    
    static <G, B> void combine(OrFuture<? extends G, ? extends B> future,
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

    static <G, A, ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>>
            accumulate(Queue<Or<? extends G, ? extends ERR>> ors, Collector<? super G, A, I> collector) {
        A goods = collector.supplier().get();
        Vector<ERR> bads = Vector.empty();
        for (Or<? extends G,? extends ERR> or : ors) {
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
