package com.github.javactic.futures;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collector;

import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;

import javaslang.collection.Vector;

class Helper {
    
    private Helper() {}

    // ---------------------------------- HELPERS -------------------------------------------------

    static <RESULT, ERR> OrFuture<RESULT, Every<ERR>> withPromise(Consumer<? super OrPromise<RESULT, Every<ERR>>> consumer) {
        OrPromise<RESULT, Every<ERR>> promise = OrPromise.create();
        consumer.accept(promise);
        return promise.future();
    }
    
    static <G, A, ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>>
            accumulate(Queue<Or<? extends G, ? extends Every<? extends ERR>>> ors, Collector<? super G, A, I> collector) {
        A goods = collector.supplier().get();
        Vector<ERR> bads = Vector.empty();
        for (Or<? extends G,? extends Every<? extends ERR>> or : ors) {
            if (or.isGood() && bads.isEmpty())
                collector.accumulator().accept(goods, or.get());
            if (or.isBad())
                bads = bads.appendAll(or.getBad());
        }

        if (bads.isEmpty()) {
            I gds = collector.finisher().apply(goods);
            return Good.of(gds);
        } else
            return Bad.of(Every.of(bads.head(), bads.tail()));
    }

}
