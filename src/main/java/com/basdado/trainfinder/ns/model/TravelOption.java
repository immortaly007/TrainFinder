package com.basdado.trainfinder.ns.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.basdado.trainfinder.ns.xml.NSOffsetDateTimeXmlAdapter;
import com.basdado.trainfinder.ns.xml.TravelAdviceDurationXmlAdapter;

@XmlRootElement(name="ReisMogelijkheid")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelOption {

	@XmlElement(name="Melding")
	private List<TravelOptionNotice> notices;
	
	@XmlElement(name="AantalOverstappen")
	private int transferCount;
	
	@XmlElement(name="GeplandeReisTijd")
	@XmlJavaTypeAdapter(TravelAdviceDurationXmlAdapter.class)
	private Duration plannedTravelTime;
	
	@XmlElement(name="ActueleReisTijd")
	@XmlJavaTypeAdapter(TravelAdviceDurationXmlAdapter.class)
	private Duration actualDuration;
	
	@XmlElement(name="Optimaal")
	private boolean optimal;
	
	@XmlElement(name="GeplandeVertrekTijd")
	@XmlJavaTypeAdapter(NSOffsetDateTimeXmlAdapter.class)
	private OffsetDateTime plannedDepartureTime;
	
	@XmlElement(name="ActueleVertrekTijd")
	@XmlJavaTypeAdapter(NSOffsetDateTimeXmlAdapter.class)
	private OffsetDateTime actualDepartureTime;
	
	@XmlElement(name="GeplandeAankomstTijd")
	@XmlJavaTypeAdapter(NSOffsetDateTimeXmlAdapter.class)
	private OffsetDateTime plannedArrivalTime;
	
	@XmlElement(name="ActueleAankomstTijd")
	@XmlJavaTypeAdapter(NSOffsetDateTimeXmlAdapter.class)
	private OffsetDateTime actualArrivalTime;
	
	@XmlElement(name="Status")
	private String status;
	
	@XmlElement(name="ReisDeel")
	private List<TravelOptionPart> parts;

	public List<TravelOptionNotice> getNotices() {
		return notices;
	}

	public void setNotices(List<TravelOptionNotice> notices) {
		this.notices = notices;
	}

	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
	}

	public Duration getPlannedTravelTime() {
		return plannedTravelTime;
	}

	public void setPlannedTravelTime(Duration plannedTravelTime) {
		this.plannedTravelTime = plannedTravelTime;
	}

	public Duration getActualDuration() {
		return actualDuration;
	}

	public void setActualDuration(Duration actualDuration) {
		this.actualDuration = actualDuration;
	}

	public boolean isOptimal() {
		return optimal;
	}

	public void setOptimal(boolean optimal) {
		this.optimal = optimal;
	}

	public OffsetDateTime getPlannedDepartureTime() {
		return plannedDepartureTime;
	}

	public void setPlannedDepartureTime(OffsetDateTime plannedDepartureTime) {
		this.plannedDepartureTime = plannedDepartureTime;
	}

	public OffsetDateTime getActualDepartureTime() {
		return actualDepartureTime;
	}

	public void setActualDepartureTime(OffsetDateTime actualDepartureTime) {
		this.actualDepartureTime = actualDepartureTime;
	}

	public OffsetDateTime getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public void setPlannedArrivalTime(OffsetDateTime plannedArrivalTime) {
		this.plannedArrivalTime = plannedArrivalTime;
	}

	public OffsetDateTime getActualArrivalTime() {
		return actualArrivalTime;
	}

	public void setActualArrivalTime(OffsetDateTime actualArrivalTime) {
		this.actualArrivalTime = actualArrivalTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<TravelOptionPart> getParts() {
		return parts;
	}

	public void setParts(List<TravelOptionPart> parts) {
		this.parts = parts;
	}
	
}
