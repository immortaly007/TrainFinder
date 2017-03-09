package com.basdado.trainfinder.model;

import com.basdado.trainfinder.util.CoordinateUtil;
import com.basdado.trainfinder.util.SparseGrid;

public class GeoGrid<T> {

	private final double tileSize;
	private final SparseGrid<T> grid;
	
	public GeoGrid(double tileSize) {
		int gridWidth = calculateHorizontalTileCount(tileSize);
		int gridHeight = calculateVerticalTileCount(tileSize);
		this.grid = new SparseGrid<T>(gridWidth, gridHeight);
		this.tileSize = tileSize;
	}
	
	public void setTileAt(LatLonCoordinate pos, T value) {
		grid.set(
				calculateHorizontalTileIdx(pos), 
				calculateVerticalTileIdx(pos),
				value);
	}
	
	public T getTileAt(LatLonCoordinate pos) {
		return getTile(
				calculateHorizontalTileIdx(pos),
				calculateVerticalTileIdx(pos));
	}
	
	public T getTile(int x, int y) {
		return grid.get(x, y);
	}
	
	public boolean isOnTile(LatLonCoordinate pos, int x, int y) {
		return calculateHorizontalTileIdx(pos) == x && calculateVerticalTileIdx(pos) == y;
	}
	
	public double getDistanceToTile(LatLonCoordinate pos, int x, int y) {
		
		if (isOnTile(pos, x, y)) {
			return 0;
		}
		
		double minLon = getMinimumLongitude(x);
		double maxLon = getMaximumLongitude(x);
		double minLat = getMinimumLatitude(y);
		double maxLat = getMaximumLatitude(y);
		
		double distance1 = Math.abs(CoordinateUtil.crossTrackDist(
				new LatLonCoordinate(minLat, minLon), 
				new LatLonCoordinate(minLat, maxLon),
				pos));
		double distance2 = Math.abs(CoordinateUtil.crossTrackDist(
				new LatLonCoordinate(minLat, maxLon), 
				new LatLonCoordinate(maxLat, maxLon),
				pos));
		double distance3 = Math.abs(CoordinateUtil.crossTrackDist(
				new LatLonCoordinate(maxLat, minLon), 
				new LatLonCoordinate(maxLat, maxLon),
				pos));
		double distance4 = Math.abs(CoordinateUtil.crossTrackDist(
				new LatLonCoordinate(minLat, minLon), 
				new LatLonCoordinate(maxLat, minLon),
				pos));
		
		return Math.min(
				Math.min(distance1, distance2),
				Math.min(distance3, distance4));
		
	}
	
	/**
	 * Calculates the distance of pos to the point that is farthest away from pos on the tile
	 * @param pos The coordinate for which the farthest away tile should be found
	 * @param x The x-index of the tile
	 * @param y The y-index of the tile
	 * @return The distance from pos to the corner of the tile that is farthest from pos.
	 */
	public double getMaximumDistanceOnTile(LatLonCoordinate pos, int x, int y) {
		
		// Note that the point furthest away from pos is always a corner, so we just check the distance to all four corners, and return the longest one.
		double minLon = getMinimumLongitude(x);
		double maxLon = getMaximumLongitude(x);
		double minLat = getMinimumLatitude(y);
		double maxLat = getMaximumLatitude(y);
		
		double distance1 = CoordinateUtil.dist(pos, new LatLonCoordinate(minLat, minLon));
		double distance2 = CoordinateUtil.dist(pos, new LatLonCoordinate(maxLat, minLon));
		double distance3 = CoordinateUtil.dist(pos, new LatLonCoordinate(minLat, maxLon));
		double distance4 = CoordinateUtil.dist(pos, new LatLonCoordinate(maxLat, maxLon));
		
		return Math.min(
				Math.min(distance1, distance2),
				Math.min(distance3, distance4));
	}
	
	private static int calculateHorizontalTileCount(double tileSize) {
		double totalLon = CoordinateUtil.MAX_LONGITUDE - CoordinateUtil.MIN_LONGITUDE;
		return (int)Math.ceil(totalLon / tileSize);
	}
	
	private static int calculateVerticalTileCount(double tileSize) {
		double totalLon = CoordinateUtil.MAX_LATITUDE - CoordinateUtil.MIN_LATITUDE;
		return (int)Math.ceil(totalLon / tileSize);
	}
	
	public int calculateHorizontalTileIdx(LatLonCoordinate coord) {
		return ((int)Math.floor((coord.getLongitude() - CoordinateUtil.MIN_LONGITUDE) / tileSize));
	}
	
	public int calculateVerticalTileIdx(LatLonCoordinate coord) {
		return ((int)Math.floor((coord.getLatitude() - CoordinateUtil.MIN_LATITUDE) / tileSize));
	}
	
	private double getMinimumLongitude(int x) {
		return CoordinateUtil.MIN_LONGITUDE + (x * tileSize);
	}
	
	private double getMaximumLongitude(int x) {
		return CoordinateUtil.MIN_LONGITUDE + ((x  + 1) * tileSize);
	}
	
	private double getMinimumLatitude(int y) {
		return CoordinateUtil.MIN_LATITUDE + (y * tileSize);
	}
	
	private double getMaximumLatitude(int y) {
		return CoordinateUtil.MIN_LATITUDE + ((y + 1) * tileSize);
	}
}
