package com.github.javactic.futures;

import com.github.javactic.Or;

public class FutureFactoryTest {
    
    private final String bad = "failure";
    private final String good = "success";
    
//    @Test
//    public void fail() throws Exception {
//        FutureFactory<String> f = FutureFactory.OF_EXCEPTION_MESSAGE;
//        OrFuture<String, String> orFuture = f.newFuture(this::get);
//        Or<String, String> result = orFuture.get(Duration.ofSeconds(10));
//        Assert.assertEquals(bad, result.getBad());
//    }
//
//    @Test
//    public void success() throws Exception {
//        FutureFactory<String> f = FutureFactory.OF_EXCEPTION_MESSAGE;
//        OrFuture<String, String> orFuture = f.newFuture(() -> Good.of(good));
//        Or<String, String> result = orFuture.get(Duration.ofSeconds(10));
//        Assert.assertEquals(good, result.get());
//    }
    
    public Or<String, String> get() {
        throw new RuntimeException(bad);
    }
}
