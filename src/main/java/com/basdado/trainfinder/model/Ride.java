package com.basdado.trainfinder.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Ride {
	
	private final LocalDate startDate;
	private final String rideCode;
	private final Station destination;	
	private final String carrier;
	private final String trainType;
	
	private List<RideStop> stops;
	
	public Ride(LocalDate startDate, String rideCode, Station destination, String carrier, String trainType) {
		this.startDate = startDate;
		this.rideCode = rideCode;
		this.destination = destination;
		this.carrier = carrier;
		this.trainType = trainType;
		stops = new CopyOnWriteArrayList<>();
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
	
	public String getCarrier() {
		return carrier;
	}
	
	public String getTrainType() {
		return trainType;
	}
	
	public Collection<RideStop> getStops() {
		// Don't allow editing departures directly
		return Collections.unmodifiableCollection(stops);
	}
	
	/**
	 * @return True if the last stop if known, i.e. if the last stop in #stops is at the destination.
	 */
	public boolean isLastStopKnown() {
		if (stops.isEmpty()) return false;
		return getLastKnownStop().getStation().equals(destination);
	}
	
	/**
	 * Adds the stop to this ride. If a stop at the given station already exists it will be overriden. 
	 * @param newStop
	 */
	public void addStop(RideStop newStop) {
		
		if (newStop == null) return;
		
		boolean stopFound = false;
		int i = 0;
		// If we already have a departure for the given station is this ride, we remove is so we can replace it by our new departure
		for(RideStop s : stops) {
			if (s.getStation().equals(newStop.getStation())) {
				stopFound = true;
				stops.set(i, newStop);
			}
			i++;
		}
		
		// Find the first departure after the new one
		if (!stopFound) {
			int nextStopIdx = getNextStopIndex(newStop.getDepartureTime());		
			stops.add(nextStopIdx, newStop);
		}
		
	}
	
	private int getNextStopIndex(OffsetDateTime time) {
		
		int nextStopIdx = 0;
		for(RideStop s : stops) {
			if (s.getDepartureTime().isAfter(time)) {
				break;
			}
			nextStopIdx++;
		}
		return nextStopIdx;
	}
	
	private int getActualNextStopIndex(OffsetDateTime time) {
		int nextStopIdx = 0;
		for(RideStop s : stops) {
			if (s.getActualDepartureTime().isAfter(time)) {
				break;
			}
			nextStopIdx++;
		}
		return nextStopIdx;
	}
	
	/**
	 * @return The last stop of which the details (departure time etc.) are known. Not necessarily the same as the final destination.
	 */
	public RideStop getLastKnownStop() {
		
		if (stops == null || stops.isEmpty()) return null;
		return stops.get(stops.size() - 1);
	}
	
	/**
	 * @return The first stop of which details are known.
	 */
	public RideStop getFirstKnownStop() {
		if (stops == null || stops.isEmpty()) return null;
		return stops.get(0);
	}
	
	/**
	 * Returns the previous stop at the given time based on the planned departure times of the train.
	 * @param time
	 * @return The last stop the train departed from (given the time). Will return the final destination if this ride is over. Does not account for delays. 
	 */
	public RideStop getPreviousStop(OffsetDateTime time) {
		
		if (stops == null || stops.isEmpty()) return null;
		
		int nextStopIdx = getNextStopIndex(time);
		if (nextStopIdx == 0) return null;
		return stops.get(nextStopIdx - 1);
	}
	
	/**
	 *  Returns the previous stop at the given time based on the actual departure times of the train.
	 * @param time
	 * @return The last stop the train departed from (given the time). Will return the final destination if this ride is over. Does not account for delays. 
	 */
	public RideStop getActualPreviousStop(OffsetDateTime time) {
		
		if (stops == null || stops.isEmpty()) return null;
		
		int nextStopIdx = getActualNextStopIndex(time);
		if (nextStopIdx == 0) return null;
		return stops.get(nextStopIdx - 1);
	}
	
	/**
	 * @param time
	 * @return The next stop the train arrives at given a time, or null if the given time is after the last known stop. Returns the first stop if this ride
	 * hasn't departed yet. Does not account for delays. 
	 */
	public RideStop getNextStop(OffsetDateTime time) {
		
		if (stops == null || stops.isEmpty()) return null;
		
		int nextStopIdx = getNextStopIndex(time);
		if (nextStopIdx == stops.size()) {
			return null;
		} else {
			return stops.get(nextStopIdx);
		}
		
	}
	
	/**
	 * @param time
	 * @return The next stop the train arrives at given a time, or null if the given time is after the last known stop. Returns the first stop if this ride
	 * hasn't departed yet. Does not account for delays. 
	 */
	public RideStop getActualNextStop(OffsetDateTime time) {
		
		if (stops == null || stops.isEmpty()) return null;
		
		int nextStopIdx = getActualNextStopIndex(time);
		if (nextStopIdx == stops.size()) {
			return null;
		} else {
			return stops.get(nextStopIdx);
		}
		
	}
	
	public RideStop getStopBefore(RideStop stop) {
		
		int idx = stops.indexOf(stop);
		if (idx <= 0) return null;
		return stops.get(idx - 1);
		
	}
	
	public RideStop getStopAfter(RideStop stop) {
		
		int idx = stops.indexOf(stop);
		if (idx == -1 || idx + 1 >= stops.size()) return null;
		return stops.get(idx + 1);
	}

	public RideStop getStopAt(Station station) {
		
		for (RideStop stop : stops) {
			if (stop.getStation().equals(station)) {
				return stop;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Ride " + getRideCode() + " on " + getStartDate() + " to " + (getDestination() == null ? "unknown" : getDestination().getShortName()));
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
