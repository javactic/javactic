package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Pass;
import javaslang.control.Option;
import javaslang.control.Try;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class OrFutureTest {

  ExecutionContext<String> CTX = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newSingleThreadExecutor());

  @DataPoints
  public static Executor[] configs = {Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool()};

  private static final String FAIL = "fail";

  @Theory
  public void create(Executor es) throws InterruptedException {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, String> orf = ctx.future(() -> {
      try {
        latch.await();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return Good.of("success");
    });
    assertEquals(Option.none(), orf.getOption());
    latch.countDown();
    CountDownLatch latch2 = new CountDownLatch(1);
    orf.onComplete(or -> latch2.countDown());
    latch2.await();
    assertEquals(Good.of("success"), orf.getOption().get());
  }

  @Theory
  public void filter(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<Integer, String> orFuture = getF(ctx, 4)
      .filter(i -> (i > 10) ? Pass.instance() : Fail.of(FAIL));
    Or<Integer, String> or = orFuture.get(Duration.ofSeconds(10));
    Assert.assertEquals(FAIL, or.getBad());
  }

  @Theory
  public void map(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<String, String> orFuture = getF(ctx, 5)
      .map(i -> "" + i);
    Assert.assertEquals("5", orFuture.get(Duration.ofSeconds(10)).get());
  }

  @Theory
  public void badMap(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<String, String> orFuture = ctx.future(() -> Bad.<String, String>of("bad"))
      .badMap(s -> new StringBuilder(s).reverse().toString());
    Assert.assertEquals("dab", orFuture.get(Duration.ofSeconds(10)).getBad());
  }

  @Theory
  public void flatMap(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<String, String> orFuture = getF(ctx, 6)
      .flatMap(i -> ctx.future(() -> Good.of(i + "")));
    Assert.assertEquals("6", orFuture.get(Duration.ofSeconds(10)).get());

    orFuture = getF(ctx, 7).flatMap(i -> ctx.badFuture(FAIL));
    Assert.assertEquals(FAIL, orFuture.get(Duration.ofSeconds(10)).getBad());

    orFuture = ctx.badFuture(FAIL).flatMap(i -> ctx.goodFuture("7"));
    assertEquals(FAIL, orFuture.getUnsafe().getBad());
  }

  @Theory
  public void andThen(Executor es) throws Exception {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFuture<String, String> future = ctx.goodFuture("good");
    AtomicBoolean visited = new AtomicBoolean();
    Or<String, String> result = future
      .andThen(or -> {
        throw new RuntimeException();
      })
      .andThen(or -> visited.set(true))
      .getUnsafe();
    assertTrue(visited.get());
    assertEquals("good", result.get());
  }

  @Test
  public void recover() throws Exception {
    OrFuture<String, String> recover = CTX.<String>badFuture(FAIL).recover(f -> "5");
    assertEquals("5", recover.get(Duration.ofSeconds(10)).get());
  }

  @Test
  public void recoverWith() throws Exception {
    OrFuture<String, String> recover = CTX.<String>badFuture(FAIL).recoverWith(f -> CTX.goodFuture("5"));
    assertEquals("5", recover.get(Duration.ofSeconds(10)).get());
  }

  @Test
  public void transform() throws Exception {
    OrFuture<String, String> or = CTX.badFuture(FAIL);
    OrFuture<Integer, Integer> transform = or.transform(s -> 5, f -> -5);
    assertEquals(-5, transform.get(Duration.ofSeconds(10)).getBad().intValue());
  }

  @Test
  public void withContext() throws InterruptedException {
    String startThread = "start";
    String endThread = "end";
    ExecutionContext<String> start = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, newExecutor(startThread));
    ExecutionContext<String> end = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, newExecutor(endThread));

    OrFuture<String, String> startGood = start.future(() -> Or.good("good"));
    OrFuture<String, String> endGood = startGood.with(end);

    SimpleSafe<String> startSafe = new SimpleSafe<>();
    SimpleSafe<String> endSafe = new SimpleSafe<>();

    startGood.onComplete(or -> startSafe.set(Thread.currentThread().getName()));
    endGood.onComplete(or -> endSafe.set(Thread.currentThread().getName()));

    assertEquals(startThread, startSafe.get());
    assertEquals(endThread, endSafe.get());
  }

  @Test
  public void getUnsafe() {
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, String> or = CTX.future(() -> {
      try {
        latch.await();
        return Or.good("good");
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    Assert.assertEquals("OrFuture(N/A)", or.toString());
    Thread thread = Thread.currentThread();
    ForkJoinPool.commonPool().execute(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      thread.interrupt();
    });
    try {
      or.getUnsafe();
      Assert.fail("should throw");
    } catch(CompletionException ce) {
      // expected
      latch.countDown();
    }
  }

  private <G> OrFuture<G, String> getF(ExecutionContext<String> ctx, G g) {
    return ctx.future(() -> Good.of(g));
  }

  private Executor newExecutor(String name) {
    return Executors.newSingleThreadExecutor(r -> new Thread(r, name));
  }

  private static class SimpleSafe<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private T value;

    void set(T value) {
      this.value = value;
      latch.countDown();
    }

    T get() {
      sneakyRun(latch::await);
      return value;
    }
  }

  public static <T extends Throwable> void sneakyRun(Try.CheckedRunnable r) throws T {
    try {
      r.run();
    } catch (Throwable throwable) {
      throw (T)throwable;
    }
  }
}
