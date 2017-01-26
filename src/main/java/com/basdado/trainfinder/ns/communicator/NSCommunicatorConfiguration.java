package com.basdado.trainfinder.ns.communicator;

public interface NSCommunicatorConfiguration {
	
	static final String DEFAULT_STATION_LIST_REQUEST_URL = "https://webservices.ns.nl/ns-api-stations-v2";
	static final String DEFAULT_DEPARTURES_REQUEST_URL = "https://webservices.ns.nl/ns-api-avt?station=${station}";
	static final String DEFAULT_TRAVEL_ADVICE_REQUEST_URL = "https://webservices.ns.nl/ns-api-treinplanner?${parameters}";
	
	/**
	 * @return The NS provided username, required to access the API.
	 */
	String getUsername();
	
	/**
	 * @return The NS provided password, required to access the API.
	 */
	String getPassword();
	
	/**
	 * @return The full request URL to retrieve the station list. e.g.: https://webservices.ns.nl/ns-api-stations-v2
	 */
	default String getStationListRequestURL() {
		return DEFAULT_STATION_LIST_REQUEST_URL;
	}
	
	/**
	 * @return The full request URL to retrieve the departures at a certain station. 
	 * Should contain the parameter: "${station}", which will be replied by the supplied station.
	 * E.g.: https://webservices.ns.nl/ns-api-avt?station=${station}
	 */
	default String getDeparturesRequestUrl() {
		return DEFAULT_DEPARTURES_REQUEST_URL;
	}
	
	/**
	 * @return The full request URL to retrieve travel advice for a given route.
	 * Should contain the parameter "${parameters}", which will be replaced by the query parameters
	 * E.g.: https://webservices.ns.nl/ns-api-avt?${parameters}
	 */
	default String getTravelAdviceRequestUrl() {
		return DEFAULT_TRAVEL_ADVICE_REQUEST_URL;
	}
}
