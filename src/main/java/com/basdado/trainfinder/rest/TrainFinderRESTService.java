package com.basdado.trainfinder.rest;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.basdado.trainfinder.data.StationRepository;
import com.basdado.trainfinder.model.Station;

/**
 * This class produces a RESTful service to access the train finder
 */
@Path("train")
@RequestScoped
public class TrainFinderRESTService {

	@Inject
    private Logger logger;
	
	@Inject private StationRepository stationRepository;

    @GET
    @Path("/{lat:[0-9]*.?[0-9]*}-{lon:[0-9]*.?[0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public String lookupMemberById(@PathParam("lat") float lat, @PathParam("lon") float lon) {
        
    	logger.log(Level.INFO, "Latitude: " + lat + ", Longitude: " + lon);
    	return "Latitude: " + lat + ", Longitude: " + lon;
    	
    }
    
    @GET
    @Path("getStations")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Station> getStations() {
    	return stationRepository.getStations();
    }
}
