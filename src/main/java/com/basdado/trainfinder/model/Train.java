package com.basdado.trainfinder.model;

import java.time.OffsetDateTime;

public class Train {
	
	private LatLonCoordinate position;
	private Station departureStation;
	private OffsetDateTime plannedDepartureTime;
	private OffsetDateTime actualDepartureTime;
	private Station arrivalStation;
	private OffsetDateTime plannedArrivalTime;
	private OffsetDateTime actualArrivalTime;
	private String rideCode;
	
	public String getRideCode() {
		return rideCode;
	}

	public void setRideCode(String rideCode) {
		this.rideCode = rideCode;
	}

	public LatLonCoordinate getPosition() {
		return position;
	}

	public void setPosition(LatLonCoordinate position) {
		this.position = position;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public void setDepartureStation(Station departureStation) {
		this.departureStation = departureStation;
	}

	public OffsetDateTime getPlannedDepartureTime() {
		return plannedDepartureTime;
	}

	public void setPlannedDepartureTime(OffsetDateTime plannedDepartureTime) {
		this.plannedDepartureTime = plannedDepartureTime;
	}

	public OffsetDateTime getActualDepartureTime() {
		return actualDepartureTime;
	}

	public void setActualDepartureTime(OffsetDateTime actualDepartureTime) {
		this.actualDepartureTime = actualDepartureTime;
	}

	public Station getArrivalStation() {
		return arrivalStation;
	}

	public void setArrivalStation(Station arrivalStation) {
		this.arrivalStation = arrivalStation;
	}

	public OffsetDateTime getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public void setPlannedArrivalTime(OffsetDateTime plannedArrivalTime) {
		this.plannedArrivalTime = plannedArrivalTime;
	}

	public OffsetDateTime getActualArrivalTime() {
		return actualArrivalTime;
	}

	public void setActualArrivalTime(OffsetDateTime actualArrivalTime) {
		this.actualArrivalTime = actualArrivalTime;
	}
	
	

}
