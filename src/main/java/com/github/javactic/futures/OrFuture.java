package com.github.javactic.futures;

import com.github.javactic.Accumulation;
import com.github.javactic.Every;
import com.github.javactic.One;
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
import javaslang.collection.Iterator;
import javaslang.collection.Vector;
import javaslang.control.Option;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.javactic.futures.Helper.accumulate;
import static com.github.javactic.futures.Helper.withPromise;

public interface OrFuture<G, B> {

  // -- constructors --

  static <G, B> OrFuture<G, B> of(ExecutorService executor, Supplier<? extends Or<? extends G, ? extends B>> orSupplier) {
    Objects.requireNonNull(orSupplier, "orSupplier is null");
    final OrFutureImpl<G, B> future = new OrFutureImpl<>(executor);
    future.run(orSupplier);
    return future;
  }

  static <G, B> OrFuture<G, B> of(Supplier<? extends Or<? extends G, ? extends B>> orSupplier) {
    return of(ForkJoinPool.commonPool(), orSupplier);
  }

  static <G, B> OrFuture<G, B> ofBad(B bad) {
    return OrPromise.<G, B>create().bad(bad).future();
  }

  static <G, B> OrFuture<G, One<B>> ofOneBad(B bad) {
    return OrPromise.<G, One<B>>create().bad(One.of(bad)).future();
  }

  static <G, B> OrFuture<G, B> ofGood(G good) {
    return OrPromise.<G, B>create().good(good).future();
  }

  // -- functor stuff --

