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
import com.basdado.trainfinder.model.LatLng;
import com.basdado.trainfinder.model.LatLngBounds;
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

	/**
	 * 
	 * @param lat The current latitude of the user
	 * @param lng The current longitude of the user
	 * @param heading Current heading or bearing of the user, useful to determine if the user is moving in the same
	 * direction as some train.
	 * @return The most likely train the user is on.
	 */
    @GET
    @Path("/near/{lat:[0-9]*.?[0-9]*},{lng:[0-9]*.?[0-9]*},{heading:[0.9]*.?[0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Train getNearestTrain(@PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("heading") double heading) {
        
    	LatLng pos = new LatLng(lat, lng);
    	
    	
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
    
    @GET
    @Path("trains/{minLat:[0-9]*.?[0-9]*},{minLng:[0-9]*.?[0-9]*},{maxLat:[0-9]*.?[0-9]*},{maxLng:[0-9]*.?[0-9]*}")
    public Collection<Train> getTrains(
    		@PathParam("minLat") double minLat, @PathParam("minLng") double minLng, 
    		@PathParam("maxLat") double maxLat, @PathParam("maxLng") double maxLng) {
    	
    	LatLngBounds bounds = new LatLngBounds(minLat, minLng, maxLat, maxLng);
    	return trainStatusFacade.getCurrentTrainsInBounds(bounds);
    	
    }
}
