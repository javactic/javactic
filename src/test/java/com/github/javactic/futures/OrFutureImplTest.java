package com.github.javactic.futures;

import static org.junit.Assert.assertFalse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Bad;
import com.github.javactic.Good;
import com.github.javactic.Or;

public class OrFutureImplTest {

    private static final String FAIL = "fail";

    @Test
    public void completions() {
        OrFutureImpl<String,String> f = new OrFutureImpl<>();
        f.complete(Good.of("good"));
        boolean retry = f.tryComplete(Bad.of(FAIL));
        assertFalse(retry);
    }
    
    @Test
    public void run() throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>();
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
        Or<String, String> result = f.result(10, TimeUnit.SECONDS);
        try {
            f.run(() -> Bad.of("should be complete"));
            Assert.fail("can't run a completed future");
        } catch (IllegalStateException e) {
            // expected
        }
        Assert.assertEquals("5", result.get());
    }
    
    @Test
    public void resultWithBad() throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>();
        CountDownLatch latch = new CountDownLatch(1);
        f.run(() -> {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Good.of("5");
        });
        Or<String, String> fail = f.result(10, TimeUnit.MILLISECONDS, FAIL);
        Assert.assertEquals(FAIL, fail.getBad());
        latch.countDown();
        Or<String, String> succ = f.result(10, TimeUnit.SECONDS, FAIL);
        Assert.assertEquals("5", succ.get());
    }
    
    @Test
    public void resultWithException() throws Exception {
        OrFutureImpl<String,String> f = new OrFutureImpl<>();
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
            f.result(10, TimeUnit.MILLISECONDS);
            Assert.fail("should throw timeout");
        } catch (TimeoutException e) {
            // expected
        }
        latch.countDown();
        Or<String, String> succ = f.result(10, TimeUnit.SECONDS);
        Assert.assertEquals("5", succ.get());
    }

}
