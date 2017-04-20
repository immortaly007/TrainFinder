package com.basdado.trainfinder.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.exception.PathFindingException;
import com.basdado.trainfinder.model.LatLngBounds;
import com.basdado.trainfinder.model.PositionAndBearing;
import com.basdado.trainfinder.model.Railway;
import com.basdado.trainfinder.model.Ride;
import com.basdado.trainfinder.model.RideStop;
import com.basdado.trainfinder.model.Train;
import com.basdado.trainfinder.util.DateTimeUtil;

@Stateless
public class TrainStatusFacade {
	
	private static final Logger logger = LoggerFactory.getLogger(TrainStatusFacade.class);
	
	@Inject private TrainRideDataManager trainRideDataManager;
	@Inject private TrainRoutingController trainRoutes;

	public Collection<Train> getCurrentTrains() {
		
		List<Train> res = new ArrayList<>();
		OffsetDateTime now = OffsetDateTime.now();
		List<Ride> rides = trainRideDataManager.getRides();
		for (Ride ride : rides) {
			if (ride == null || ride.getFirstKnownStop() == null) continue; // no information about this ride
			
			if (DateTimeUtil.between(ride.getFirstKnownStop().getActualDepartureTime(), ride.getLastKnownStop().getActualDepartureTime(), now)) {
				
				RideStop previousStop = ride.getActualPreviousStop(now);
				RideStop nextStop = ride.getActualNextStop(now);
				if (previousStop == null || nextStop == null) {
					logger.warn("Previous or next stop not found at " + now + "  for ride: " + ride);
					continue;
				}
				try {
					Railway trainRailwayPath = trainRoutes.getRailway(previousStop.getStation(), nextStop.getStation());
					double traveledTime = now.toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond();
					double totalTime = nextStop.getActualDepartureTime().toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond();
					double f = traveledTime <= 0.0001 ? 0 : traveledTime / totalTime;
					
					PositionAndBearing currentTrainPositionAndBearing = trainRailwayPath.calculatePositionAndBearingForProgress(f);
					
					Train train = new Train(
							currentTrainPositionAndBearing.getPosition(),
							currentTrainPositionAndBearing.getBearing(),
							previousStop.getStation(),
							previousStop.getDepartureTime(),
							previousStop.getActualDepartureTime(),
							nextStop.getStation(),
							nextStop.getDepartureTime(),
							nextStop.getActualDepartureTime(),
							ride);
	
					res.add(train);
				} catch (PathFindingException e) {
					logger.warn("Could not find railways from " + previousStop.getStation() + " to " + nextStop.getStation() + ": " + e.getMessage());
				}
			}
		}
		
		return res;
	}
	
	public Collection<Train> getCurrentTrainsInBounds(LatLngBounds bounds) {
		
		return getCurrentTrains().stream()
				.filter(t -> bounds.contains(t.getPosition()))
				.collect(Collectors.toList());
		
	}
}
