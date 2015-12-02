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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

import com.github.javactic.Every;

import javaslang.collection.List;
import javaslang.collection.Stream;

public class EveryTest {

    @Test
    public void addString() {
        Every<String> e = Every.of("a", "b", "c");
        StringBuilder sb = e.addString(new StringBuilder());
        assertEquals("abc", sb.toString());
        sb = e.addString(new StringBuilder(), ":");
        assertEquals("a:b:c", sb.toString());
        sb = e.addString(new StringBuilder(), "[", ":", "]");
        assertEquals("[a:b:c]", sb.toString());
    }
    
    @Test
    public void append() {
        Every<String> e = Every.of("a", "b", "c");
        assertEquals("d", e.append("d").last());
        assertEquals("d", e.appendAll(Stream.of("d")).last());
        assertEquals("d", e.appendAll(Every.of("d")).last());
    }
    
    @Test
    public void applyAndGet() {
        Every<String> e = Every.of("a", "b", "c");
        assertEquals("a", e.get(0)); // get calls apply, no separate test
        assertEquals("c", e.getOrElse(2, i -> "x")); // idem
        assertEquals("d", e.getOrElse(3, i -> "d"));
        
        assertEquals("c", e.getOrElse(2, "x"));
        assertEquals("d", e.getOrElse(3, "d"));
    }
    
    @Test
    public void compose() {
        Every<String> e = Every.of("1", "2");
        Function<String, String> compose = e.compose((String a) -> Integer.parseInt(a) -1);
        assertEquals("1", compose.apply("1"));
    }
    
    @Test
    public void containsAndContainsSlice() {
        Every<String> e = Every.of("a", "b", "b", "c");
        assertTrue(e.contains("a"));
        assertTrue(e.containsSlice(Stream.of("b", "c")));
    }
    
    @Test
    public void copyToArray() {
        Every<String> e = Every.of("a", "b", "c");
        String[] target = new String[2];
        e.copyToArray(target);
        assertEquals(List.of("a", "b"), List.of(target));
        target = new String[4];
        e.copyToArray(target);
        assertEquals(List.of("a", "b", "c", null), List.of(target));
        target = new String[1];
        e.copyToArray(target, 4);
        assertEquals(null, target[0]);
    }
    
	@Test
	public void groupedTest1() {
		Every<String> e = Every.of("a", "b", "c");
		Iterator<Every<String>> it = e.grouped(2);
		Every<String> first = it.next();
		Every<String> secnd = it.next();
		assertEquals("a", first.get(0));
		assertEquals("b", first.get(1));
		assertEquals("c", secnd.get(0));
		try{
			it.next();
			Assert.fail("should have two");
		}
		catch(NoSuchElementException ex) {
			// expected
		}
	}

	@Test
	public void groupedTest2() {
		Every<String> e = Every.of("a");
		Iterator<Every<String>> it = e.grouped(2);
		Every<String> first = it.next();
		assertEquals("a", first.get(0));
		try{
			it.next();
			Assert.fail("should have two");
		}
		catch(NoSuchElementException ex) {
			// expected
		}
	}

	@Test
	public void groupedTest3() {
		Every<String> e = Every.of("a", "b");
		Iterator<Every<String>> it = e.grouped(2);
		Every<String> first = it.next();
		assertEquals("a", first.get(0));
		assertEquals("b", first.get(1));
		try{
			it.next();
			Assert.fail("should have two");
		}
		catch(NoSuchElementException ex) {
			// expected
		}
	}
	
	@Test
	public void indexOf() {
		Every<String> e = Every.of("a", "b", "c");
		assertEquals(0, e.indexOf("a"));
		assertEquals(2, e.indexOf("c", 1));
		assertEquals(1, e.indexOf("b", 1));
		assertEquals(2, e.indexOf("c", 2));
		assertEquals(-1, e.indexOf("c", 3));
	}
	
	@Test
	public void patch() {
		Every<String> e = Every.of("a", "b", "c");
		Every<String> patch = Every.of("d");
		assertEquals(Every.of("a", "b", "c", "d"), e.patch(3, patch, 0));
		assertEquals(Every.of("a", "b", "d", "c"), e.patch(2, patch, 0));
		assertEquals(Every.of("a", "b", "c", "d"), e.patch(5, patch, 5));
		assertEquals(Every.of("a", "b", "c", "d"), e.patch(5, patch, -5));
		assertEquals(Every.of("d", "a", "b", "c"), e.patch(0, patch, -5));
		assertEquals(Every.of("d"), e.patch(0, patch, 5));
		assertEquals(Every.of("d", "b", "c"), e.patch(0, patch, 1));
		assertEquals(Every.of("a", "b", "d"), e.patch(2, patch, 1));
	}

	@Test
	public void sortWith() {
		Every<Integer> e = Every.of(3,6,1,7);
		Every<Integer> sorted = e.sortWith((l,r) -> l < r);
		assertEquals(Every.of(1,3,6,7), sorted);
	}
	
}
