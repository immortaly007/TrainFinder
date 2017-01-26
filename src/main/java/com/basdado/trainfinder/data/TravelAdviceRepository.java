package com.basdado.trainfinder.data;

import java.time.OffsetDateTime;

import com.basdado.trainfinder.exception.TravelAdviceException;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.model.TravelAdvice;

public interface TravelAdviceRepository {
	
	TravelAdvice getTravelAdvice(Station fromStation, Station toStation, OffsetDateTime dateTime, TimeType timeType) throws TravelAdviceException;
	TravelAdvice getTravelAdvice(Station fromStation, Station toStation, Station intermediateStation, OffsetDateTime dateTime, TimeType timeType) throws TravelAdviceException;
	
	public static enum TimeType {
		DEPARTURE, ARRIVAL
	}
}
