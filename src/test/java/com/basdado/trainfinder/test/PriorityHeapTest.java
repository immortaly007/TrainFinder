package com.basdado.trainfinder.test;

import org.junit.Test;

import com.basdado.trainfinder.util.PriorityHeap;

import org.junit.Assert;

public class PriorityHeapTest {
	
	@Test
	public void singleItemTest() {
		
		PriorityHeap<String> heap = new PriorityHeap<String>();
		heap.add("a", 1.0);
		Assert.assertEquals("a", heap.remove());
	}
	
	@Test
	public void resizeTest() {
		
		PriorityHeap<String> heap = new PriorityHeap<>(1);
		heap.add("a", 1.0);
		heap.add("b", 2.0);
		Assert.assertEquals("a", heap.remove());
		Assert.assertEquals("b", heap.remove());
	}
	
	@Test
	public void orderTest() {
		
		PriorityHeap<String> heap = new PriorityHeap<>();
		heap.add("b", 2.0);
		heap.add("c", 3.0);
		heap.add("d", 4.0);
		heap.add("a", 1.0);
		
		Assert.assertEquals("a", heap.remove());
		Assert.assertEquals("b", heap.remove());
		Assert.assertEquals("c", heap.remove());
		Assert.assertEquals("d", heap.remove());
		
	}
	
	@Test
	public void orderTest2() {
		
		PriorityHeap<String> heap = new PriorityHeap<>();
		heap.add("a", 1.0);
		heap.add("b", 2.0);
		heap.add("c", 3.0);
		heap.add("d", 4.0);
		
		Assert.assertEquals("a", heap.remove());
		Assert.assertEquals("b", heap.remove());
		Assert.assertEquals("c", heap.remove());
		Assert.assertEquals("d", heap.remove());
		
	}
	
}
