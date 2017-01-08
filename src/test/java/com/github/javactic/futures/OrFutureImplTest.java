package com.github.javactic.futures;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;

@RunWith(Theories.class)
public class OrFutureImplTest {

  @DataPoints
  public static Executor[] configs = {Executors.newSingleThreadExecutor(), Executors.newCachedThreadPool()};

  private static final String FAIL = "fail";

  @Theory
  public void completions(Executor es) {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
    OrFutureImpl<String, String> f = new OrFutureImpl<>(ctx);
    f.complete(Good.of("success"));
    boolean retry = f.tryComplete(Bad.of(FAIL));
    assertFalse(retry);
  }

//  @Theory
//  public void resultWithBad(Executor es) throws Exception {
//    ExecutionContext<String> ctx = ExecutionContext.with(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
//    OrFutureImpl<String, String> f = new OrFutureImpl<>(ctx, es);
//    CountDownLatch latch = new CountDownLatch(1);
//    f.run(() -> {
//      try {
//        latch.await();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//      return Good.of("5");
//    });
//    Or<String, String> fail = f.get(Duration.ofMillis(10), FAIL);
//    Assert.assertEquals(FAIL, fail.getBad());
//    latch.countDown();
//    Or<String, String> succ = f.get(Duration.ofSeconds(10), FAIL);
//    Assert.assertEquals("5", succ.get());
//  }
//
//  @Theory
//  public void resultWithException(Executor es) throws Exception {
//    ExecutionContext<String> ctx = ExecutionContext.with(ExecutionContext.OF_EXCEPTION_MESSAGE, es);
//    OrFutureImpl<String, String> f = new OrFutureImpl<>(ctx, es);
//    CountDownLatch latch = new CountDownLatch(1);
//    f.run(() -> {
//      try {
//        latch.await();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//      return Good.of("5");
//    });
//    try {
//      f.get(Duration.ofMillis(10));
//      Assert.fail("should throw timeout");
//    } catch (TimeoutException e) {
//      // expected
//    }
//    latch.countDown();
//    Or<String, String> succ = f.get(Duration.ofSeconds(10));
//    Assert.assertEquals("5", succ.get());
//  }

  @Test
  public void getOption() {
    ExecutionContext<String> ctx = ExecutionContext.of(ExecutionContext.OF_EXCEPTION_MESSAGE, Executors.newSingleThreadExecutor());
    OrPromise<String,String> p = ctx.promise();
    OrFuture<String, String> future = p.future();
    Assert.assertTrue(future.getOption().isEmpty());
    p.complete(Or.good(""));
    Assert.assertTrue(future.getOption().isDefined());
  }
  
}
