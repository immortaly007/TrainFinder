package com.basdado.trainfinder.data;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.StationInfoResponse;

@Stateless
@Local(StationRepository.class)
public class NSStationRepository implements StationRepository {

	@Inject private NSCommunicator nsCommunicator;
	
	@Inject private CacheManager cacheManager;
	
	private static final String STATION_COLLECTION_CACHE_NAME = "stationCollectionCache";
	private static final String NS_STATION_COLLECTION_CACHE_KEY = "nsStations";
	
	@SuppressWarnings("rawtypes")
	private Cache<String, Collection> stationCollectionCache;
	
	@Override
	public Collection<Station> getStations() {
		
		@SuppressWarnings("unchecked")
		Collection<Station> nsStations = stationCollectionCache.get(NS_STATION_COLLECTION_CACHE_KEY);
		
		if (nsStations == null) {
			
			StationInfoResponse stationInfoResponse;
			try {
				stationInfoResponse = nsCommunicator.getStations();
			} catch (NSException e) {
				throw new RuntimeException("NS API error while loading stations: " + e.getMessage(), e);
			}
			
			nsStations = stationInfoResponse.getStations().stream().map(s -> new Station(s.getCode(),
					s.getStationNames().getShortName(), new LatLonCoordinate(s.getLat(), s.getLon()), s.getCountry()))
					.collect(Collectors.toList());
			stationCollectionCache.put(NS_STATION_COLLECTION_CACHE_KEY, Collections.unmodifiableCollection(nsStations));
		}
		
		return nsStations;
	}
	
	@PostConstruct
	public void init() {
		stationCollectionCache = cacheManager.getCache(STATION_COLLECTION_CACHE_NAME, String.class, Collection.class);
	}

}
