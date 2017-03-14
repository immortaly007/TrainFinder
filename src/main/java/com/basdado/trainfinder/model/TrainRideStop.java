package com.basdado.trainfinder.model;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TrainRideStop {
	
	private final String station;
	private final OffsetDateTime departure;
	private final Duration delay;
	private final String track;
	
	public TrainRideStop(String stationCode, OffsetDateTime departureTime, Duration delay, String track) {
		this.station = stationCode;
		this.departure = departureTime;
		this.delay = delay;
		this.track = track;
	}
	
	public TrainRideStop(RideStop stop) {
		this.station = stop.getStation().getCode();
		this.departure = stop.getDepartureTime();
		this.delay = stop.getDelay();
		this.track = stop.getTrack();
	}

	public String getStation() {
		return station;
	}

	public OffsetDateTime getDepartureTime() {
		return departure;
	}

	public Duration getDelay() {
		return delay;
	}
	
	public String getTrack() {
		return track;
	}

}
