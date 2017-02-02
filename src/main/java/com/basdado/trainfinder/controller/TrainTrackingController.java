package com.basdado.trainfinder.controller;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.basdado.trainfinder.model.Ride;
import com.basdado.trainfinder.model.RideKey;
import com.basdado.trainfinder.model.RideStop;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.model.Train;
import com.basdado.trainfinder.model.TravelAdvice;
import com.basdado.trainfinder.model.TravelAdviceOption;
import com.basdado.trainfinder.model.TravelAdviceOptionPart;
import com.basdado.trainfinder.model.TravelAdviceOptionPartStop;
import com.basdado.trainfinder.util.ObjectUtil;
import com.basdado.trainfinder.util.PathHelper;

@Singleton
@Startup
public class TrainTrackingController {
	
	private static final Logger logger = LoggerFactory.getLogger(TrainTrackingController.class);
	
	private static final LatLonCoordinate STATION_FILTER_MAX = new LatLonCoordinate(51.777160, 6.4139832);
	private static final LatLonCoordinate STATION_FILTER_MIN = new LatLonCoordinate(50.649176, 4.58460);
	
	private static final Duration MAXIMUM_RIDE_LENGTH = Duration.ofHours(8);
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
	
	private Map<RideKey, Ride> rides;
	private Map<Station, OffsetDateTime> nextUpdateTimes;
	private Map<Station, OffsetDateTime> lastUpdateTimes;
	
	public TrainTrackingController() {
		rides = new HashMap<>();
		nextUpdateTimes = new HashMap<>();
		lastUpdateTimes = new HashMap<>();
	}
	

	public Collection<Train> getCurrentTrains() {
		
		List<Train> res = new ArrayList<>();
		OffsetDateTime now = OffsetDateTime.now();
		for (Ride ride : rides.values()) {
			if (ride == null || ride.getFirstKnownStop() == null) continue; // no information about this ride
			
			if (between(ride.getFirstKnownStop().getActualDepartureTime(), ride.getLastKnownStop().getActualDepartureTime(), now)) {
				
				RideStop previousStop = ride.getPreviousStop(now);
				RideStop nextStop = ride.getNextStop(now);
				if (previousStop == null || nextStop == null) {
					logger.warn("Previous or next stop not found at " + now + "  for ride: " + ride);
					continue;
				}
				List<LatLonCoordinate> trainRailwayPath = trainRoutes.getRailway(previousStop.getStation(), nextStop.getStation());
				double f = ((double)(nextStop.getActualDepartureTime().toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond())) /
						((double)(now.toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond()));
				
				LatLonCoordinate currentTrainPosition = PathHelper.getPointAt(trainRailwayPath, f);
				
				Train train = new Train();
				train.setDepartureStation(previousStop.getStation());
				train.setActualDepartureTime(previousStop.getActualDepartureTime());
				train.setPlannedDepartureTime(previousStop.getDepartureTime());
				train.setArrivalStation(nextStop.getStation());
				train.setActualArrivalTime(nextStop.getActualDepartureTime());
				train.setPlannedArrivalTime(nextStop.getDepartureTime());
				train.setPosition(currentTrainPosition);
				train.setRideCode(ride.getRideCode());
				
				res.add(train);
			}
		}
		
		return res;
		
		
	}
	
	
	@Schedule(hour="*",minute="*/5")
	public void RefreshDepartures() {
		
		Collection<Station> stations = getStations();
		Collection<Station> stationsWorthUpdating = stations.stream().filter(s -> isStationWorthUpdating(s)).collect(Collectors.toList());
		
		List<LatLonCoordinate> railwayPath = trainRoutes.getRailway(stationRepo.getStationWithName("Weert"), stationRepo.getStationWithName("Eindhoven"));
		logger.info("Found path from Heerlen to Vlissingen: " + String.join(",", railwayPath.stream().map(r -> "[" + r.getLongitude() + "," + r.getLatitude() + "]").collect(Collectors.toList())));
		
		for(Station station: stationsWorthUpdating) {

			logger.info("Getting departures at " + station.getFullName());
			
			final Collection<Departure> departures = departuresRepo.getDeparturesAt(station);
			
			for (Departure departure : departures) {
				Ride ride = getRide(departure);
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
		for (Ride ride : rides.values()) {
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
		
		List<Ride> sortedRides = rides.values().stream().sorted((r1, r2) -> r1.getRideCode().compareTo(r2.getRideCode())).collect(Collectors.toList());
		for (Ride ride: sortedRides) {
			logger.info("Found ride: " + ride);
		}
		
		cleanNextUpdateTimes();
		cleanRides();
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
	
	private void cleanRides() {
		
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
		for (Ride ride : rides.values()) {
			
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
			ride = new Ride(departure.getDepartureTime().toLocalDate(), departure.getRideNumber(), departure.getFinalDestination());
			rides.put(new RideKey(ride), ride);
		}
		
		// Sometimes ride destination are unclear at some when queried from some stations (e.g. when the train splits into another ride)
		// but at other stations are clear. In this case we need to replace the ride with the missing destination by one with a correct destination:
		if (ride != null && ride.getDestination() == null && departure.getFinalDestination() != null) {
			
			Ride newRide = new Ride(ride.getStartDate(), ride.getRideCode(), departure.getFinalDestination());
			for (RideStop s : ride.getStops()) {
				newRide.addStop(s);
			}
			rides.put(new RideKey(newRide), newRide); // override existing ride
			ride = newRide;			
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
	
	public static boolean between(OffsetDateTime min, OffsetDateTime max, OffsetDateTime x) {
		return (x.isAfter(min) || x.isEqual(min)) && x.isBefore(max);
	}
	
}