  static <F, G, ERR> OrFuture<Vector<G>, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> f) {
    return validatedBy(iterable, f, Vector.collector());
  }

  static <F, G, A, I extends Iterable<? extends G>, ERR> OrFuture<I, Every<ERR>>
  validatedBy(Iterable<? extends F> iterable,
              Function<? super F, ? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> f,
              Collector<? super G, A, I> collector) {

    Vector<OrFuture<? extends G, ? extends Every<? extends ERR>>> futures =
      Iterator
        .ofAll(iterable)
        .foldLeft(Vector.empty(), (vec, elem) -> vec.append(f.apply(elem)));
    return combined(futures, collector);
  }

  @SafeVarargs
  static <G, ERR> OrFuture<G, Every<ERR>>
  when(OrFuture<? extends G, ? extends Every<? extends ERR>> or,
       Function<? super G, ? extends Validation<ERR>>... validations) {

    OrPromise<G, Every<ERR>> promise = OrPromise.create();
    or.onComplete(o -> promise.complete(Accumulation.when(o, validations)));
    return promise.future();
  }

  static <A, B, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           BiFunction<? super A, ? super B, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          promise.complete(Accumulation.withGood(ora, orb, function))))
    );
  }

  static <A, B, C, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           Function3<? super A, ? super B, ? super C, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            promise.complete(Accumulation.withGood(ora, orb, orc, function)))))
    );
  }

  static <A, B, C, D, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           Function4<? super A, ? super B, ? super C, ? super D, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              promise.complete(Accumulation.withGood(ora, orb, orc, ord, function))))))
    );
  }

  static <A, B, C, D, E, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           Function5<? super A, ? super B, ? super C, ? super D, ? super E, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, function)))))))
    );
  }

  static <A, B, C, D, E, F, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           Function6<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, function))))))))
    );
  }

  static <A, B, C, D, E, F, G, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           Function7<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? extends RESULT> function) {
    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  fg.onComplete(org ->
                    promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, function)))))))))
    );
  }

  static <A, B, C, D, E, F, G, H, ERR, RESULT> OrFuture<RESULT, Every<ERR>>
  withGood(OrFuture<? extends A, ? extends Every<? extends ERR>> fa,
           OrFuture<? extends B, ? extends Every<? extends ERR>> fb,
           OrFuture<? extends C, ? extends Every<? extends ERR>> fc,
           OrFuture<? extends D, ? extends Every<? extends ERR>> fd,
           OrFuture<? extends E, ? extends Every<? extends ERR>> fe,
           OrFuture<? extends F, ? extends Every<? extends ERR>> ff,
           OrFuture<? extends G, ? extends Every<? extends ERR>> fg,
           OrFuture<? extends H, ? extends Every<? extends ERR>> fh,
           Function8<? super A, ? super B, ? super C, ? super D, ? super E, ? super F, ? super G, ? super H, ? extends RESULT> function) {

    return withPromise(promise ->
      fa.onComplete(ora ->
        fb.onComplete(orb ->
          fc.onComplete(orc ->
            fd.onComplete(ord ->
              fe.onComplete(ore ->
                ff.onComplete(orf ->
                  fg.onComplete(org ->
                    fh.onComplete(orh ->
                      promise.complete(Accumulation.withGood(ora, orb, orc, ord, ore, orf, org, orh, function)))))))))));
  }

  // ---------------------------------- COMBINED ------------------------------------------------

  static <G, ERR> OrFuture<Vector<G>, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input) {
    return combined(input, Vector.collector());
  }

  static <G, ERR, A, I extends Iterable<? extends G>> OrFuture<I, Every<ERR>>
  combined(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> input,
           Collector<? super G, A, I> collector) {

    OrPromise<I, Every<ERR>> promise = OrPromise.create();
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean finished = new AtomicBoolean(false);
    java.util.Iterator<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> iterator = input.iterator();
    while(iterator.hasNext()) {
      count.incrementAndGet();
      OrFuture<? extends G, ? extends Every<? extends ERR>> future = iterator.next();
      if (!iterator.hasNext()) {
        finished.set(true);
      }
      future.onComplete(or -> {
        int c = count.decrementAndGet();
        if (finished.get() && c == 0) {
          promise.complete(accumulate(input, collector));
        }
      });
    }
    return promise.future();
  }

  // ----------------------------------- ZIPS ---------------------------------------------------

  static <A, B, ERR> OrFuture<Tuple2<A, B>, Every<ERR>>
  zip(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
      OrFuture<? extends B, ? extends Every<? extends ERR>> b) {
    return withGood(a, b, Tuple::of);
  }

  static <A, B, C, ERR> OrFuture<Tuple3<A, B, C>, Every<ERR>>
  zip3(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c) {
    return withGood(a, b, c, Tuple::of);
  }

  static <A, B, C, D, ERR> OrFuture<Tuple4<A, B, C, D>, Every<ERR>>
  zip4(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c,
       OrFuture<? extends D, ? extends Every<? extends ERR>> d) {
    return withGood(a, b, c, d, Tuple::of);
  }

  static <A, B, C, D, E, ERR> OrFuture<Tuple5<A, B, C, D, E>, Every<ERR>>
  zip5(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c,
       OrFuture<? extends D, ? extends Every<? extends ERR>> d,
       OrFuture<? extends E, ? extends Every<? extends ERR>> e) {
    return withGood(a, b, c, d, e, Tuple::of);
  }

  static <A, B, C, D, E, F, ERR> OrFuture<Tuple6<A, B, C, D, E, F>, Every<ERR>>
  zip6(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c,
       OrFuture<? extends D, ? extends Every<? extends ERR>> d,
       OrFuture<? extends E, ? extends Every<? extends ERR>> e,
       OrFuture<? extends F, ? extends Every<? extends ERR>> f) {
    return withGood(a, b, c, d, e, f, Tuple::of);
  }

  static <A, B, C, D, E, F, G, ERR> OrFuture<Tuple7<A, B, C, D, E, F, G>, Every<ERR>>
  zip7(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c,
       OrFuture<? extends D, ? extends Every<? extends ERR>> d,
       OrFuture<? extends E, ? extends Every<? extends ERR>> e,
       OrFuture<? extends F, ? extends Every<? extends ERR>> f,
       OrFuture<? extends G, ? extends Every<? extends ERR>> g) {
    return withGood(a, b, c, d, e, f, g, Tuple::of);
  }

  static <A, B, C, D, E, F, G, H, ERR> OrFuture<Tuple8<A, B, C, D, E, F, G, H>, Every<ERR>>
  zip8(OrFuture<? extends A, ? extends Every<? extends ERR>> a,
       OrFuture<? extends B, ? extends Every<? extends ERR>> b,
       OrFuture<? extends C, ? extends Every<? extends ERR>> c,
       OrFuture<? extends D, ? extends Every<? extends ERR>> d,
       OrFuture<? extends E, ? extends Every<? extends ERR>> e,
       OrFuture<? extends F, ? extends Every<? extends ERR>> f,
       OrFuture<? extends G, ? extends Every<? extends ERR>> g,
       OrFuture<? extends H, ? extends Every<? extends ERR>> h) {
    return withGood(a, b, c, d, e, f, g, h, Tuple::of);
  }

  // -- instance --

  boolean isCompleted();

  void onComplete(Consumer<? super Or<G, B>> result);

  Option<Or<G, B>> value();

  Or<G, B> result(Duration timeout) throws TimeoutException, InterruptedException, ExecutionException;

  Or<G, B> result(Duration timeout, B timeoutBad) throws InterruptedException;

  default OrFuture<G, One<B>> accumulating() {
    return transform(Function.identity(), One::of);
  }

  default OrFuture<G, B> filter(Function<? super G, ? extends Validation<? extends B>> validator) {
    Objects.requireNonNull(validator, "validator is null");
    OrPromise<G, B> promise = OrPromise.create();
    onComplete(result -> promise.complete(result.filter(validator)));
    return promise.future();
  }

  default <H> OrFuture<H, B> map(Function<? super G, ? extends H> mapper) {
    Objects.requireNonNull(mapper, "mapper is null");
    OrPromise<H, B> promise = OrPromise.create();
    onComplete(result -> promise.complete(result.map(mapper)));
    return promise.future();
  }

  default <H> OrFuture<H, B> flatMap(Function<? super G, ? extends OrFuture<? extends H, ? extends B>> mapper) {
    Objects.requireNonNull(mapper, "mapper is null");
    OrPromise<H, B> promise = OrPromise.create();
    onComplete(orResult -> orResult.forEach(
      g -> promise.completeWith(mapper.apply(g)),
      promise::bad));
    return promise.future();
  }

  default OrFuture<G, B> recover(Function<? super B, ? extends G> fn) {
    Objects.requireNonNull(fn, "fn is null");
    OrPromise<G, B> promise = OrPromise.create();
    onComplete(orResult -> promise.complete(orResult.recover(fn)));
    return promise.future();
  }

  default <C> OrFuture<G, C> recoverWith(Function<? super B, ? extends OrFuture<? extends G, ? extends C>> fn) {
    Objects.requireNonNull(fn, "fn is null");
    OrPromise<G, C> promise = OrPromise.create();
    onComplete(orResult -> orResult.forEach(
      promise::good,
      b -> promise.completeWith(fn.apply(b))));
    return promise.future();
  }

  default <H, C> OrFuture<H, C> transform(Function<? super G, ? extends H> s, Function<? super B, ? extends C> f) {
    Objects.requireNonNull(s, "s is null");
    Objects.requireNonNull(f, "f is null");
    OrPromise<H, C> promise = OrPromise.create();
    onComplete(orResult -> promise.complete(orResult.transform(s, f)));
    return promise.future();
  }

}
