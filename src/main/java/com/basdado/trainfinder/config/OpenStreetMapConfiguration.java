package com.basdado.trainfinder.config;

import org.apache.commons.configuration2.Configuration;

public class OpenStreetMapConfiguration {
	
	private final String railroadFile;
	
	public OpenStreetMapConfiguration(Configuration config) {
		railroadFile = config.getString("OpenStreetMap.RailroadFile");
	}
	
	public String getRailroadFile() {
		return railroadFile;
	}

}
