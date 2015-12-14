package com.github.javactic.futures;

import java.time.Duration;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Good;
import com.github.javactic.Or;

public class FutureFactoryTest {
    
    private final String bad = "bad";
    private final String good = "good";
    
    @Test
    public void fail() throws Exception {
        FutureFactory<String> f = FutureFactory.OF_EXCEPTION_MESSAGE;
        OrFuture<String, String> orFuture = f.future(() -> get());
        Or<String, String> result = orFuture.result(Duration.ofSeconds(10));
        Assert.assertEquals(bad, result.getBad());
    }
    
    @Test
    public void success() throws Exception {
        FutureFactory<String> f = FutureFactory.OF_EXCEPTION_MESSAGE;
        OrFuture<String, String> orFuture = f.future(() -> Good.of(good));
        Or<String, String> result = orFuture.result(Duration.ofSeconds(10));
        Assert.assertEquals(good, result.get());
    }
    
    public Or<String, String> get() {
        throw new RuntimeException(bad);
    }
}
