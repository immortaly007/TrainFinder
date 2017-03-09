package com.basdado.trainfinder.data;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.model.Departure;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.DepartureInfoResponse;

@Stateless
@Local(DeparturesRepository.class)
public class NSDeparturesRepository implements DeparturesRepository {

	private static final Logger logger = LoggerFactory.getLogger(NSDeparturesRepository.class);
	
	@Inject NSCommunicator communicator;
	@Inject StationRepository nsStationRepo;
	
	@Override
	public Collection<Departure> getDeparturesAt(Station station) {
		
		DepartureInfoResponse departureInfoResponse;
		try {
			departureInfoResponse = communicator.getDepartures(station.getCode());
		} catch (NSException e) {
			logger.error("NSException while trying to load departure times: " + e.getMessage(), e);
			return Collections.emptyList();
		}
		
		
		return departureInfoResponse.getDepartures().stream()
				.map(d ->
					new Departure(station, nsStationRepo.getStationWithName(d.getDestination()), d.getRideNumber(), d.getDepartureTime(), d.getDelay(), d.getTrack(), d.getCarrier(), d.getTrainType())
				).collect(Collectors.toList());
		
	}

}
