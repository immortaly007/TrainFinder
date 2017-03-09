package com.basdado.trainfinder.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.model.LatLonCoordinate;
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
				Railway trainRailwayPath = trainRoutes.getRailway(previousStop.getStation(), nextStop.getStation());
				double f = ((double)(now.toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond())) / 
						((double)(nextStop.getActualDepartureTime().toEpochSecond() - previousStop.getActualDepartureTime().toEpochSecond()));
				
				LatLonCoordinate currentTrainPosition = trainRailwayPath.calculatePositionForProgress(f);
				
				Train train = new Train(
						currentTrainPosition,
						previousStop.getStation(),
						previousStop.getDepartureTime(),
						previousStop.getActualDepartureTime(),
						nextStop.getStation(),
						nextStop.getDepartureTime(),
						nextStop.getActualDepartureTime(),
						ride.getRideCode(),
						ride.getCarrier(),
						ride.getTrainType());

				res.add(train);
			}
		}
		
		return res;
		
		
	}
}
