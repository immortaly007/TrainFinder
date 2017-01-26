package com.basdado.trainfinder.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TravelAdviceOptionPart {
	
	private final String carrier;
	private final String transportType;
	private final String rideNumber;
	private final String status;
	private final List<String> travelDetails;
	private final String plannedInterruptionId;
	private final String unplannedInterruptionId;
	private final List<TravelAdviceOptionPartStop> stops;
	
	public TravelAdviceOptionPart(String carrier, String transportType, String rideNumber, String status,
			List<String> travelDetails, String plannedInterruptionId, String unplannedInterruptionId,
			List<TravelAdviceOptionPartStop> stops) {

		this.carrier = carrier;
		this.transportType = transportType;
		this.rideNumber = rideNumber;
		this.status = status;
		this.travelDetails = Collections.unmodifiableList(travelDetails);
		this.plannedInterruptionId = plannedInterruptionId;
		this.unplannedInterruptionId = unplannedInterruptionId;
		this.stops = Collections.unmodifiableList(stops);
	}

	public String getCarrier() {
		return carrier;
	}
	
	public String getTransportType() {
		return transportType;
	}

	public String getRideNumber() {
		return rideNumber;
	}

	public String getStatus() {
		return status;
	}

	public List<String> getTravelDetails() {
		return travelDetails;
	}

	public String getPlannedInterruptionId() {
		return plannedInterruptionId;
	}

	public String getUnplannedInterruptionId() {
		return unplannedInterruptionId;
	}
	
	public List<TravelAdviceOptionPartStop> getStops() {
		return stops;
	}
	
	@Override
	public String toString() {
		return transportType + "(" + rideNumber + ") by " + carrier + 
				", stops: [" + String.join(", ", stops.stream().map(s -> s.toString()).collect(Collectors.toList())) + "]" + 
				((travelDetails != null && !travelDetails.isEmpty()) ? ", details: " + String.join(", ", travelDetails) : "");
	}
}
