package com.basdado.trainfinder.model;

public class LatLngBounds {

	private final LatLng southWest;
	private final LatLng northEast;
	
	public LatLngBounds(LatLng southWest, LatLng northEast) {
		this.southWest = southWest;
		this.northEast = northEast;
	}
	
	public LatLngBounds(double minLat, double minLng, double maxLat, double maxLng) {
		this.southWest = new LatLng(minLat, minLng);
		this.northEast = new LatLng(maxLat, maxLng);
	}
	
	public LatLng getNorthEast() {
		return northEast;
	}
	
	public LatLng getSouthWest() {
		return southWest;
	}
	
	/**
	 * Returns if the given pos it within the bounds (e.g. the square that is given by the bounds contains
	 * the given position).
	 * @param pos The position to check
	 * @return true iff pos is within the bounds
	 */
	public boolean contains(LatLng pos) {
		return southWest.getLatitude() <= pos.getLatitude() &&
				southWest.getLongitude() <= pos.getLongitude() &&
				northEast.getLatitude() >= pos.getLatitude() &&
				northEast.getLongitude() >= pos.getLongitude();
	}
	
}
