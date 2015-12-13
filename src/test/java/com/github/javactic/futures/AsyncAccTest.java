package com.github.javactic.futures;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.github.javactic.Bad;
import com.github.javactic.Every;
import com.github.javactic.Good;
import com.github.javactic.Or;

import javaslang.collection.Seq;
import javaslang.collection.Vector;

public class AsyncAccTest {

    @Test
    public void withGoodFail() throws Exception {
        OrFuture<String, String> success = OrFuture.successful("good");
        OrFuture<String, String> fail = OrFuture.failed("bad");
        OrFuture<String, Every<String>> result = AsyncAcc.withGood(success, fail, (a1,a2) -> "doesn't matter");
        Or<String, Every<String>> or = result.result(10, TimeUnit.SECONDS);
        Assert.assertEquals("bad", or.getBad().head());
    }
    
    @Test
    public void withGoodSuccess() throws Exception {
        OrFuture<String, String> s1 = OrFuture.successful("A");
        OrFuture<Integer, String> s2 = OrFuture.successful(1);
        OrFuture<String, Every<String>> result = AsyncAcc.withGood(s1, s2, (a1,a2) -> a1 + a2);
        Or<String, Every<String>> or = result.result(10, TimeUnit.SECONDS);
        Assert.assertEquals("A1", or.get());
    }
    
    @Test
    public void sequenceSuccess() throws Exception {
        Seq<OrFuture<Integer,String>> seq = Vector.empty();
        for(int i = 0; i < 10; i++) {
            final int fi = i;
            seq = seq.append(OrFuture.of(() -> Good.of(fi)));
        }
        OrFuture<Vector<Integer>, Every<String>> sequence = AsyncAcc.combined(seq);
        Or<Vector<Integer>, Every<String>> or = sequence.result(10, TimeUnit.SECONDS);
        Assert.assertTrue(or.isGood());
        String fold = or.get().foldLeft("", (s,i) -> s + i);
        Assert.assertEquals("0123456789", fold);
    }
    
    @Test
    public void sequenceFailure() throws Exception {
        Seq<OrFuture<Integer,Integer>> seq = Vector.empty();
        for(int i = 0; i < 10; i++) {
            final int fi = i;
            if(i % 2 == 0)
                seq = seq.append(OrFuture.of(() -> Good.of(fi)));
            else
                seq = seq.append(OrFuture.of(() -> Bad.of(fi)));
        }
        OrFuture<Vector<Integer>, Every<Integer>> sequence = AsyncAcc.combined(seq);
        Or<Vector<Integer>, Every<Integer>> or = sequence.result(10, TimeUnit.SECONDS);
        Assert.assertTrue(or.isBad());
        String fold = or.getBad().foldLeft("", (s,i) -> s + i);
        Assert.assertEquals("13579", fold);
    }
    
    @Test
    public void withGood() throws Exception {
        Function<OrFuture<String, String>[], OrFuture<?, Every<String>>> fun = 
                ors -> AsyncAcc.withGood(ors[0], ors[1], (a,b) -> "");
        testWithF(fun, 2);
        fun = o -> AsyncAcc.withGood(o[0], o[1], o[2], (a, b, c) -> "");
        testWithF(fun, 3);
        fun = o -> AsyncAcc.withGood(o[0],o[1],o[2],o[3], (a,b,c,d) -> "");
        testWithF(fun, 4);
        fun = o -> AsyncAcc.withGood(o[0],o[1],o[2],o[3],o[4], (a,b,c,d,e) -> "");
        testWithF(fun, 5);
        fun = o -> AsyncAcc.withGood(o[0],o[1],o[2],o[3],o[4],o[5], (a,b,c,d,e,f) -> "");
        testWithF(fun, 6);
        fun = o -> AsyncAcc.withGood(o[0],o[1],o[2],o[3],o[4],o[5],o[6], (a,b,c,d,e,f,g) -> "");
        testWithF(fun, 7);
        fun = o -> AsyncAcc.withGood(o[0],o[1],o[2],o[3],o[4],o[5],o[6],o[7], (a,b,c,d,e,f,g,h) -> "");
        testWithF(fun, 8);
    }

    @Test
    public void zips() throws Exception {
        Function<OrFuture<String, String>[], OrFuture<?, Every<String>>> fun = 
                ors -> AsyncAcc.zip(ors[0], ors[1]);
        testWithF(fun, 2);
        fun = o -> AsyncAcc.zip3(o[0], o[1], o[2]);
        testWithF(fun, 3);
        fun = o -> AsyncAcc.zip4(o[0],o[1],o[2],o[3]);
        testWithF(fun, 4);
        fun = o -> AsyncAcc.zip5(o[0],o[1],o[2],o[3],o[4]);
        testWithF(fun, 5);
        fun = o -> AsyncAcc.zip6(o[0],o[1],o[2],o[3],o[4],o[5]);
        testWithF(fun, 6);
        fun = o -> AsyncAcc.zip7(o[0],o[1],o[2],o[3],o[4],o[5],o[6]);
        testWithF(fun, 7);
        fun = o -> AsyncAcc.zip8(o[0],o[1],o[2],o[3],o[4],o[5],o[6],o[7]);
        testWithF(fun, 8);
        
    }
        
    private void testWithF(Function<OrFuture<String, String>[], OrFuture<?, Every<String>>> f, int size) throws Exception {
        @SuppressWarnings("unchecked")
        OrFuture<String, String>[] ors = new OrFuture[size];
        for(int i = 0; i <= ors.length; i++){
            for(int j = 0; j < ors.length; j++) {
                if(j == i) ors[j] = OrFuture.of(() -> Bad.of("bad"));
                else ors[j] = OrFuture.of(() -> Good.of("good"));
            }
            OrFuture<?, Every<String>> val = f.apply(ors);
            if(i < ors.length)
                assertTrue(val.result(10, TimeUnit.MINUTES).isBad());
            else
                assertTrue(val.result(10, TimeUnit.MINUTES).isGood());
        }
    }
    
    @Test
    public void constructorsForCoverage() throws Exception {
        Constructor<AsyncAcc> constructor = AsyncAcc.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
