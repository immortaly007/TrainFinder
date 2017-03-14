package com.basdado.trainfinder.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LatLng implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("lat")
	private final double latitude;
	
	@JsonProperty("lng")
	private final double longitude;
	
	public LatLng(double latitude, double longitude) {
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
		
		if (obj == null || !(obj instanceof LatLng)) return false;
		if (this == obj) return true;
		
		LatLng other = (LatLng)obj;
		return this.getLatitude() == other.getLatitude() && this.getLongitude() == other.getLongitude();
		
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(latitude).append(longitude).build();
		
	}
	
	@Override
	public String toString() {
		return "[" + latitude + "," + longitude + "]";
	}
	
}
