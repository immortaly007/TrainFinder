package com.basdado.trainfinder.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TravelAdviceOption {

	private final List<TravelAdviceOptionNotice> notices;
	private final int transferCount;
	private final Duration plannedTravelTime;
	private final Duration actualDuration;
	private final boolean optimal;
	private final OffsetDateTime plannedDepartureTime;
	private final OffsetDateTime actualDepartureTime;
	private final OffsetDateTime plannedArrivalTime;
	private final OffsetDateTime actualArrivalTime;
	private final String status;
	private final List<TravelAdviceOptionPart> parts;
	
	public TravelAdviceOption(List<TravelAdviceOptionNotice> notices, int transferCount, Duration plannedTravelTime,
			Duration actualDuration, boolean optimal, OffsetDateTime plannedDepartureTime,
			OffsetDateTime actualDepartureTime, OffsetDateTime plannedArrivalTime, OffsetDateTime actualArrivalTime,
			String status, List<TravelAdviceOptionPart> parts) {

		this.notices = Collections.unmodifiableList(notices);
		this.transferCount = transferCount;
		this.plannedTravelTime = plannedTravelTime;
		this.actualDuration = actualDuration;
		this.optimal = optimal;
		this.plannedDepartureTime = plannedDepartureTime;
		this.actualDepartureTime = actualDepartureTime;
		this.plannedArrivalTime = plannedArrivalTime;
		this.actualArrivalTime = actualArrivalTime;
		this.status = status;
		this.parts = Collections.unmodifiableList(parts);
	}

	public List<TravelAdviceOptionNotice> getNotices() {
		return notices;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public Duration getPlannedTravelTime() {
		return plannedTravelTime;
	}

	public Duration getActualDuration() {
		return actualDuration;
	}

	public boolean isOptimal() {
		return optimal;
	}

	public OffsetDateTime getPlannedDepartureTime() {
		return plannedDepartureTime;
	}

	public OffsetDateTime getActualDepartureTime() {
		return actualDepartureTime;
	}

	public OffsetDateTime getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public OffsetDateTime getActualArrivalTime() {
		return actualArrivalTime;
	}

	public String getStatus() {
		return status;
	}

	public List<TravelAdviceOptionPart> getParts() {
		return parts;
	}
	
	public Duration getDepartureDelay() {
		return Duration.between(plannedDepartureTime, actualDepartureTime);
	}
	
	public Duration getArrivalDelay() {
		return Duration.between(plannedArrivalTime, actualArrivalTime);
	}
	
	
	@Override
	public String toString() {
		Duration departureDelay = getDepartureDelay();
		Duration arrivalDelay = getArrivalDelay();
		
		return plannedDepartureTime + (departureDelay.isZero() ? "" : " +" + departureDelay.toMinutes()) + " min" + " - " + 
				plannedArrivalTime + (arrivalDelay.isZero() ? "" : " +" + arrivalDelay.toMinutes()) + " min" +
				", optimal: " + optimal + 
				", notices: " + String.join(", ", notices.stream().map(n -> "'" + n.getText() + "'").collect(Collectors.toList())) +
				", status: '" + status + "'" +
				", parts: " + String.join(", ", parts.stream().map(n -> n.toString()).collect(Collectors.toList()));
	}

}
