package com.basdado.trainfinder.ns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ReisMogelijkheden")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelAdviceResponse {

	@XmlElement(name="ReisMogelijkheid")
	private List<TravelOption> travelOptions;
	
	public List<TravelOption> getTravelOptions() {
		return travelOptions;
	}
	
	public void setTravelOptions(List<TravelOption> travelOptions) {
		this.travelOptions = travelOptions;
	}
	
}
