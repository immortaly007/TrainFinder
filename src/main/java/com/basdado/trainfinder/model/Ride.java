package com.basdado.trainfinder.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Ride {
	
	private final LocalDate startDate;
	private final String rideCode;
	private final Station destination;	
	
	private List<RideStop> stops;
	
	public Ride(LocalDate startDate, String rideCode, Station destination) {
		this.startDate = startDate;
		this.rideCode = rideCode;
		this.destination = destination;
		stops = new ArrayList<>();
	}
	
	public LocalDate getStartDate() {
		return startDate;
	}
	
	public String getRideCode() {
		return rideCode;
	}
	
	public Station getDestination() {
		return destination;
	}
	
	public List<RideStop> getStops() {
		// Don't allow editing departures directly
		return Collections.unmodifiableList(stops);
	}
	
	public void addStop(RideStop newStop) {
		
		if (newStop == null) return;
		
		// If we already have a departure for the given station is this ride, we remove is so we can replace it by our new departure
		for(Iterator<RideStop> stopIt = stops.iterator(); stopIt.hasNext();) {
			
			RideStop s = stopIt.next();
			if (s.getStation().equals(newStop.getStation())) {
				stopIt.remove();
			}
		}
		
		// Find the first departure after the new one
		int nextStopIdx = 0;
		for(RideStop s : stops) {
			if (s.getDepartureTime().isAfter(newStop.getDepartureTime())) {
				break;
			}
			nextStopIdx++;
		}
		
		int newDepartureIdx = nextStopIdx == 0 ? 0 : nextStopIdx;
		stops.add(newDepartureIdx, newStop);
		
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Ride " + getRideCode() + " on " + getStartDate());
		if (!stops.isEmpty()) {
			builder.append(", stops: ");
		}
		for (RideStop stop : stops) {
			builder.append(stop.getStation().getShortName() + "(" + stop.getDepartureTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + 
					(stop.getDelay() != null && !stop.getDelay().isZero() ? "+" + stop.getDelay().toString() : "") + ")");
			builder.append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
	
}
