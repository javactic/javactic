/**
 *    ___                       _   _      
 *   |_  |                     | | (_)     
 *     | | __ ___   ____ _  ___| |_ _  ___ 
 *     | |/ _` \ \ / / _` |/ __| __| |/ __|
 * /\__/ / (_| |\ V / (_| | (__| |_| | (__   -2015-
 * \____/ \__,_| \_/ \__,_|\___|\__|_|\___|
 *                                          
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.github.javactic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import javaslang.Tuple;
import javaslang.collection.Stream;
import javaslang.collection.Vector;

public class EveryATest {

    @Test
    public void testTrivial() {
        Every<String> every = Every.of("a", "b");
        assertTrue(every.isDefinedAt(0));
        assertTrue(every.isDefinedAt(1));
        assertFalse(every.isDefinedAt(2));
        assertFalse(every.isDefinedAt(-1));
        assertFalse(every.isEmpty());
        assertTrue(every.headOption().isDefined());
        assertTrue(every.lastOption().isDefined());
        assertEquals(2, every.size());
        assertEquals(0, every.lengthCompare(2));
        assertTrue(every.sameElements(Stream.of("a", "b")));
    }
    
    @Test
    public void reverse() {
        Every<String> every = Every.of("a", "b");
        Every<String> reversed = Every.of("b", "a");
        assertEquals(reversed, every.reverse());
        assertEquals(Vector.ofAll(every.reverse().iterator()), Vector.ofAll(every.reverseIterator()));
        
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
