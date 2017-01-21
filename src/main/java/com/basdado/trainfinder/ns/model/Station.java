package com.basdado.trainfinder.ns.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Station")
@XmlAccessorType(XmlAccessType.FIELD)
public class Station {
	
	@XmlElement(name="Code")
	private String code;
	
	@XmlElement(name="Code")
	private String type;
	
	@XmlElement(name="Namen")
	private StationNames stationNames;
	
	@XmlElement(name="Land")
	private String country;
	
	@XmlElement(name="UICCode")
	private String UICCode;
	
	@XmlElement(name="Lat")
	private double lat;
	
	@XmlElement(name="Lon")
	private double lon;
	
	@XmlElementWrapper(name="Synoniemen")
	@XmlElement(name="Synoniem")
	private List<String> synonyms;

	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public StationNames getStationNames() {
		return stationNames;
	}

	public void setStationNames(StationNames stationNames) {
		this.stationNames = stationNames;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUICCode() {
		return UICCode;
	}

	public void setUICCode(String uICCode) {
		UICCode = uICCode;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}	
}
