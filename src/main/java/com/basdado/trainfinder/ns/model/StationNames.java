package com.basdado.trainfinder.ns.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Namen")
@XmlAccessorType(XmlAccessType.FIELD)
public class StationNames {
	
	@XmlElement(name="Kort")
	private String shortName;
	
	@XmlElement(name="Middel")
	private String mediumName;
	
	@XmlElement(name="Lang")
	private String longName;

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getMediumName() {
		return mediumName;
	}

	public void setMediumName(String mediumName) {
		this.mediumName = mediumName;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}
	
	
}
