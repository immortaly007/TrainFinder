package com.basdado.trainfinder.data;

import java.util.Collection;

import com.basdado.trainfinder.model.Departure;
import com.basdado.trainfinder.model.Station;

public interface DeparturesRepository {
	
	/**
	 * Returns actual upcoming the departures at the given station.
	 * @param station
	 * @return
	 */
	public Collection<Departure> getDeparturesAt(Station station);
}
