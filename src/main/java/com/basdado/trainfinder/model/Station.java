package com.basdado.trainfinder.model;

import java.io.Serializable;

public class Station implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String code;
	
	private final String shortName;
	
	private final String fullName;
	
	private final LatLng location;
	
	private final String countryCode;

	public Station(String code, String shortName, String fullName, LatLng location, String countryCode) {
		this.code = code;
		this.shortName = shortName;
		this.fullName = fullName;
		this.location = location;
		this.countryCode = countryCode;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCode() {
		return code;
	}

	public String getShortName() {
		return shortName;
	}
	
	public String getFullName() {
		return fullName;
	}

	public LatLng getLocation() {
		return location;
	}
	
	public String getCountryCode() {
		return countryCode;
	}
	
	/**
	 * Simple equality comparer. Only compares the code, as this uniquely identifies a station.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null || !(obj instanceof Station)) return false;
		if (this == obj) return true;
		
		Station other = (Station)obj;
		return this.code.equals(other.getCode());
	}
	
	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
	
}
