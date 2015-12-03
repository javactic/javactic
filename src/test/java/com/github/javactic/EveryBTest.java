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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Stream;
import javaslang.collection.Vector;

public class EveryBTest {

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
        assertEquals("c", e.appendAll(Stream.empty()).last());
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
    public void corresponds() {
        Every<String> e = Every.of("1", "2", "3");
        Stream<Integer> yes = Stream.of(1,2,3);
        Stream<Integer> not = Stream.of(1,2,3,4);
        assertTrue(e.corresponds(yes, (s,i) -> Integer.parseInt(s) ==  i));
        assertFalse(e.corresponds(not, (s,i) -> Integer.parseInt(s) ==  i));
    }
    
    @Test
    public void count() {
        Every<Integer> e = Every.of(1,2,3,4,5);
        assertEquals(3, e.count(i -> i < 4));
    }
    
    @Test
    public void distinct() {
        Every<Integer> e = Every.of(1,2,2,3,3);
        assertEquals(3, e.distinct().length());
    }
    
    @Test
    public void forEach() {
        Every<Integer> e = Every.of(1,2,3,4,5);
        java.util.List<Integer> list = new ArrayList<>();
        e.forEach(list::add);
        assertEquals(e.toJavaList(), list);
    }
    
    @Test
    public void forAll() {
        Every<Integer> e = Every.of(1,2,3,4,5);
        assertTrue(e.forAll(i -> i < 6));
    }
    
    @Test
    public void flatten() {
        Every<Every<Integer>> e = Every.of(One.of(1), One.of(2), Many.of(3, 4));
        assertEquals(Every.of(1,2,3,4), Every.flatten(e));
    }
    
    @Test
    public void fold() {
        Every<Integer> e = Every.of(1,2,3,4);
        assertEquals(10, e.fold(0, (i1, i2) -> i1 + i2).intValue());
        assertEquals("1234", e.foldLeft("", (s1, i1) -> s1 + i1));
        assertEquals("4321", e.foldRight("", (i1, s1) -> s1 + i1));
    }
    
    @Test
    public void groupBy() {
        Every<Integer> e = Every.of(1,2,3,4);
        Map<String, Every<Integer>> groupBy = e.groupBy(i -> (i % 2 == 0) ? "even" : "odd");
        assertEquals(Every.of(1, 3), groupBy.get("odd").get());
        assertEquals(Every.of(2, 4), groupBy.get("even").get());
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
	public void indexOfSlice() {
	    Every<String> e = Every.of("a", "b", "c", "b", "c");
	    Stream<String> s = Stream.of("b", "c");
	    assertEquals(1, e.indexOfSlice(s));
	    assertEquals(3, e.indexOfSlice(s, 2));
	}
	
	@Test
	public void indexWhere() {
	    Every<Integer> e = Every.of(3,4,5,6,7,1,2,5,6,7);
	    assertEquals(2, e.indexWhere(i -> i > 4));
	    assertEquals(7, e.indexWhere(i -> i > 4, 5));
	}
	
	@Test
	public void lastIndex() {
	    Every<Integer> e = Every.of(3,4,5,6,7,1,2,5,6,7);
	    assertEquals(9, e.lastIndexOf(7));
	    assertEquals(4, e.lastIndexOf(7, 8));
	}
	
	@Test
	public void lastIndexOfSlice() {
	    Every<Integer> e = Every.of(1,2,3,1,2,3);
	    Stream<Integer> s = Stream.of(1,2,3);
	    assertEquals(3, e.lastIndexOfSlice(s));
	    // assertEquals(0, e.lastIndexOfSlice(s, 2)); Javaslang bug
	}
	
    @Test
    public void lastIndexWhere() {
        Every<Integer> e = Every.of(3, 4, 5, 6, 7, 1, 2, 5, 6, 7);
        assertEquals(9, e.lastIndexWhere(i -> i > 4));
        assertEquals(4, e.lastIndexWhere(i -> i > 4, 5));
    }
    
    @Test
    public void map() {
        Every<Integer> e = Every.of(1,2,3,4);
        assertEquals(Every.of("1","2","3","4"), e.map(i -> "" +i));
    }
    
    @Test
    public void max() {
        Every<Integer> e = Every.of(1,2,3,4);
        assertEquals(4, e.max().get().intValue());
        assertEquals(1, e.maxBy(Comparator.reverseOrder()).intValue());
        assertEquals(4, e.maxBy(i -> ""+i).intValue());
    }
    
    @Test
    public void min() {
        Every<Integer> e = Every.of(1,2,3,4);
        assertEquals(1, e.min().get().intValue());
        assertEquals(4, e.minBy(Comparator.reverseOrder()).intValue());
        assertEquals(1, e.minBy(i -> ""+i).intValue());
    }
    
    @Test
    public void padTo() {
        Every<Integer> e = Every.of(1,2);
        assertEquals(Every.of(1,2,3,3), e.padTo(4, 3));
    }
    
    @Test
    public void reduces() {
        Every<Integer> e = Every.of(1,2,3,4);
        assertEquals(10, e.reduce((i1, i2) -> i1 + i2).intValue());
        assertEquals(10, e.reduceOption((i1, i2) -> i1 + i2).get().intValue());
        
        Every<String> e2 = Every.of("1","2","3","4");
        assertEquals("1234", e2.reduceLeft((s1, s2) -> s1 + s2));
        assertEquals("1234", e2.reduceLeftOption((s1, s2) -> s1 + s2).get());
        assertEquals("4321", e2.reduceRight((s2, s1) -> s1 + s2));
        assertEquals("4321", e2.reduceRightOption((s2, s1) -> s1 + s2).get());
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
