package com.basdado.trainfinder.data;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.basdado.trainfinder.exception.TravelAdviceException;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.model.TravelAdvice;
import com.basdado.trainfinder.model.TravelAdviceOption;
import com.basdado.trainfinder.model.TravelAdviceOptionNotice;
import com.basdado.trainfinder.model.TravelAdviceOptionPart;
import com.basdado.trainfinder.model.TravelAdviceOptionPartStop;
import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.TravelAdviceResponse;
import com.basdado.trainfinder.ns.model.TravelOption;
import com.basdado.trainfinder.ns.model.TravelOptionPart;
import com.basdado.trainfinder.ns.model.TravelOptionPartStation;
import com.basdado.trainfinder.util.ObjectUtil;

@Stateless
@Local(TravelAdviceRepository.class)
public class NSTravelAdviceRepository implements TravelAdviceRepository {

	@Inject private NSCommunicator nsCommunicator;
	@Inject private StationRepository stationRepo;
	
	@Override
	public TravelAdvice getTravelAdvice(Station fromStation, Station toStation, OffsetDateTime dateTime, TimeType timeType) throws TravelAdviceException {
		
		TravelAdviceResponse response;
		try {
			response = nsCommunicator.getTravelAdvice(fromStation.getCode(), toStation.getCode(), dateTime, timeType == TimeType.DEPARTURE);
		} catch (NSException e) {
			throw new TravelAdviceException(e);
		}
		
		return toTravelAdvice(response);
		
	}

	@Override
	public TravelAdvice getTravelAdvice(Station fromStation, Station toStation, Station intermediateStation, OffsetDateTime dateTime, TimeType timeType) throws TravelAdviceException {
		
		TravelAdviceResponse response;
		try {
			response = nsCommunicator.getTravelAdvice(
					fromStation.getCode(), 
					toStation.getCode(),
					intermediateStation.getCode(),
					5, 5,
					dateTime, 
					timeType == TimeType.DEPARTURE, 
					true, 
					false);
		} catch (NSException e) {
			throw new TravelAdviceException(e);
		}
		
		return toTravelAdvice(response);
	}

	private TravelAdvice toTravelAdvice(TravelAdviceResponse response) {
		
		List<TravelAdviceOption> travelAdviceOptions = new ArrayList<>();
		List<TravelOption> responseTravelOptions = response.getTravelOptions();
		if (responseTravelOptions != null && !responseTravelOptions.isEmpty()) {
			for (TravelOption responseTravelOption : responseTravelOptions) {
				
				List<TravelAdviceOptionNotice> notices;
				if (responseTravelOption.getNotices() == null) {
					notices = new ArrayList<>();
				} else {
					notices = responseTravelOption.getNotices().stream().map(n -> 
							new TravelAdviceOptionNotice(n.getId(), n.isSerious(), n.getText()))
							.collect(Collectors.toList());
				}
				
				List<TravelAdviceOptionPart> parts = new ArrayList<>();
				if (responseTravelOption.getParts() != null) {
					for (TravelOptionPart responsePart : responseTravelOption.getParts()) {
						List<TravelAdviceOptionPartStop> stops = new ArrayList<>();
						if (responsePart.getStops() != null) {
							for (TravelOptionPartStation responseStop : responsePart.getStops()) {
								Station stopStation = stationRepo.getStationWithName(responseStop.getName());
								TravelAdviceOptionPartStop stop = new TravelAdviceOptionPartStop(
										stopStation, 
										responseStop.getTime(), Duration.ofMinutes(ObjectUtil.coalesce(responseStop.getDepartureDelayMinutes(), 0)), responseStop.getTrack());
								stops.add(stop);
							}
						}
						
						parts.add(new TravelAdviceOptionPart(
								responsePart.getCarrier(), 
								responsePart.getTransportType(), 
								responsePart.getRideNumber(), 
								responsePart.getStatus(), 
								ObjectUtil.coalesce(responsePart.getTravelDetails(), Collections.emptyList()),
								responsePart.getPlannedInterruptionId(),
								responsePart.getUnplannedInterruptionId(),
								stops));
					}
				}
				
				TravelAdviceOption travelAdviceOption = new TravelAdviceOption(
						notices,
						responseTravelOption.getTransferCount(), 
						responseTravelOption.getPlannedTravelTime(), 
						responseTravelOption.getActualDuration(), 
						responseTravelOption.isOptimal(), 
						responseTravelOption.getPlannedDepartureTime(),
						responseTravelOption.getActualDepartureTime(), 
						responseTravelOption.getPlannedArrivalTime(), 
						responseTravelOption.getActualArrivalTime(), 
						responseTravelOption.getStatus(), 
						parts);
				travelAdviceOptions.add(travelAdviceOption);
			}
		}

		return new TravelAdvice(travelAdviceOptions);
		
	}
	
}
