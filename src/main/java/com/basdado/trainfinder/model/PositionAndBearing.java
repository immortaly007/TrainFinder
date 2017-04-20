package com.basdado.trainfinder.model;

public class PositionAndBearing {
	
	private final LatLng position;
	private final double bearing;
	
	public PositionAndBearing(LatLng position, double bearing) {
		this.position = position;
		this.bearing = bearing;
	}

	public LatLng getPosition() {
		return position;
	}

	public double getBearing() {
		return bearing;
	}
}
