package com.github.javactic.futures;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Pass;

import javaslang.control.Option;

@RunWith(Theories.class)
public class OrFutureTest {
    
    @DataPoints
    public static ExecutorService[] configs = {Executors.newSingleThreadExecutor(), ForkJoinPool.commonPool()};
    
    private static final String FAIL = "fail";

    @Theory
    public void create(ExecutorService es) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        OrFuture<String, Object> orf = OrFuture.of(es, () -> {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Good.of("good");
        });
        assertEquals(Option.none(), orf.value());
        latch.countDown();
        CountDownLatch latch2 = new CountDownLatch(1);
        orf.onComplete(or -> latch2.countDown());
        latch2.await();
        assertEquals(Good.of("good"), orf.value().get());
    }

    @Theory
    public void filter(ExecutorService es) throws Exception {
        OrFuture<Integer, String> orFuture = getF(es, 5)
                .filter(i -> (i > 10) ? Pass.instance() : Fail.of(FAIL));
        Or<Integer, String> or = orFuture.result(Duration.ofSeconds(10));
        Assert.assertEquals(FAIL, or.getBad());
    }

    @Theory
    public void map(ExecutorService es) throws Exception {
        OrFuture<String, String> orFuture = getF(es, 5)
                .map(i -> "" + i);
        Assert.assertEquals("5", orFuture.result(Duration.ofSeconds(10)).get());
    }
    
    @Theory
    public void flatMap(ExecutorService es) throws Exception {
        OrFuture<String, String> orFuture = getF(es, 5)
                .flatMap(i -> OrFuture.of(es, () -> Good.of(i + "")));
        Assert.assertEquals("5", orFuture.result(Duration.ofSeconds(10)).get());
        
        orFuture = getF(es, 5).flatMap(i -> OrFuture.failed(FAIL));
        Assert.assertEquals(FAIL, orFuture.result(Duration.ofSeconds(10)).getBad());
        
        orFuture = OrFuture.<String, String>failed(FAIL).flatMap(i -> OrFuture.successful("5"));
        assertEquals(FAIL, orFuture.result(Duration.ofSeconds(10)).getBad());
    }

    @Test
    public void recover() throws Exception {
        OrFuture<String, String> recover = OrFuture.<String,String>failed(FAIL).recover(f -> "5");
        assertEquals("5", recover.result(Duration.ofSeconds(10)).get());
    }
    
    @Test
    public void recoverWith() throws Exception {
        OrFuture<String, String> recover = OrFuture.<String,String>failed(FAIL).recoverWith(f -> OrFuture.successful("5"));
        assertEquals("5", recover.result(Duration.ofSeconds(10)).get());
    }
    
    @Test
    public void transform() throws Exception {
        OrFuture<String, String> or = OrFuture.failed(FAIL);
        OrFuture<Integer, Integer> transform = or.transform(s -> 5, f -> -5);
        assertEquals(-5, transform.result(Duration.ofSeconds(10)).getBad().intValue());
    }
    
    private <G> OrFuture<G,String> getF(ExecutorService es, G g){
        return OrFuture.of(es, () -> Good.of(g));
    }
}
