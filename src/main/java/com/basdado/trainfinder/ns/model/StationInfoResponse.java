package com.basdado.trainfinder.ns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Stations")
@XmlAccessorType(XmlAccessType.FIELD)
public class StationInfoResponse {

	@XmlElement(name="Station")
	private List<Station> stations;

	public List<Station> getStations() {
		return stations;
	}

	public void setStations(List<Station> stations) {
		this.stations = stations;
	}
	
	
}
