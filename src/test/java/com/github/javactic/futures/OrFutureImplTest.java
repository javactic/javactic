package com.github.javactic.futures;

import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;

@RunWith(Theories.class)
public class OrFutureImplTest {

    @DataPoints
    public static ExecutorService[] configs = {Executors.newSingleThreadExecutor(), ForkJoinPool.commonPool()};

    private static final String FAIL = "fail";

    @Theory
    public void completions(ExecutorService es) {
        OrFutureImpl<String,String> f = new OrFutureImpl<>(es);
        f.complete(Good.of("good"));
        boolean retry = f.tryComplete(Bad.of(FAIL));
        assertFalse(retry);
    }
    
    @Theory
    public void run(ExecutorService es) throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>(es);
        CountDownLatch latch = new CountDownLatch(1);
        f.run(() -> {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Good.of("5");
        });
        try {
            f.run(() -> Bad.of("shouldn't succeed"));
            Assert.fail("can't run the same future twice");
        } catch(IllegalStateException e) {
            // expected
        }
        latch.countDown();
        Or<String, String> result = f.result(Duration.ofSeconds(10));
        try {
            f.run(() -> Bad.of("should be complete"));
            Assert.fail("can't run a completed future");
        } catch (IllegalStateException e) {
            // expected
        }
        Assert.assertEquals("5", result.get());
    }
    
    @Theory
    public void resultWithBad(ExecutorService es) throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>(es);
        CountDownLatch latch = new CountDownLatch(1);
        f.run(() -> {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Good.of("5");
        });
        Or<String, String> fail = f.result(Duration.ofMillis(10), FAIL);
        Assert.assertEquals(FAIL, fail.getBad());
        latch.countDown();
        Or<String, String> succ = f.result(Duration.ofSeconds(10), FAIL);
        Assert.assertEquals("5", succ.get());
    }
    
    @Theory
    public void resultWithException(ExecutorService es) throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>(es);
        CountDownLatch latch = new CountDownLatch(1);
        f.run(() -> {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Good.of("5");
        });
        try {
            f.result(Duration.ofMillis(10));
            Assert.fail("should throw timeout");
        } catch (TimeoutException e) {
            // expected
        }
        latch.countDown();
        Or<String, String> succ = f.result(Duration.ofSeconds(10));
        Assert.assertEquals("5", succ.get());
    }

}
