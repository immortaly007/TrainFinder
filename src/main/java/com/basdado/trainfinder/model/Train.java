package com.basdado.trainfinder.model;

import java.time.OffsetDateTime;

public class Train {
	
	private final LatLonCoordinate position;
	private final Station departureStation;
	private final OffsetDateTime plannedDepartureTime;
	private final OffsetDateTime actualDepartureTime;
	private final Station arrivalStation;
	private final OffsetDateTime plannedArrivalTime;
	private final OffsetDateTime actualArrivalTime;
	private final String rideCode;
	private final String carrier;
	private final String trainType;

	
	public Train(LatLonCoordinate position, Station departureStation, OffsetDateTime plannedDepartureTime,
			OffsetDateTime actualDepartureTime, Station arrivalStation, OffsetDateTime plannedArrivalTime,
			OffsetDateTime actualArrivalTime, String rideCode, String carrier, String trainType) {
		this.position = position;
		this.departureStation = departureStation;
		this.plannedDepartureTime = plannedDepartureTime;
		this.actualDepartureTime = actualDepartureTime;
		this.arrivalStation = arrivalStation;
		this.plannedArrivalTime = plannedArrivalTime;
		this.actualArrivalTime = actualArrivalTime;
		this.rideCode = rideCode;
		this.carrier = carrier;
		this.trainType = trainType;
	}

	public String getRideCode() {
		return rideCode;
	}

	public LatLonCoordinate getPosition() {
		return position;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public OffsetDateTime getPlannedDepartureTime() {
		return plannedDepartureTime;
	}
	
	public OffsetDateTime getActualDepartureTime() {
		return actualDepartureTime;
	}

	public Station getArrivalStation() {
		return arrivalStation;
	}

	public OffsetDateTime getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public OffsetDateTime getActualArrivalTime() {
		return actualArrivalTime;
	}

	public String getCarrier() {
		return carrier;
	}
	
	public String getTrainType() {
		return trainType;
	}
}
