package com.github.javactic.futures;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Fail;
import com.github.javactic.Good;
import com.github.javactic.Or;
import com.github.javactic.Pass;

import javaslang.control.Option;

public class OrFutureTest {

    private static final String FAIL = "fail";

    @Test
    public void create() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        OrFuture<String, Object> orf = OrFuture.of(() -> {
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

    @Test
    public void filter() throws Exception {
        OrFuture<Integer, String> orFuture = getF(5)
                .filter(i -> (i > 10) ? Pass.instance() : Fail.of(FAIL));
        Or<Integer, String> or = orFuture.result(Duration.ofSeconds(10));
        Assert.assertEquals(FAIL, or.getBad());
    }

    @Test
    public void map() throws Exception {
        OrFuture<String, String> orFuture = getF(5)
                .map(i -> "" + i);
        Assert.assertEquals("5", orFuture.result(Duration.ofSeconds(10)).get());
    }
    
    @Test
    public void flatMap() throws Exception {
        OrFuture<String, String> orFuture = getF(5)
                .flatMap(i -> OrFuture.of(() -> Good.of(i + "")));
        Assert.assertEquals("5", orFuture.result(Duration.ofSeconds(10)).get());
        
        orFuture = getF(5).flatMap(i -> OrFuture.failed(FAIL));
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
    
    private <G> OrFuture<G,String> getF(G g){
        return OrFuture.of(() -> Good.of(g));
    }
}
