package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Pass;
import javaslang.control.Option;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class OrFutureTest {

  private FutureFactory<String> ff = FutureFactory.OF_EXCEPTION_MESSAGE;

  @DataPoints
  public static Executor[] configs = {Executors.newSingleThreadExecutor(), Helper.DEFAULT_EXECUTOR};

  private static final String FAIL = "fail";

  @Theory
  public void create(Executor es) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, Object> orf = OrFuture.of(es, () -> {
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
    OrFuture<Integer, String> orFuture = getF(es, 5)
      .filter(i -> (i > 10) ? Pass.instance() : Fail.of(FAIL));
    Or<Integer, String> or = orFuture.get(Duration.ofSeconds(10));
    Assert.assertEquals(FAIL, or.getBad());
  }

  @Theory
  public void map(Executor es) throws Exception {
    OrFuture<String, String> orFuture = getF(es, 5)
      .map(i -> "" + i);
    Assert.assertEquals("5", orFuture.get(Duration.ofSeconds(10)).get());
  }

  @Theory
  public void badMap(Executor es) throws Exception {
    OrFuture<String, String> orFuture = ff.newFuture(es, () -> Bad.<String, String>of("bad"))
      .badMap(s -> new StringBuilder(s).reverse().toString());
    Assert.assertEquals("dab", orFuture.get(Duration.ofSeconds(10)).getBad());
  }

  @Theory
  public void flatMap(Executor es) throws Exception {
    OrFuture<String, String> orFuture = getF(es, 5)
      .flatMap(i -> ff.newFuture(es, () -> Good.of(i + "")));
    Assert.assertEquals("5", orFuture.get(Duration.ofSeconds(10)).get());

    orFuture = getF(es, 5).flatMap(i -> OrFuture.ofBad(FAIL));
    Assert.assertEquals(FAIL, orFuture.get(Duration.ofSeconds(10)).getBad());

    orFuture = OrFuture.<String, String>ofBad(FAIL).flatMap(i -> OrFuture.ofGood("5"));
    assertEquals(FAIL, orFuture.getUnsafe().getBad());
  }

  @Test
  public void andThen() throws Exception {
    OrFuture<String, String> future = OrFuture.ofGood("good");
    AtomicBoolean visited = new AtomicBoolean();
    Or<String, String> result = future
      .andThen(or -> {
        throw new RuntimeException();
      })
      .andThen(or -> visited.set(true))
      .get(Duration.ofSeconds(10));
    assertTrue(visited.get());
    assertEquals("good", result.get());
  }

  @Test
  public void recover() throws Exception {
    OrFuture<String, String> recover = OrFuture.<String, String>ofBad(FAIL).recover(f -> "5");
    assertEquals("5", recover.get(Duration.ofSeconds(10)).get());
  }

  @Test
  public void recoverWith() throws Exception {
    OrFuture<String, String> recover = OrFuture.<String, String>ofBad(FAIL).recoverWith(f -> OrFuture.ofGood("5"));
    assertEquals("5", recover.get(Duration.ofSeconds(10)).get());
  }

  @Test
  public void transform() throws Exception {
    OrFuture<String, String> or = OrFuture.ofBad(FAIL);
    OrFuture<Integer, Integer> transform = or.transform(s -> 5, f -> -5);
    assertEquals(-5, transform.get(Duration.ofSeconds(10)).getBad().intValue());
  }

  @Test
  public void getUnsafe() {
    CountDownLatch latch = new CountDownLatch(1);
    OrFuture<String, String> or = OrFuture.of(() -> {
      try {
        latch.await();
        return Or.good("good");
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    Assert.assertEquals("OrFuture(N/A)", or.toString());
    Thread thread = Thread.currentThread();
    Helper.DEFAULT_EXECUTOR.execute(() -> {
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
    }
  }

  private <G> OrFuture<G, String> getF(Executor es, G g) {
    return ff.newFuture(es, () -> Good.of(g));
  }
}
