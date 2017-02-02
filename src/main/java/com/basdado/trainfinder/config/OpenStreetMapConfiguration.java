package com.basdado.trainfinder.config;

import org.apache.commons.configuration2.Configuration;

public class OpenStreetMapConfiguration {
	
	private final String railroadFile;
	private final double maxStationToTrackDistance;
	private final double preferredStationToTrackDistance;
	
	public OpenStreetMapConfiguration(Configuration config) {
		railroadFile = config.getString("OpenStreetMap.RailroadFile");
		preferredStationToTrackDistance = config.getDouble("OpenStreetMap.PreferredStationToTrackDistance");
		maxStationToTrackDistance = config.getDouble("OpenStreetMap.MaxStationToTrackDistance");
	}
	
	public String getRailroadFile() {
		return railroadFile;
	}
	
	public double getPreferredStationToTrackDistance() {
		return preferredStationToTrackDistance;
	}
	
	public double getMaxStationToTrackDistance() {
		return maxStationToTrackDistance;
	}

}
