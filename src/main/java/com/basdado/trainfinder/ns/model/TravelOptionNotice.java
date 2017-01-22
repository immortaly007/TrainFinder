package com.basdado.trainfinder.ns.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Melding")
@XmlAccessorType(XmlAccessType.FIELD)
public class TravelOptionNotice {
	
	@XmlElement(name="Id")
	private String id;
	
	@XmlElement(name="Ernstig")
	private boolean serious;
	
	@XmlElement(name="Text")
	private String text;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSerious() {
		return serious;
	}

	public void setSerious(boolean serious) {
		this.serious = serious;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
