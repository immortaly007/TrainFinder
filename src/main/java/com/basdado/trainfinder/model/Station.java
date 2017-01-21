package com.basdado.trainfinder.model;

import java.io.Serializable;

public class Station implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String code;
	
	private final String name;
	
	private final LatLonCoordinate location;
	
	private final String countryCode;

	public Station(String code, String name, LatLonCoordinate location, String countryCode) {
		this.code = code;
		this.name = name;
		this.location = location;
		this.countryCode = countryCode;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public LatLonCoordinate getLocation() {
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
	
}
