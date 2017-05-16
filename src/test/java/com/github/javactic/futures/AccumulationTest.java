package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.One;
import com.github.javactic.Or;
import com.github.javactic.Pass;
import com.github.javactic.Validation;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(Theories.class)
public class AccumulationTest {

  @DataPoints
  public static Executor[] configs = {Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool()};

  @Theory
  public void withGoodFail(Executor ex) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, ex);
    OrFuture<String, One<String>> success = ctx.goodFuture("success").accumulating();
    OrFuture<String, One<String>> fail = ctx.<String>badFuture("failure").accumulating();
    OrFuture<String, Every<String>> result = ctx.withGood(success, fail, (a1, a2) -> "doesn't matter");
    Or<String, Every<String>> or = result.get(Duration.ofSeconds(10));
    assertEquals("failure", or.getBad().head());
  }

  @Theory
  public void withGoodSuccess(Executor ex) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, ex);
    OrFuture<String, One<String>> s1 = ctx.goodFuture("A").accumulating();
    OrFuture<Integer, One<String>> s2 = ctx.goodFuture(1).accumulating();
    OrFuture<String, Every<String>> result = ctx.withGood(s1, s2, (a1, a2) -> a1 + a2);
    Or<String, Every<String>> or = result.get(Duration.ofSeconds(10));
    assertEquals("A1", or.get());
  }

  @Theory
  public void sequenceIterator(Executor ex) {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, ex);
    OrFuture<String, One<String>> f1 = ctx.future(() -> Good.of("1")).accumulating();
    OrFuture<String, One<String>> f2 = ctx.future(() -> Bad.<String,String>of("2")).accumulating();
    OrFuture<Vector<String>, Every<String>> sequence = ctx.sequence(Iterator.of(f1, f2));
    Or<Vector<String>, Every<String>> or = sequence.getUnsafe();
    String fold = or.getBad().foldLeft("", (s, i) -> s + i);
    assertEquals("2", fold);
  }

  @Theory
  public void sequenceSuccess(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    Seq<OrFuture<Integer, One<String>>> seq = Vector.empty();
    for (int i = 0; i < 10; i++) {
      final int fi = i;
      seq = seq.append(ctx.future(() -> Good.of(fi)).accumulating());
    }
    OrFuture<Vector<Integer>, Every<String>> sequence = ctx.sequence(seq);
    Or<Vector<Integer>, Every<String>> or = sequence.get(Duration.ofSeconds(10));
    Assert.assertTrue(or.isGood());
    String fold = or.get().foldLeft("", (s, i) -> s + i);
    assertEquals("0123456789", fold);
  }

  @Theory
  public void sequenceFailure(Executor es) throws Exception {
    ExecutionContext<One<String>> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es).accumulating();
    Seq<OrFuture<Integer, One<String>>> seq = Vector.empty();
    for (int i = 0; i < 10; i++) {
      final int fi = i;
      if (i % 2 == 0)
        seq = seq.append(ctx.future(() -> Good.of(fi)));
      else
        seq = seq.append(ctx.future(() -> Bad.ofOne(fi+ "")));
    }
    OrFuture<Vector<Integer>, Every<String>> sequence = ctx.sequence(seq);
    Or<Vector<Integer>, Every<String>> or = sequence.get(Duration.ofSeconds(10));
    Assert.assertTrue(or.isBad());
    String fold = or.getBad().foldLeft("", (s, i) -> s + i);
    assertEquals(1, fold.length());
  }

  @Theory
  public void combinedIterator(Executor es) {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<String, One<String>> f1 = ctx.future(() -> Good.of("1")).accumulating();
    OrFuture<String, One<String>> f2 = ctx.future(() -> Good.of("2")).accumulating();
    OrFuture<Vector<String>, Every<String>> combined = ctx.combined(Iterator.of(f1, f2));
    Or<Vector<String>, Every<String>> or = combined.getUnsafe();
    String fold = or.get().foldLeft("", (s, i) -> s + i);
    assertEquals("12", fold);
  }

  @Test
  public void combinedSecondFinishesFirst() throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newCachedThreadPool());
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, One<String>> f1 = ctx.future(() -> {
      try {
        latch.await();
      } catch (Exception e) {
        Assert.fail();
      }
      return Good.of("1");
    }).accumulating();
    OrFuture<String, One<String>> f2 = ctx.future(() -> Good.of("2")).accumulating();
    OrFuture<String, One<String>> f3 = ctx.future(() -> Good.of("3")).accumulating();
    OrFuture<String, One<String>> f4 = ctx.future(() -> Good.of("4")).accumulating();
    OrFuture<Vector<String>, Every<String>> combined = ctx.combined(Vector.of(f1, f2, f3, f4));
    f4.onComplete(or -> latch.countDown());
    Or<Vector<String>, Every<String>> or = combined.get(Duration.ofSeconds(10));
    Assert.assertTrue(or.isGood());
    String fold = or.get().foldLeft("", (s, i) -> s + i);
    assertEquals("1234", fold);
  }

  @Test
  public void combinedSecondFinishesLast() throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newCachedThreadPool());
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, One<String>> f1 = ctx.future(() -> Good.<String,String>of("1")).accumulating();
    OrFuture<String, One<String>> f2 = ctx.future(() -> Good.of("2")).accumulating();
    OrFuture<String, One<String>> f3 = ctx.future(() -> Good.of("3")).accumulating();
    OrFuture<String, One<String>> f4 = ctx.future(() -> {
      try {
        latch.await();
      } catch (Exception e) {
        Assert.fail();
      }
      return Good.of("4");
    }).accumulating();
    OrFuture<Vector<String>, Every<String>> combined = ctx.combined(Vector.of(f1, f2, f3, f4));
    f1.onComplete(or -> latch.countDown());
    Or<Vector<String>, Every<String>> or = combined.get(Duration.ofSeconds(10));
    Assert.assertTrue(or.isGood());
    String fold = or.get().foldLeft("", (s, i) -> s + i);
    assertEquals("1234", fold);
  }

  @Test
  public void combined() throws TimeoutException, InterruptedException {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newCachedThreadPool());
    int total = 0;
    for (int i = 0; i < 50; i++) {
      total += testCombined(ctx);
    }
  }

  private int testCombined(ExecutionContext<String> ctx) throws TimeoutException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    int size = ThreadLocalRandom.current().nextInt(40,100);
    Vector<OrFuture<String, One<String>>> vector = Vector.empty();
    for (int i = 0; i < size; i++) {
      vector = vector.append(createRandomWaitingFuture(ctx, latch));
    }
    vector = vector.append(ctx.future(() -> Good.of("direct")).accumulating());
    OrFuture<Vector<String>, Every<String>> combined = ctx.combined(vector);
    vector.last().onComplete(stringOneOr -> {
      latch.countDown();
    });
    Or<Vector<String>, Every<String>> or = combined.get(Duration.ofSeconds(20));
    Assert.assertTrue(or.isGood());
    return size;
  }

  private OrFuture<String, One<String>> createRandomWaitingFuture(ExecutionContext<String> ctx, CountDownLatch latch) {
    if (ThreadLocalRandom.current().nextBoolean()) {
      return ctx.future(() -> {
        try {
          latch.await();
        } catch (Exception e) {
          Assert.fail();
        }
        return Good.of("waiting");
      }).accumulating();
    } else {
      return ctx.future(() -> Good.<String, String>of("direct")).accumulating();
    }
  }

  @Theory
  public void validatedBy(Executor es) throws InterruptedException, ExecutionException, TimeoutException {
    ExecutionContext<One<String>> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es).accumulating();
    Vector<Integer> vec = Vector.of(1,2,3,4);
    Function<Integer, OrFuture<Integer, One<String>>> f = i ->
      ctx.future(() -> {
        if(i < 10) return Good.of(i);
        else return Bad.ofOne("wasn't under 10");
      });
    Or<Vector<Integer>, Every<String>> res = ctx.validatedBy(vec, f).get(Duration.ofSeconds(10));
    assertTrue(res.isGood());
    assertEquals(vec, res.get());
    res = ctx.validatedBy(Vector.of(11), f, Vector.collector()).get(Duration.ofSeconds(10));
    assertTrue(res.isBad());
    assertTrue(res.getBad() instanceof One);
  }

  @Theory
  public void when(Executor es) throws InterruptedException, ExecutionException, TimeoutException {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    Function<String, Validation<String>> f1 = f -> f.startsWith("s") ? Pass.instance() : Fail.of("does not start with s");
    Function<String, Validation<String>> f2 = f -> f.length() > 4 ? Fail.of("too long") : Pass.instance();
    OrFuture<String, One<String>> orFuture = ctx.future(() -> Bad.<String,String>of("failure")).accumulating();
    OrFuture<String, Every<String>> res = ctx.when(orFuture, f1, f2);
    assertEquals("failure", res.get(Duration.ofSeconds(10)).getBad().get(0));
    orFuture = ctx.future(() -> Good.of("sub")).accumulating();
    res = ctx.when(orFuture, f1, f2);
    assertTrue(res.get(Duration.ofSeconds(10)).isGood());
    orFuture = ctx.future(() -> Good.of("fubiluuri")).accumulating();
    res = ctx.when(orFuture, f1, f2);
    assertTrue(res.get(Duration.ofSeconds(10)).isBad());
  }

  @Theory
  public void withGood(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    Function<? super OrFuture<String, ? extends Every<String>>[], OrFuture<?, Every<String>>> fun =
      ors -> ctx.withGood(ors[0], ors[1], (a, b) -> "");
    testWithF(es, fun, 2);
    fun = o -> ctx.withGood(o[0], o[1], o[2], (a, b, c) -> "");
    testWithF(es, fun, 3);
    fun = o -> ctx.withGood(o[0], o[1], o[2], o[3], (a, b, c, d) -> "");
    testWithF(es, fun, 4);
    fun = o -> ctx.withGood(o[0], o[1], o[2], o[3], o[4], (a, b, c, d, e) -> "");
    testWithF(es, fun, 5);
    fun = o -> ctx.withGood(o[0], o[1], o[2], o[3], o[4], o[5], (a, b, c, d, e, f) -> "");
    testWithF(es, fun, 6);
    fun = o -> ctx.withGood(o[0], o[1], o[2], o[3], o[4], o[5], o[6], (a, b, c, d, e, f, g) -> "");
    testWithF(es, fun, 7);
    fun = o -> ctx.withGood(o[0], o[1], o[2], o[3], o[4], o[5], o[6], o[7], (a, b, c, d, e, f, g, h) -> "");
    testWithF(es, fun, 8);
  }

  @Theory
  public void zips(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    Function<? super OrFuture<String, ? extends Every<String>>[], OrFuture<?, Every<String>>> fun =
      ors -> ctx.zip(ors[0], ors[1]);
    testWithF(es, fun, 2);
    fun = o -> ctx.zip3(o[0], o[1], o[2]);
    testWithF(es, fun, 3);
  }

  private void testWithF(Executor es,
                         Function<? super OrFuture<String, ? extends Every<String>>[], OrFuture<?, Every<String>>> f,
                         int size) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    @SuppressWarnings("unchecked")
    OrFuture<String, One<String>>[] ors = new OrFuture[size];
    for (int i = 0; i <= ors.length; i++) {
      for (int j = 0; j < ors.length; j++) {
        if (j == i) ors[j] = ctx.future(() -> Bad.<String,String>of("failure")).accumulating();
        else ors[j] = ctx.future(() -> Good.of("success")).accumulating();
      }
      OrFuture<?, Every<String>> val = f.apply(ors);
      if (i < ors.length)
        assertTrue(val.get(Duration.ofSeconds(10)).isBad());
      else
        assertTrue(val.get(Duration.ofSeconds(10)).isGood());
    }
  }

  @Theory
  public void sequenceWithErrors(Executor es) throws TimeoutException, InterruptedException {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    Tuple2<Iterable<OrFuture<String, One<String>>>, AtomicInteger> t2 = iterable(ctx, 1000, true);
    OrFuture<Vector<String>, Every<String>> withErrorsF = ctx.sequence(t2._1);
    Or<Vector<String>, Every<String>> withErrors = withErrorsF.get(Duration.ofSeconds(10));
    if(t2._2.get() > 0) {
      Assert.assertTrue(withErrors.isBad());
    } else {
      Assert.assertTrue(withErrors.isGood());
    }
  }

  @Theory
  public void sequenceWithoutErrors(Executor es) throws TimeoutException, InterruptedException {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    int COUNT = 1000;
    Tuple2<Iterable<OrFuture<String, One<String>>>, AtomicInteger> t2 = iterable(ctx, COUNT, false);
    OrFuture<Vector<String>, Every<String>> withoutErrorsF = ctx.sequence(t2._1);
    Or<Vector<String>, Every<String>> withoutErrors = withoutErrorsF.get(Duration.ofSeconds(10));
    Assert.assertEquals(0, t2._2.get());
    Assert.assertTrue(withoutErrors.isGood());
    Assert.assertEquals(COUNT, withoutErrors.get().length());
  }

  private Tuple2<Iterable<OrFuture<String, One<String>>>, AtomicInteger> iterable(ExecutionContext<String> ctx, int count, boolean errors) {
    List<OrFuture<String, One<String>>> list = List.empty();
    AtomicInteger errorCount = new AtomicInteger();
    for (int i = 0; i < count; i++) {
      int value = i;
      OrFuture<String, One<String>> future = ctx.future(() -> {
        if (errors && ThreadLocalRandom.current().nextBoolean()) {
          errorCount.incrementAndGet();
          return Bad.of("bad " + value);
        } else {
          return Or.good("good " + value);
        }
      }).accumulating();
      list = list.prepend(future);
    }
    return Tuple.of(list, errorCount);
  }

  @Test
  public void sequence() {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newCachedThreadPool());
    CountDownLatch second = new CountDownLatch(1);
    Iterable<OrFuture<String, One<String>>> iterable = Vector.of(getBad(ctx, new CountDownLatch(0)), getBad(ctx, second));
    OrFuture<Vector<String>, Every<String>> sequence = ctx.sequence(iterable);
    try {
      Or<Vector<String>, Every<String>> or = sequence.get(Duration.ofSeconds(10));
      Assert.assertTrue(or.isBad());
    } catch (TimeoutException | InterruptedException e) {
      Assert.fail(e.getMessage());
    } finally {
      second.countDown();
    }
  }

  private OrFuture<String, One<String>> getBad(ExecutionContext<String> ctx, CountDownLatch latch) {
    return ctx.future(() -> {
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return Bad.<String,String>of("bad");
    }).accumulating();
  }

  @Test
  public void firstCompletedOf() {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newCachedThreadPool());
    CountDownLatch first = new CountDownLatch(1);
    Iterable<OrFuture<String, String>> iterable = Vector.of(getGood(ctx, first, "a"), getGood(ctx, new CountDownLatch(0), "b"));
    OrFuture<String, String> completed = ctx.firstCompletedOf(iterable);
    try {
      Or<String, String> or = completed.get(Duration.ofSeconds(10));
      Assert.assertEquals("b", or.get());
    } catch (TimeoutException | InterruptedException e) {
      Assert.fail(e.getMessage());
    } finally {
      first.countDown();
    }
  }

  private OrFuture<String, String> getGood(ExecutionContext<String> ctx, CountDownLatch latch, String value) {
    return ctx.future(() -> {
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return Good.of(value);
    });
  }

  @Test
  public void constructorsForCoverage() throws Exception {
    Constructor<Helper> constructor = Helper.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    constructor.newInstance();
  }

}
