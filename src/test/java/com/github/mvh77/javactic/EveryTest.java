package com.github.mvh77.javactic;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

import com.github.mvh77.javactic.Every;

public class EveryTest {

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
