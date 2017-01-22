package com.basdado.trainfinder.data;

import java.util.Collection;

import com.basdado.trainfinder.model.Station;

public interface StationRepository {
	
	public Collection<Station> getStations();
	
	/**
	 * Finds the station with the given code. Returns null if no such station is found.
	 * @param code The station code
	 * @return The Station with the given code (or null)
	 */
	public Station getStationWithCode(String code);
	
	/**
	 * Finds a station with the given name. Can be short or full name. Return null if no station could be found
	 * @param name The station name
	 * @return The station with that name.
	 */
	public Station getStationWithName(String name);

}
