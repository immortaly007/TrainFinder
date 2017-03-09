package com.basdado.trainfinder.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;

import com.basdado.trainfinder.model.Departure;
import com.basdado.trainfinder.model.Ride;
import com.basdado.trainfinder.model.RideKey;
import com.basdado.trainfinder.model.RideStop;

@Singleton
public class TrainRideDataManager {
	
	private static final Duration MAXIMUM_RIDE_LENGTH = Duration.ofHours(8);
	private static final Duration CLEAN_TIMEOUT = Duration.ofMinutes(10);
	
	private LocalDateTime lastCleanTime;
	private Map<RideKey, Ride> rides;
	
	public TrainRideDataManager() {
		rides = new ConcurrentHashMap<>();
		lastCleanTime = LocalDateTime.MIN;
	}
	
	public List<Ride> getRides() {
		
		cleanRides();
		List<Ride> res = new ArrayList<>(rides.size());
		for (Ride ride : rides.values()) {
			res.add(ride);
		}
		return res;
		
	}
	
	private void cleanRides() {
		
		// Short-circuit if we cleaned recently.
		if (lastCleanTime.isAfter(LocalDateTime.now().minus(CLEAN_TIMEOUT))) {
			return;
		}
		
		List<RideKey> inactiveRides = new ArrayList<>();
		
		OffsetDateTime oldestAllowedRide = OffsetDateTime.now().minus(MAXIMUM_RIDE_LENGTH);
		
		for (Entry<RideKey, Ride> rideKeyValuePair : rides.entrySet()) {
			
			RideKey rideKey = rideKeyValuePair.getKey();
			Ride ride = rideKeyValuePair.getValue();
			
			RideStop lastStop = ride.getLastKnownStop();
			
			if (lastStop == null || // If we were not able to find any stops for a ride, then just remove it (probably never happens, saveguard)
					lastStop.getDepartureTime().isBefore(oldestAllowedRide) // or if the last stop was longer ago than the MAXIMUM_RIDE_LENGTH
					) { 
				inactiveRides.add(rideKey);
			}
		}
		
		for (RideKey inactiveRideKey : inactiveRides) {
			rides.remove(inactiveRideKey);
		}
		
	}
	
	public Ride findOrCreateRide(Departure departure) {
		
		// RideNumber/RideCode is unique each day. We have to find out however if the ride started yesterday or today
		
		RideKey rk = new RideKey(departure.getDepartureTime().toLocalDate(), departure.getRideNumber());
		Ride ride = rides.get(rk);
		if (ride == null) { 
			// Maybe the ride started yesterday
			OffsetDateTime minimumRideStart = departure.getDepartureTime().minus(MAXIMUM_RIDE_LENGTH);
			if (!minimumRideStart.toLocalDate().equals(departure.getDepartureTime().toLocalDate())) {
				RideKey rkYesterday = new RideKey(minimumRideStart.toLocalDate(), departure.getRideNumber());
				ride = rides.get(rkYesterday);
				
				if (ride != null && !ride.getStops().isEmpty()) {
					if (ride.getFirstKnownStop().getDepartureTime().isBefore(minimumRideStart)) {
						// If this ride was indeed yesterday, but it started way before the current departure, then this is not our ride
						ride = null;
					}
				}
			}
		}
		
		if (ride == null) {
			// Maybe our ride started at the end of the day, and another departure has registered this ride under the wrong ride key (i.e. for tomorrow)
			OffsetDateTime maximumRideEnd = departure.getDepartureTime().plus(MAXIMUM_RIDE_LENGTH);
			if (!maximumRideEnd.toLocalDate().equals(departure.getDepartureTime().toLocalDate())) {
				RideKey rkTomorrow = new RideKey(maximumRideEnd.toLocalDate(), departure.getRideNumber());
				Ride rideTomorrow = rides.get(rkTomorrow);
				
				if (rideTomorrow != null && !rideTomorrow.getStops().isEmpty()) {
					RideStop lastKnownStop = rideTomorrow.getLastKnownStop();
					if (lastKnownStop.getDepartureTime().isBefore(maximumRideEnd)) {
						// if we actually found a wrongly categorized ride, we need a new one.
						Ride newRide = new Ride(departure.getDepartureTime().toLocalDate(), departure.getRideNumber(), departure.getFinalDestination(), departure.getCarrier(), departure.getTrainType());
						rides.remove(rkTomorrow);
						for(RideStop s: rideTomorrow.getStops()) {
							newRide.addStop(s);
						}
						rides.put(new RideKey(newRide), newRide);
						ride = newRide;
					}
				}
			}
		}		
		
		if (ride == null) {
			// If we still haven't been able to find the ride, it doesn't exist yet, so we create it
			ride = new Ride(departure.getDepartureTime().toLocalDate(), departure.getRideNumber(), departure.getFinalDestination(), departure.getCarrier(), departure.getTrainType());
			rides.put(new RideKey(ride), ride);
		}
		
		// Sometimes ride destination are unclear at some when queried from some stations (e.g. when the train splits into another ride)
		// but at other stations are clear. In this case we need to replace the ride with the missing destination by one with a correct destination:
		if (ride != null && ride.getDestination() == null && departure.getFinalDestination() != null) {
			
			Ride newRide = new Ride(ride.getStartDate(), ride.getRideCode(), departure.getFinalDestination(), departure.getCarrier(), departure.getTrainType());
			for (RideStop s : ride.getStops()) {
				newRide.addStop(s);
			}
			rides.put(new RideKey(newRide), newRide); // override existing ride
			ride = newRide;			
		}
		
		return ride;
	}

}
