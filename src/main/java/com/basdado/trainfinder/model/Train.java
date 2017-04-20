package com.basdado.trainfinder.model;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Train {
	
	private final LatLng position;
	private final double bearing;
	private final String previousStop;
	private final OffsetDateTime plannedTimeAtPreviousStop;
	private final OffsetDateTime actualTimeAtPreviousStop;
	private final String nextStop;
	private final OffsetDateTime plannedTimeAtNextStop;
	private final OffsetDateTime actualTimeAtNextStop;
	private final String carrier;
	private final String trainType;
	private final String rideCode;
	private final List<TrainRideStop> stops;

	
	public Train(LatLng position, double bearing,
			Station previousStop, OffsetDateTime plannedTimeAtPreviousStop,	OffsetDateTime actualTimeAtPreviousStop,
			Station nextStop, OffsetDateTime plannedTimeAtNextStop,	OffsetDateTime actualTimeAtNextStop,			
			Ride ride) {
		this.position = position;
		this.bearing = bearing;
		this.previousStop = previousStop.getCode();
		this.plannedTimeAtPreviousStop = plannedTimeAtPreviousStop;
		this.actualTimeAtPreviousStop = actualTimeAtPreviousStop;
		this.nextStop = nextStop.getCode();
		this.plannedTimeAtNextStop = plannedTimeAtNextStop;
		this.actualTimeAtNextStop = actualTimeAtNextStop;
		this.carrier = ride.getCarrier();
		this.trainType = ride.getTrainType();
		this.rideCode = ride.getRideCode();
		this.stops = Collections.unmodifiableList(ride.getStops().stream().map(s -> new TrainRideStop(s)).collect(Collectors.toList()));
	}

	public List<TrainRideStop> getStops() {
		return stops;
	}
	
	public String getRideCode() {
		return rideCode;
	}

	public LatLng getPosition() {
		return position;
	}

	public String getPreviousStop() {
		return previousStop;
	}

	public OffsetDateTime getPlannedTimeAtPreviousStop() {
		return plannedTimeAtPreviousStop;
	}

	public OffsetDateTime getActualTimeAtPreviousStop() {
		return actualTimeAtPreviousStop;
	}

	public String getNextStop() {
		return nextStop;
	}

	public OffsetDateTime getPlannedTimeAtNextStop() {
		return plannedTimeAtNextStop;
	}

	public OffsetDateTime getActualTimeAtNextStop() {
		return actualTimeAtNextStop;
	}

	public String getCarrier() {
		return carrier;
	}
	
	public String getTrainType() {
		return trainType;
	}
}
