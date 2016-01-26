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
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;
import javaslang.collection.Vector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collector;

class Helper {

  /**
   * The default executor service {@link Executors#newCachedThreadPool()}.
   */
  static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  private Helper() {}

  // ---------------------------------- HELPERS -------------------------------------------------

  static <RESULT, ERR> OrFuture<RESULT, Every<ERR>> withPromise(Consumer<? super OrPromise<RESULT, Every<ERR>>> consumer) {
    OrPromise<RESULT, Every<ERR>> promise = OrPromise.create();
    consumer.accept(promise);
    return promise.future();
  }

  static <G, A, ERR, I extends Iterable<? extends G>> Or<I, Every<ERR>>
  accumulate(Iterable<? extends OrFuture<? extends G, ? extends Every<? extends ERR>>> ors,
             Collector<? super G, A, I> collector) {
    A goods = collector.supplier().get();
    Vector<ERR> bads = Vector.empty();
    for (OrFuture<? extends G, ? extends Every<? extends ERR>> orFuture : ors) {
      Or<? extends G, ? extends Every<? extends ERR>> or = orFuture.getOption().get();
      if (or.isGood() && bads.isEmpty())
        collector.accumulator().accept(goods, or.get());
      if (or.isBad())
        bads = bads.appendAll(or.getBad());
    }

    if (bads.isEmpty())
      return Good.of(collector.finisher().apply(goods));
    else
      return Bad.of(Every.of(bads.head(), bads.tail()));
  }

}
