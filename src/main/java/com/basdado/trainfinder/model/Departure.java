package com.basdado.trainfinder.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

public class Departure implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Station station;
	private final Station finalDestination;
	private final String rideNumber;
	private final OffsetDateTime departureTime;
	private final Duration delay;
	private final String track;
	
	public Departure(Station station, Station finalDestination, String rideNumber, OffsetDateTime departureTime, Duration delay, String track) {
		super();
		this.station = station;
		this.finalDestination = finalDestination;
		this.rideNumber = rideNumber;
		this.departureTime = departureTime;
		this.delay = delay;
		this.track = track;
	}

	public Station getStation() {
		return station;
	}
	
	public Station getFinalDestination() {
		return finalDestination;
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
