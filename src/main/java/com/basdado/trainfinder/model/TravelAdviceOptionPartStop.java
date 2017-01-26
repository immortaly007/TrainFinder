package com.basdado.trainfinder.model;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TravelAdviceOptionPartStop {
	
	private final Station station;
	private final OffsetDateTime time;
	private final Duration departureDelay;
	private final String track;
	
	public TravelAdviceOptionPartStop(Station station, OffsetDateTime time, Duration departureDelay, String track) {

		this.station = station;
		this.time = time;
		this.departureDelay = departureDelay;
		this.track = track;
	}

	public Station getStation() {
		return station;
	}

	public OffsetDateTime getTime() {
		return time;
	}

	public Duration getDepartureDelay() {
		return departureDelay;
	}

	public String getTrack() {
		return track;
	}
	
	@Override
	public String toString() {
		return station.getFullName() + ", track: " + track + 
				"(" + time + ((departureDelay == null || departureDelay.isZero()) ? "" : " +" + departureDelay.toMinutes() + " min") + ")";
	}
	
}
