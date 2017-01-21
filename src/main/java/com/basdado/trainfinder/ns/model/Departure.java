package com.basdado.trainfinder.ns.model;

import java.time.Duration;
import java.time.OffsetDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.basdado.trainfinder.ns.xml.NSOffsetDateTimeXmlAdapter;
import com.migesok.jaxb.adapter.javatime.DurationXmlAdapter;

@XmlRootElement(name="VertrekkendeTrein")
@XmlAccessorType(XmlAccessType.FIELD)
public class Departure {
	
	@XmlElement(name="RitNummer")
	private String rideNumber;
	
	@XmlElement(name="VertrekTijd")
	@XmlJavaTypeAdapter(NSOffsetDateTimeXmlAdapter.class)
	private OffsetDateTime departureTime;
	
	@XmlElement(name="VertrekVertraging")
	@XmlJavaTypeAdapter(DurationXmlAdapter.class)
	private Duration delay;
	
	@XmlElement(name="VertrekVertragingTekst")
	private String delayText;
	
	@XmlElement(name="EindBestemming")
	private String destination;
	
	@XmlElement(name="TreinSoort")
	private String trainType;
	
	@XmlElement(name="RouteTekst")
	private String routeText;
	
	@XmlElement(name="RouteTekst")
	private String carrier;
	
	@XmlElement(name="VertrekSpoor")
	private String track;
	
	@XmlElement(name="ReisTip")
	private String tip;
	
	@XmlElement(name="Remarks")
	private String remarks;

	public String getRideNumber() {
		return rideNumber;
	}

	public void setRideNumber(String rideNumber) {
		this.rideNumber = rideNumber;
	}

	public OffsetDateTime getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(OffsetDateTime departureTime) {
		this.departureTime = departureTime;
	}

	public Duration getDelay() {
		return delay;
	}

	public void setDelay(Duration delay) {
		this.delay = delay;
	}

	public String getDelayText() {
		return delayText;
	}

	public void setDelayText(String delayText) {
		this.delayText = delayText;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTrainType() {
		return trainType;
	}

	public void setTrainType(String trainType) {
		this.trainType = trainType;
	}

	public String getRouteText() {
		return routeText;
	}

	public void setRouteText(String routeText) {
		this.routeText = routeText;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
}
