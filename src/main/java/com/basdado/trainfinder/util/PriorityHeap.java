package com.basdado.trainfinder.util;

import java.util.Arrays;

/**
 * Array-based priority heap implementation.
 * 
 * A priority heap is essentially a complete binary tree, in which each element
 * is smaller than it's children (based on some criteria). 
 * This allows for O(1) retrieval of the smallest node in the tree, because this
 * node is always the root, and O(log N) performance for adding nodes to the tree.
 * 
 * @author Immortaly007
 */
public class PriorityHeap<T> {

	/**
	 * The complete binary tree represented as an array.
	 * The root is at index 0. The nodes are stored in level order, e.g.:
	 * <pre>
	 *           0
	 *         /   \
	 *        1     2
	 *       / \   / \
	 *      3   4 5   6
	 * </pre>
	 * This ensures that you can find the left child index by:
	 * <code>currentIdx * 2</code>, and the right child index by
	 * <code>currentIdx * 2 + 1</code>. Similarly, the parent node
	 * can be find by using an integer division by two, as in:
	 * <code>currentIdx / 2</code> or <code>currentIdx << 1</code>.
	 * 
	 */
	private PriorityHeapNode<T>[] tree;
	private int size;
	
	
	@SuppressWarnings("unchecked")
	public PriorityHeap(int capacity) {
		tree = new PriorityHeapNode[capacity];
	}
	
	public PriorityHeap() {
		this(8);
	}
	
	/**
	 * Adds an item with the given rating to the priority heap (O(log N)).
	 * @param item
	 * @param rating
	 */
	public void add(T item, double rating) {
		
		if (size >= tree.length) {
			resize(size * 2);
		}
		
		int newNodeIdx = size++;
		tree[newNodeIdx] = new PriorityHeapNode<T>(item, rating);
		upheap(newNodeIdx);
	}
	
	public boolean contains(T item) {
		return indexOf(item) >= 0;
	}
	
	public double getRating(T item) {
		int itemIdx = indexOf(item);
		if (itemIdx < 0) return Double.MAX_VALUE;
		else return tree[itemIdx].getRating();
	}
	
	/**
	 * Updates the rating of the given item (possibly moving it in the heap).
	 * Returns false if the item was not found (and thus the rating wasn't updated);
	 * returns true if the item was found and the rating is successfully updated.
	 * @param item The item to find
	 * @param rating The new rating for that item
	 * @return True iff the update was successful.
	 */
	public boolean updateRating(T item, double newRating) {
		
		int itemIdx = indexOf(item);
		if (itemIdx == -1) {
			return false;
		}
		
		double curRating = tree[itemIdx].getRating();
		tree[itemIdx].setRating(newRating);
		if (newRating <= curRating) {
			upheap(itemIdx);
		} else {
			downheap(itemIdx);
		}
		return true;
	}
	
	/**
	 * Updates the rating of the given item if the current rating of the item is 
	 * higher than the new rating. Returns true if the rating was updated,
	 * and false if the item was not found or the rating was already higher than
	 * the given rating.
	 * @param item The item for which the rating needs to be updated
	 * @param newRating The new rating of the item.
	 * @return
	 */
	public boolean updateMinRating(T item, double newRating) {
		
		int itemIdx = indexOf(item);
		if (itemIdx == 1) {
			return false;
		}
		
		double curRating = tree[itemIdx].getRating();
		if (newRating < curRating) {
			tree[itemIdx].setRating(newRating);
			upheap(itemIdx);
			return true;
		} else {
			return false;
		}
		
	}
	
	private int indexOf(T item) {
		
		for (int i = 0; i < size; i++) {
			if (tree[i].getItem().equals(item)) {
				return i;
			}
		}
		return -1;
	} 
	
	/**
	 * @return The first (smallest) item on the heap (O(1)).
	 */
	public T peek() {
		if (isEmpty()) return null;
		else return tree[0].getItem();
	}
	
	/**
	 * @return The rating of the first (smallest) item on the heap (O(1)).
	 */
	public double peekRating() {
		if (isEmpty()) return Double.MAX_VALUE;
		else return tree[0].getRating();
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int size() {
		return size;
	}
	
	/**
	 * Removes the smallest node from the heap and returns it (O(log N)).
	 * @return The smallest node.
	 */
	public T remove() {
		
		if (size == 0) return null; // Or throw exception?
		
		PriorityHeapNode<T> removedNode = tree[0];
		size--;
		if (size > 0) {
			tree[0] = tree[size]; // Put the last added node at the root
			downheap(0); // And make sure the heap is ordered again
		}
		
		return removedNode.getItem();
	}
	
	/**
	 * Resizes the heap to the new capacity.
	 * @param newCapacity
	 */
	public void resize(int newCapacity) {
		tree = Arrays.copyOf(tree, newCapacity);
	}
	
	/**
	 * From the node with the given index, goes up in the tree and swaps nodes
	 * if the order is not correct.
	 * @param index The index of the node to be considered
	 */
	private void upheap(int index) {
		int parentIdx = parentIdx(index);
		if (tree[parentIdx].getRating() > tree[index].getRating()) {
			swap(index, parentIdx);
			upheap(parentIdx);
		}
	}
	
	/**
	 * From the node with the given index, goes down in the tree, and swaps
	 * with the child smallest child if that child is smaller than the current
	 * node.
	 * @param index The index of the node to be considered.
	 */
	private void downheap(int index) {
		
		if (index >= size) {
			return;
		}
		
		int leftChildIdx = leftChildIdx(index);
		int rightChildIdx = rightChildIdx(index);
		
		// Swap with the smallest child (if that child is bigger than this node):
		double nodeRating = tree[index].getRating();
		double leftChildRating = leftChildIdx < size ? tree[leftChildIdx].getRating() : Double.MAX_VALUE;
		double rightChildRating = rightChildIdx < size ? tree[rightChildIdx].getRating() : Double.MAX_VALUE;
		
		if (leftChildRating <= rightChildRating && leftChildRating < nodeRating) { // if the left child is the smallest and we need to swap with it:
			swap(leftChildIdx, index);
			downheap(leftChildIdx);
		} else if (rightChildRating < leftChildRating && rightChildRating < nodeRating) { // if the right child is the smallest and we need to swap with it:
			swap(rightChildIdx, index);
			downheap(rightChildIdx);
		}
	}
	
	private int parentIdx(int index) {
		return index / 2;
	}
	
	private int leftChildIdx(int index) {
		return index * 2;
	}
	
	private int rightChildIdx(int index) {
		return index * 2 + 1;
	}
	
	private void swap(int aIdx, int bIdx) {
		
		PriorityHeapNode<T> temp = tree[aIdx];
		tree[aIdx] = tree[bIdx];
		tree[bIdx] = temp;
		
	}
	
	private static final class PriorityHeapNode<T> {
		
		private final T item;
		private double rating;
		
		public PriorityHeapNode(T item, double rating) {
			this.item = item;
			this.rating = rating;
		}
		
		public T getItem() {
			return item;
		}
		
		public double getRating() {
			return rating;
		}
		
		public void setRating(double rating) {
			this.rating = rating;
		}
		
	}
}
