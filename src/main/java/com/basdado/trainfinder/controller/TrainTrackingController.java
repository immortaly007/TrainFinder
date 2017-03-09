package com.basdado.trainfinder.controller;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.basdado.trainfinder.data.TravelAdviceRepository;
import com.basdado.trainfinder.data.TravelAdviceRepository.TimeType;
import com.basdado.trainfinder.exception.TravelAdviceException;
import com.basdado.trainfinder.model.Departure;
import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Railway;
import com.basdado.trainfinder.model.Ride;
import com.basdado.trainfinder.model.RideStop;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.model.TravelAdvice;
import com.basdado.trainfinder.model.TravelAdviceOption;
import com.basdado.trainfinder.model.TravelAdviceOptionPart;
import com.basdado.trainfinder.model.TravelAdviceOptionPartStop;
import com.basdado.trainfinder.util.ObjectUtil;

@Singleton
@Startup
public class TrainTrackingController {
	
	private static final Logger logger = LoggerFactory.getLogger(TrainTrackingController.class);
	
	private static final LatLonCoordinate STATION_FILTER_MAX = new LatLonCoordinate(51.777160, 6.4139832);
	private static final LatLonCoordinate STATION_FILTER_MIN = new LatLonCoordinate(50.649176, 4.58460);
	

	/**
	 * The duration before a train reaches the stop before the final destination, at which we start querying the final stop arrival time.s 
	 */
	private static final Duration FINAL_DESTINATION_QUERY_TIME = Duration.ofMinutes(10);
	
	/**
	 * The duration for which the departure times at a station will not be updated if the last update indicates that there are no departures
	 */
	private static final Duration STATION_UPDATE_SKIP = Duration.ofMinutes(60);
	
	/**
	 * The maximum time between updates for a station.
	 */
	private static final Duration STATION_UPDATE_MAXIMUM_SKIP = Duration.ofMinutes(60); 
	
	/**
	 * A station will be updated if a train reaches the stop before that station within this timespan. 
	 */
	private static final Duration STATION_UPDATE_OFFSET = Duration.ofMinutes(5);
	
	@Inject StationRepository stationRepo;
	@Inject DeparturesRepository departuresRepo;
	@Inject TravelAdviceRepository travelAdviceRepo;
	@Inject TrainRoutingController trainRoutes;	
	@Inject TrainRideDataManager trainRideDataManager;
	
	private Map<Station, OffsetDateTime> nextUpdateTimes;
	private Map<Station, OffsetDateTime> lastUpdateTimes;
	
	public TrainTrackingController() {
		nextUpdateTimes = new HashMap<>();
		lastUpdateTimes = new HashMap<>();
	}
	
	
	@Schedule(hour="*",minute="*/5", persistent=false)
	public void RefreshDepartures() {
		
		Collection<Station> stations = getStations();
		
		Railway rw = trainRoutes.getRailway(stationRepo.getStationWithCode("HRT"), stationRepo.getStationWithCode("BR"));
		logger.info("Found railway: "  + rw);
		
		Collection<Station> stationsWorthUpdating = stations.stream().filter(s -> isStationWorthUpdating(s)).collect(Collectors.toList());
		
		for(Station station: stationsWorthUpdating) {

			logger.info("Getting departures at " + station.getFullName());
			
			final Collection<Departure> departures = departuresRepo.getDeparturesAt(station);
			
			for (Departure departure : departures) {
				Ride ride = trainRideDataManager.findOrCreateRide(departure);
				ride.addStop(new RideStop(departure.getStation(), departure.getDepartureTime(), departure.getDelay(), departure.getTrack()));
			}
			
			setLastUpdateTime(station);
			
			// If there were no departures, we skip updating this station for a while
			if (departures == null || departures.isEmpty()) {
				setNextUpdateTime(station, OffsetDateTime.now().plus(STATION_UPDATE_SKIP));
			}
		}
		
		// We can't get the time at the destination station from the departures alone.
		// Therefore, when a ride for which the time at the final station is not known is reaching the last known station,
		// we get a travel advice from the last known stop till the final destination, which should gives us the required details
		List<Ride> rides = trainRideDataManager.getRides();
		for (Ride ride : rides) {
			OffsetDateTime now = OffsetDateTime.now();
			RideStop lastKnownStop = ride.getLastKnownStop();
			RideStop nextStop = ride.getNextStop(now);
			
			if (lastKnownStop == null) continue; // If the ride doesn't have any stops, ignore it...
			
			OffsetDateTime actualLastKnownDeparture = lastKnownStop.getDepartureTime().plus(ObjectUtil.coalesce(lastKnownStop.getDelay(), Duration.ZERO));
			
			if (lastKnownStop.getStation().equals(nextStop) || 
					(actualLastKnownDeparture.isBefore(now.plus(FINAL_DESTINATION_QUERY_TIME)) && actualLastKnownDeparture.isAfter(now))) {
				
				logger.info("Getting final stop for ride: " + ride);
				tryGetFinalStopForRide(ride);
			}
		}
		
		List<Ride> sortedRides = rides.stream().sorted((r1, r2) -> r1.getRideCode().compareTo(r2.getRideCode())).collect(Collectors.toList());
		for (Ride ride: sortedRides) {
			logger.info("Found ride: " + ride);
		}
		
		cleanNextUpdateTimes();
	}
	
