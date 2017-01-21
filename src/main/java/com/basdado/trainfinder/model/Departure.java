package com.basdado.trainfinder.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

public class Departure implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Station station;
	private final String rideNumber;
	private final OffsetDateTime departureTime;
	private final Duration delay;
	private final String track;
	
	public Departure(Station station, String rideNumber, OffsetDateTime departureTime, Duration delay, String track) {
		super();
		this.station = station;
		this.rideNumber = rideNumber;
		this.departureTime = departureTime;
		this.delay = delay;
		this.track = track;
	}

	public Station getStation() {
		return station;
	}
	
	public String getRideNumber() {
		return rideNumber;
	}

	public OffsetDateTime getDepartureTime() {
		return departureTime;
	}

	public Duration getDelay() {
		return delay;
	}
	
	public String getTrack() {
		return track;
	}
}
