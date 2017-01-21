package com.basdado.trainfinder.model;

import java.time.LocalDate;
import java.util.Objects;

public class RideKey {

	private final LocalDate startDate;
	private final String rideCode;
	
	public RideKey(Ride ride) {
		this(ride.getStartDate(), ride.getRideCode());
	}
	
	public RideKey(LocalDate startDate, String rideCode) {
		this.startDate = startDate;
		this.rideCode = rideCode;
	}
	
	public LocalDate getStartDate() {
		return startDate;
	}
	
	public String getRideCode() {
		return rideCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null || !(obj instanceof RideKey)) return false;
		if (this == obj) return true;
		
		RideKey other = (RideKey) obj;
		return this.startDate.equals(other.getStartDate()) &&
				this.rideCode.equals(other.getRideCode());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(rideCode.hashCode(), startDate.hashCode());
		
	}
}
