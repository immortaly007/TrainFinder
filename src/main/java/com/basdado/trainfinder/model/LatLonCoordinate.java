package com.basdado.trainfinder.model;

import java.io.Serializable;

public class LatLonCoordinate implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final double latitude;
	private final double longitude;
	
	public LatLonCoordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null || !(obj instanceof LatLonCoordinate)) return false;
		if (this == obj) return true;
		
		LatLonCoordinate other = (LatLonCoordinate)obj;
		return this.getLatitude() == other.getLatitude() && this.getLongitude() == other.getLongitude();
		
	}
	
}
