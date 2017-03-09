package com.basdado.trainfinder.util;

import java.util.HashMap;
import java.util.Map;

public class SparseGrid<T> {
	
	private final int width;
	private final int height;
	
	private final SparseGridDefaultValueProducer<? extends T> defaultProducer;
	
	private Map<GridCoord, T> grid;
	
	public SparseGrid(int width, int height, SparseGridDefaultValueProducer<? extends T> defaultProducer) {
		this.width = width;
		this.height = height;
		this.defaultProducer = defaultProducer;
		this.grid = new HashMap<>();
	}
	
	public SparseGrid(int width, int height) {
		this(width, height, (x, y) -> null);
	}
	
	public T get(int x, int y) {
		T value = grid.get(new GridCoord(x, y));
		if (value == null) {
			value = defaultProducer.get(x, y);
			set(x, y, value);
		}
		return value;
	}
	
	public void set(int x, int y, T value) {
		grid.put(new GridCoord(x, y), value);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public class GridCoord {
		private final int x;
		private final int y;
		
		public GridCoord(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
		
		@Override
		public int hashCode() {
			return x + width * y;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof SparseGrid.GridCoord)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			SparseGrid<T>.GridCoord other = (SparseGrid<T>.GridCoord)obj;
			return other.getX() == this.x && other.getY() == this.y;
		}
	}
	
	@FunctionalInterface
	public static interface SparseGridDefaultValueProducer<T> {
		T get(int x, int y);
	}

}
