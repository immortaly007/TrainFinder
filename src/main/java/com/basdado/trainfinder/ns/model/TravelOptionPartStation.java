package com.basdado.trainfinder.ns.model;

import java.time.OffsetDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.basdado.trainfinder.ns.xml.TravelAdviceDelayMinutesXmlAdapter;

@XmlRootElement(name="ReisStop")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelOptionPartStation {
	
	@XmlElement(name="Naam")
	private String name;
	
	@XmlElement(name="Tijd")
	private OffsetDateTime time;
	
	@XmlElement(name="VertrekVertraging")
	@XmlJavaTypeAdapter(TravelAdviceDelayMinutesXmlAdapter.class)
	private Integer departureDelayMinutes;
	
	@XmlElement(name="Spoor")
	private String track;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OffsetDateTime getTime() {
		return time;
	}

	public void setTime(OffsetDateTime time) {
		this.time = time;
	}

	public Integer getDepartureDelayMinutes() {
		return departureDelayMinutes;
	}

	public void setDepartureDelayMinutes(Integer departureDelayMinutes) {
		this.departureDelayMinutes = departureDelayMinutes;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}
}
