package com.basdado.trainfinder.ns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ReisDeel")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelOptionPart {
	
	@XmlElement(name="Vervoerder")
	private String carrier;
	
	@XmlElement(name="VervoerType")
	private String transportType;
	
	@XmlElement(name="RitNummer")
	private String rideNumber;
	
	@XmlElement(name="Status")
	private String status;
	
	@XmlElementWrapper(name="ReisDetails")
	@XmlElement(name="ReisDetail")
	private List<String> travelDetails;
	
	@XmlElement(name="GeplandeStoringId")
	private String plannedInterruptionId;
	
	@XmlElement(name="OngeplandeStoringId")
	private String unplannedInterruptionId;
	
	@XmlElement(name="ReisStop")
	private List<TravelOptionPartStation> stops;

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTransportType() {
		return transportType;
	}

	public void setTransportType(String transportType) {
		this.transportType = transportType;
	}

	public String getRideNumber() {
		return rideNumber;
	}

	public void setRideNumber(String rideNumber) {
		this.rideNumber = rideNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getTravelDetails() {
		return travelDetails;
	}

	public void setTravelDetails(List<String> travelDetails) {
		this.travelDetails = travelDetails;
	}

	public String getPlannedInterruptionId() {
		return plannedInterruptionId;
	}

	public void setPlannedInterruptionId(String plannedInterruptionId) {
		this.plannedInterruptionId = plannedInterruptionId;
	}

	public String getUnplannedInterruptionId() {
		return unplannedInterruptionId;
	}

	public void setUnplannedInterruptionId(String unplannedInterruptionId) {
		this.unplannedInterruptionId = unplannedInterruptionId;
	}

	public List<TravelOptionPartStation> getStops() {
		return stops;
	}

	public void setStops(List<TravelOptionPartStation> stops) {
		this.stops = stops;
	}
}
