package com.basdado.trainfinder.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

public class RideStop implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Station station;
	private final OffsetDateTime departureTime;
	private final Duration delay;
	private final String track;
	
	public RideStop(Station station, OffsetDateTime departureTime, Duration delay, String track) {
		super();
		this.station = station;
		this.departureTime = departureTime;
		this.delay = delay;
		this.track = track;
	}

	public Station getStation() {
		return station;
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
	
	public OffsetDateTime getActualDepartureTime() {
		OffsetDateTime actualDepartureTime = departureTime;
		if (delay != null && !delay.isZero()) {
			actualDepartureTime = actualDepartureTime.plus(delay);
		}
		return actualDepartureTime;
	}
}
