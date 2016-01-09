package com.github.javactic.futures;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Bad;
import com.github.javactic.Good;


public class OrPromiseTest {

    @Test
    public void doubleComplete() {
        OrPromise<String,String> p = OrPromise.create();
        OrFuture<String, String> future = p.complete(Good.of("success")).future();
        try {
            p.complete(Bad.of("failure"));
            Assert.fail("double complete");
        } catch (IllegalStateException e) {
            // expected
        }
        Assert.assertEquals("success", future.getOption().get().get());
    }
    
    @Test
    public void tries() {
        OrPromise<String,String> p = OrPromise.create();
        Assert.assertTrue(p.tryFailure("failure"));
        Assert.assertFalse(p.trySuccess("success"));
        OrFuture<String, String> future = p.future();
        Assert.assertEquals("failure", future.getOption().get().getBad());
    }
}
