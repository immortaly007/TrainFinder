package com.basdado.trainfinder.controller;

import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import com.basdado.trainfinder.config.ConfigService;
import com.basdado.trainfinder.config.OpenStreetMapConfiguration;

import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;

@Singleton
public class TrainRoutingController {
	
	@Inject ConfigService configService;
	
	@PostConstruct
	public void init() {
		
		OpenStreetMapConfiguration osmConfig = configService.getOpenStreetMapConfiguration();
		
		OsmXmlReader xmlReader;
		try {
			xmlReader = new OsmXmlReader(osmConfig.getRailroadFile(), true);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found: " + osmConfig.getRailroadFile(), e);
		}
	
		// TODO read the XML file to some domain objects
		
	}
	
}
