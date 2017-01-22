package com.basdado.trainfinder.controller;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.data.DeparturesRepository;
import com.basdado.trainfinder.data.StationRepository;
import com.basdado.trainfinder.model.Departure;
import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Ride;
import com.basdado.trainfinder.model.RideKey;
import com.basdado.trainfinder.model.RideStop;
import com.basdado.trainfinder.model.Station;

@Singleton
@Startup
public class TrainTrackingController {
	
	private static final Logger logger = LoggerFactory.getLogger(TrainTrackingController.class);
	
	private static final LatLonCoordinate STATION_FILTER_MAX = new LatLonCoordinate(51.777160, 6.4139832);
	private static final LatLonCoordinate STATION_FILTER_MIN = new LatLonCoordinate(50.649176, 4.58460);
	
	private static final Duration MAXIMUM_RIDE_LENGTH = Duration.ofHours(8);
	
	@Inject StationRepository stationRepo;
	@Inject DeparturesRepository departuresRepo;
	
	Map<RideKey, Ride> rides;
	
	public TrainTrackingController() {
		rides = new HashMap<>();
	}
	
	@Schedule(hour="*",minute="0,5,10,15,25,30,35,40,45,50,55")
	public void RefreshDepartures() {
		
		Collection<Station> stations = getStations();
		
		for(Station station: stations) {
			
			logger.info("Getting departures at " + station.getFullName());
			
			final Collection<Departure> departures = departuresRepo.getDeparturesAt(station);
			
			for (Departure departure : departures) {
				Ride ride = getRide(departure);
				ride.addStop(new RideStop(departure.getStation(), departure.getDepartureTime(), departure.getDelay(), departure.getTrack()));
			}
		}
		
		List<Ride> sortedRides = rides.values().stream().sorted((r1, r2) -> r1.getRideCode().compareTo(r2.getRideCode())).collect(Collectors.toList());
		for (Ride ride: sortedRides) {
			logger.info("Found ride: " + ride);
		}
	}
	
	private Ride getRide(Departure departure) {
		
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
					if (ride.getStops().get(0).getDepartureTime().isBefore(minimumRideStart)) {
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
					RideStop lastKnownStop = rideTomorrow.getStops().get(rideTomorrow.getStops().size() - 1);
					if (lastKnownStop.getDepartureTime().isBefore(maximumRideEnd)) {
						// if we actually found a wrongly categorized ride, we need a new one.
						Ride newRide = new Ride(departure.getDepartureTime().toLocalDate(), departure.getRideNumber(), departure.getFinalDestination());
						rides.remove(rkTomorrow);
						for(RideStop s: rideTomorrow.getStops()) {
							newRide.addStop(new RideStop(s.getStation(), s.getDepartureTime(), s.getDelay(), s.getTrack()));
						}
						rides.put(new RideKey(newRide), newRide);
						ride = newRide;
					}
				}
			}
		}
		
		
		
		if (ride == null) {
			// If we still haven't been able to find the ride, it doesn't exist yet, so we create it
			ride = new Ride(departure.getDepartureTime().toLocalDate(), departure.getRideNumber(), departure.getFinalDestination());
			rides.put(new RideKey(ride), ride);
		}
		
		return ride;
	}
	
	@PostConstruct
	public void init() {
		RefreshDepartures();
	}
	
	private Collection<Station> getStations() {
		
		Collection<Station> stations = stationRepo.getStations();
		return stations.stream()
				.filter(s -> 
					between(STATION_FILTER_MIN.getLatitude(), STATION_FILTER_MAX.getLatitude(), s.getLocation().getLatitude()) &&
					between(STATION_FILTER_MIN.getLongitude(), STATION_FILTER_MAX.getLongitude(), s.getLocation().getLongitude()) &&
					"NL".equals(s.getCountryCode()))
				.collect(Collectors.toList());
	}
	
	private static boolean between(double min, double max, double x) {
		return x >= min && x <= max;
	}
	
}
