package com.basdado.trainfinder.ns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ActueleVertrekTijden")
@XmlAccessorType(XmlAccessType.FIELD)
public class DepartureInfoResponse {

	@XmlElement(name="VertrekkendeTrein")
	private List<Departure> departures;
	
	public List<Departure> getDepartures() {
		return departures;
	}
	
	public void setDepartures(List<Departure> departures) {
		this.departures = departures;
	}
}