	private void cleanNextUpdateTimes() {
		
		List<Station> toRemove = new ArrayList<>();
		
		for (Map.Entry<Station, OffsetDateTime> entry : nextUpdateTimes.entrySet()) {
			if (entry.getValue().isBefore(OffsetDateTime.now())) {
				toRemove.add(entry.getKey());
			}
		}
		
		toRemove.forEach(s -> nextUpdateTimes.remove(s));
	}

	
	private void setNextUpdateTime(Station station, OffsetDateTime time) {
		if (!nextUpdateTimes.containsKey(station) || nextUpdateTimes.get(station).isBefore(time)) {
			nextUpdateTimes.put(station, time);
		}
	}
	
	private void setLastUpdateTime(Station station) {
		lastUpdateTimes.put(station, OffsetDateTime.now());
	}
	
	/**
	 * Based on the station timeouts (nextUpdateTimes)
	 * @param station
	 * @return
	 */
	private boolean isStationWorthUpdating(Station station) {
		
		OffsetDateTime now = OffsetDateTime.now();
		
		// If a station has not been updated in STATION_UPDATE_MAXIMUM_SKIP duration, then it is worth updating regardless of which trains arrive
		if (!lastUpdateTimes.containsKey(station) || lastUpdateTimes.get(station).isBefore(now.minus(STATION_UPDATE_MAXIMUM_SKIP))) { 
			return true;
		}
		
		// If updating is disabled (next update time in the future)
		if (nextUpdateTimes.containsKey(station)) {
			if (!nextUpdateTimes.get(station).isBefore(now)) {
				return false;
			}
		}
		
		OffsetDateTime nowPlusUpdateOffset = now.plus(STATION_UPDATE_OFFSET);
		boolean worthUpdating = false;

		// If there are no trains which within 5 minutes will have this station as the next station, then this station is not worth updating.
		List<Ride> rides = trainRideDataManager.getRides();
		for (Ride ride : rides) {
			
			Optional<RideStop> optionalStopAtThisStation = ride.getStops().stream().filter(s -> s.getStation().equals(station)).findFirst();
			
			if (optionalStopAtThisStation.isPresent()) {
				
				RideStop stopAtThisStation = optionalStopAtThisStation.get();
				RideStop stopBefore = ride.getStopBefore(stopAtThisStation);
				
				if (stopBefore == null) {
					// This station is the first stop in the current ride, so it's worth updating if this train is supposed to leave
					// within STATION_UPDATE_OFFSET.
					worthUpdating |= stopAtThisStation.getDepartureTime().isBefore(nowPlusUpdateOffset);
					
				} else { // Check if the train reaches the "stop before" within 5 minutes
					worthUpdating |= stopBefore.getDepartureTime().isBefore(nowPlusUpdateOffset);
				}				
			}
			
			if (worthUpdating) {
				logger.info("Station " + station.getFullName() + " is worth updating because of ride: " + ride);
				break; // If we find it is worth updating, stop searching further and update already!
			}
		}
				
		return worthUpdating;
		
	}

	/**
	 * Uses the travel advice repository to try to get (or update) information on the final stop in the given ride.
	 * @param ride
	 * @param lastKnownStop
	 */
	private void tryGetFinalStopForRide(Ride ride) {
		
		// Ride destination unknown, so no way to get the final stop. Happens when the destination is foreign or the ride is not complete yet.
		if (ride.getDestination() == null) {
			return;
		}
		
		RideStop lastKnownStop = ride.getLastKnownStop();
		RideStop fromStop = lastKnownStop.getStation().equals(ride.getDestination()) ? ride.getStopBefore(lastKnownStop) : lastKnownStop;
		if (fromStop == null) {
			logger.warn("Couldn't determine from stop for travel advice to final destination for ride: " + ride);
		} else {
			try {
				TravelAdvice advice = travelAdviceRepo.getTravelAdvice(fromStop.getStation(), ride.getDestination(), fromStop.getDepartureTime(), TimeType.DEPARTURE);
				
				logger.info("Got travel advice: " + advice);
				
				// We expect to get a travel advice without transfers (i.e. advice.getParts() == 1), and where the travel part has the same ride code.
				// Note: this assumes that we will not get a travel advice for the next day
				TravelAdviceOption currentRideTravelOption = advice.getTravelAdviceOptions().stream().filter(a -> 
						a.getParts() != null && 
						a.getParts().size() == 1 && 
						a.getParts().stream().anyMatch(p -> p.getRideNumber().equals(ride.getRideCode()) && p.getStops().get(p.getStops().size() - 1).getStation().equals(ride.getDestination()))
						)
						.findFirst().orElse(null);
				
				if (currentRideTravelOption == null) {
					logger.warn("Could not find valid travel advice option from " + fromStop.getStation().getFullName() + " to " + ride.getDestination() + " at " + fromStop.getDepartureTime());
				} else {
					// Extract the RideStop from the travel advice
					TravelAdviceOptionPart ridePart = currentRideTravelOption.getParts().get(0);
					TravelAdviceOptionPartStop lastStop = ridePart.getStops().get(ridePart.getStops().size() - 1);
					if (!lastStop.getStation().equals(ride.getDestination())) {
						logger.error("Given travel advice does not end at the final destination!");
					} else {
						for (TravelAdviceOptionPartStop travelAdviceStop : ridePart.getStops()) {
							RideStop stop = new RideStop(travelAdviceStop.getStation(), travelAdviceStop.getTime(), travelAdviceStop.getDepartureDelay(), travelAdviceStop.getTrack());
							ride.addStop(stop);
						}
					}
				}
				
			} catch (TravelAdviceException e) {
				logger.error("Couldn't get travel advice", e);						
			}
			
			
		}
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
