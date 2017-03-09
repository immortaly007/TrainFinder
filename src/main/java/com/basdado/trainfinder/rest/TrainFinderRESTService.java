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

import org.apache.commons.lang3.NotImplementedException;

import com.basdado.trainfinder.controller.TrainStatusFacade;
import com.basdado.trainfinder.data.StationRepository;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.model.Train;

/**
 * This class produces a RESTful service to access the train finder
 */
@Path("train")
@RequestScoped
public class TrainFinderRESTService {

	@Inject
    private Logger logger;
	
	@Inject private StationRepository stationRepository;
	@Inject private TrainStatusFacade trainStatusFacade;

    @GET
    @Path("/near-{lat:[0-9]*.?[0-9]*}-{lon:[0-9]*.?[0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Train getNearestTrain(@PathParam("lat") double lat, @PathParam("lon") double lon) {
        
    	logger.log(Level.INFO, "Latitude: " + lat + ", Longitude: " + lon);
    	throw new NotImplementedException("getNearestTrain() is not yet implemented");
    	
    }
    
    @GET
    @Path("stations")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Station> getStations() {
    	return stationRepository.getStations();
    }
    
    @GET
    @Path("trains")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Train> getTrains() {
    	return trainStatusFacade.getCurrentTrains();
    }
}
