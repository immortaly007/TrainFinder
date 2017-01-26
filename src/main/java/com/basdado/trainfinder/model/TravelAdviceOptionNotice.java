package com.basdado.trainfinder.model;

public class TravelAdviceOptionNotice {
	
	private final String id;
	private final boolean serious;
	private final String text;
	
	public TravelAdviceOptionNotice(String id, boolean serious, String text) {

		this.id = id;
		this.serious = serious;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public boolean isSerious() {
		return serious;
	}
	
	public String getText() {
		return text;
	}
}
