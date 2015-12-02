package com.github.javactic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import javaslang.Tuple;

public class EveryTest1 {

    @Test
    public void testTrivial() {
        Every<String> every = Every.of("a", "b");
        assertTrue(every.isDefinedAt(0));
        assertTrue(every.isDefinedAt(1));
        assertFalse(every.isDefinedAt(2));
        assertFalse(every.isDefinedAt(-1));
        assertFalse(every.isEmpty());
    }
    
    @Test
    public void testConversions() {
        Every<String> every = Every.of("a", "b");
        assertEquals(every.length(), every.toArray().length());
        assertEquals(every.length(), every.toJavaArray(String.class).length);
        assertEquals(every.length(), every.toJavaList().size());
        assertEquals(every.length(), every.toJavaMap(s -> Tuple.of(s, s)).size());
        assertEquals(every.length(), every.toJavaSet().size());
        assertEquals(every.length(), every.toJavaStream().count());
        assertEquals(every.length(), every.toList().length());
        assertEquals(every.length(), every.toMap(s -> Tuple.of(s, s)).length());
        assertEquals(every.length(), every.toSeq().length());
        assertEquals(every.length(), every.toSet().length());
        assertEquals(every.length(), every.toStream().length());
        assertEquals(every.length(), every.toTraversable().length());
        assertEquals(every.length(), every.toVector().length());
    }
    
    @Test
    public void hashCodeEqualsToString() {
        Many<String> pair1 = Many.of("a", "b");
        Many<String> pair2 = Many.of("a", "b");
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());
        assertEquals(pair1.toString(), pair2.toString());
        
        One<String> single1 = One.of("c");
        One<String> single2 = One.of("c");
        assertEquals(single1, single2);
        assertEquals(single1.hashCode(), single2.hashCode());
        assertEquals(single1.toString(), single2.toString());
        
        Assert.assertNotEquals(pair1, single1);
        Assert.assertNotEquals(single2, pair2);
    }

}
