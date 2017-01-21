package com.basdado.trainfinder.data;

import java.util.Collection;

import com.basdado.trainfinder.model.Station;

public interface StationRepository {
	
	public Collection<Station> getStations();

}
