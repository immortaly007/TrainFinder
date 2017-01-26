package com.basdado.trainfinder.data;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.StationInfoResponse;

@Stateless
@Local(StationRepository.class)
public class NSStationRepository implements StationRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(NSStationRepository.class);

	@Inject private NSCommunicator nsCommunicator;
	@Inject private CacheManager cacheManager;
	
	private static final String STATION_COLLECTION_CACHE_NAME = "stationCollectionCache";
	private static final String STATION_BY_CODE_CACHE_NAME = "stationByNameCache";
	private static final String STATION_BY_NAME_CACHE_NAME = "stationByCodeCache";
	private static final String NS_STATION_COLLECTION_CACHE_KEY = "nsStations";
	
	@SuppressWarnings("rawtypes")
	private Cache<String, Collection> stationCollectionCache;
	private Cache<String, Station> stationByNameCache;
	private Cache<String, Station> stationByCodeCache;
	
	
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
					s.getStationNames().getShortName(), s.getStationNames().getLongName(), new LatLonCoordinate(s.getLat(), s.getLon()), s.getCountry()))
					.collect(Collectors.toList());
			
			stationCollectionCache.put(NS_STATION_COLLECTION_CACHE_KEY, Collections.unmodifiableCollection(nsStations));
			
			nsStations.forEach(s -> stationByNameCache.put(simplifyName(s.getFullName()), s));
			nsStations.forEach(s -> stationByNameCache.put(simplifyName(s.getShortName()), s));
			nsStations.forEach(s -> stationByCodeCache.put(s.getCode(), s));
		}
		
		return nsStations;
	}
	
	@PostConstruct
	public void init() {
		stationCollectionCache = cacheManager.getCache(STATION_COLLECTION_CACHE_NAME, String.class, Collection.class);
		stationByNameCache = cacheManager.getCache(STATION_BY_NAME_CACHE_NAME, String.class, Station.class);
		stationByCodeCache = cacheManager.getCache(STATION_BY_CODE_CACHE_NAME, String.class, Station.class);
	}

	@Override
	public Station getStationWithCode(String code) {
		if (code == null) return null;
		
		Station station = stationByCodeCache.get(code);
		if (station == null) {
			Collection<Station> stations = getStations();
			station = stationByCodeCache.get(code);
			if (station == null) { // Maybe the cache doesn't work, try to find it in the returned stations collections
				station = stations.stream().filter(s -> s.getCode().equals(code)).findFirst().orElse(null);
			}
		}
		
		if (station == null) {
			logger.warn("Could not find station with code: " + code);
		}
		
		return station;
	}

	@Override
	public Station getStationWithName(String name) {
		if (name == null) return null;
		
		String nameKey = simplifyName(name);
		
		Station station = stationByNameCache.get(nameKey);
		if (station == null) {
			Collection<Station> stations = getStations();
			station = stationByNameCache.get(nameKey);
			if (station == null) { // Maybe the cache doesn't work, try to find it in the returned stations collections
				station = stations.stream().filter(s -> 
						nameKey.equals(simplifyName(s.getShortName())) || 
						nameKey.equals(simplifyName(s.getFullName())))
						.findFirst().orElse(null);
			}
		}
		
		if (station == null) {
			logger.warn("Could not find station with name: " + name);
		}
		
		return station;
	}
	
	/**
	 * Simplifies the name, making it all uppercase and replacing all non-letter non-number symbols (whitespace etc)
	 * @param name
	 * @return
	 */
	public String simplifyName(String name) {
		String simplified = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		simplified = simplified.toUpperCase();
		simplified = simplified.replaceAll("[^A-Z0-9]", "");
		return simplified;
	}

}
