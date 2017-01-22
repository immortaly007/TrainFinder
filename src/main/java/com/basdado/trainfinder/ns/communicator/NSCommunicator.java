package com.basdado.trainfinder.ns.communicator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.ns.Constants;
import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.DepartureInfoResponse;
import com.basdado.trainfinder.ns.model.NSErrorResponse;
import com.basdado.trainfinder.ns.model.StationInfoResponse;
import com.basdado.trainfinder.ns.model.TravelAdviceResponse;

public class NSCommunicator {
	
	private static final Logger logger = LoggerFactory.getLogger(NSCommunicator.class);
	
	private NSCommunicatorConfiguration config;
	
	public NSCommunicator(NSCommunicatorConfiguration config) {
		Validate.notNull(config, "NSCommunicatorConfiguration is required");
		this.config = config;
	}
	
	/**
	 * @return A StationInfoResponse containing a list of all known stations and their details.
	 * @throws NSException
	 */
	public StationInfoResponse getStations() throws NSException {
		
		return doNSGetRequest(config.getStationListRequestURL(), StationInfoResponse.class);
	}
	
	/**
	 * Gets all departing trains in the coming hour (with a minimum of 10 departures).
	 * @param station The station for which to calculate this
	 * @return Departure info
	 * @throws NSException
	 */
	public DepartureInfoResponse getDepartures(String station) throws NSException {
		
		return doNSGetRequest(config.getDeparturesRequestUrl().replace("${station}", station), DepartureInfoResponse.class);
	}
	
	/**
	 * Calculate a travel advice for the given journey.
	 * @param fromStation The departing station (required)
	 * @param toStation The destination station (required)
	 * @param viaStation Station through which to travel (optional)
	 * @param previousAdvices The preferred amount of advises before the given dateTime (default and maximum 5)
	 * @param nextAdvices The preferred amount of advises after the given dateTime (default and maximum 5)
	 * @param dateTime The time at which the journey departs or arrives (depending on if departure is true or false).
	 * This parameter is optional, the current dateTime will be used if it is not provided.
	 * @param departure indicates if the given dateTime is the departure time (true) or arrival time (false)
	 * @param hslAllowed indicates if this advice is allowed to take the High-Speed train (HSL), which might be more expensive. (default true)
	 * @param YearCard If the traveler can travel for free, an advice that overshoots the wanted station and then travels back might be preferred.
	 * @return A NS TravelAdviceResponse object containing the travel advises
	 * @throws NSException If the NS API returns an error.
	 */
	public TravelAdviceResponse getTravelAdvice(String fromStation, String toStation, String viaStation, int previousAdvices, int nextAdvices, OffsetDateTime dateTime, boolean departure, boolean hslAllowed, boolean yearCard) throws NSException {
		
		List<NameValuePair> params = new ArrayList<>();
		
		params.add(new BasicNameValuePair("fromStation", fromStation));
		params.add(new BasicNameValuePair("toStation", toStation));
		if (!StringUtils.isBlank(viaStation)) {
			params.add(new BasicNameValuePair("viaStation", viaStation));
		}
		params.add(new BasicNameValuePair("previousAdvices", String.valueOf(previousAdvices)));
		params.add(new BasicNameValuePair("nextAdvices", String.valueOf(nextAdvices)));
		if (dateTime != null) {
			params.add(new BasicNameValuePair("dateTime", Constants.NS_DATETIME_FORMATTER.format(dateTime)));
		}
		params.add(new BasicNameValuePair("departure", String.valueOf(departure)));
		params.add(new BasicNameValuePair("hslAllowed", String.valueOf(hslAllowed)));
		params.add(new BasicNameValuePair("yearCArd", String.valueOf(yearCard)));
		
		String queryParams = URLEncodedUtils.format(params, StandardCharsets.UTF_8);
		
		return doNSGetRequest(config.getTravelAdviceRequestUrl().replace("${parameters}", queryParams), TravelAdviceResponse.class);
	}
	
	public TravelAdviceResponse getTravelAdvice(String fromStation, String toStation, OffsetDateTime dateTime, boolean departure) throws NSException {
		
		List<NameValuePair> params = new ArrayList<>();
		
		params.add(new BasicNameValuePair("fromStation", fromStation));
		params.add(new BasicNameValuePair("toStation", toStation));
		if (dateTime != null) {
			params.add(new BasicNameValuePair("dateTime", Constants.NS_DATETIME_FORMATTER.format(dateTime)));
		}
		params.add(new BasicNameValuePair("departure", String.valueOf(departure)));
		
		String queryParams = URLEncodedUtils.format(params, StandardCharsets.UTF_8);
		
		return doNSGetRequest(config.getTravelAdviceRequestUrl().replace("${parameters}", queryParams), TravelAdviceResponse.class);
		
	}
	
	private <T> T doNSGetRequest(String requestUrl, Class<T> responseClass) throws NSException {
		
		URI requestUri;
		try {
			requestUri = new URI(requestUrl);
		} catch (URISyntaxException e1) {
			throw new IllegalArgumentException("The provided request URL is not a valid URI: '" + requestUrl + "'");
		}
		
		HttpHost nsHost = new HttpHost(requestUri.getHost(), getPort(requestUri), requestUri.getScheme());
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(nsHost), 
				new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
		
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build()) {
			
			HttpGet httpGet = new HttpGet(requestUri);
			
			logger.info("Executing request: " + httpGet.getRequestLine());
			
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				
				String responseBody = EntityUtils.toString(response.getEntity());
				
				logger.debug("Response: " + responseBody);
				
				Object res = unmarshalAs(responseBody, responseClass, NSErrorResponse.class);
				if (responseClass.isInstance(res)) {
					return responseClass.cast(res);
				} else {
					throw new NSException(((NSErrorResponse)res).getMessage());
				}
				
			} catch (JAXBException e) {
				throw new RuntimeException("Exception while unmarshalling response: ", e);
			}
			
		} catch (IOException e) {
			logger.error("Exception while trying to do an HttpGet request", e);
			throw new RuntimeException("EException while trying to do an HttpGet request", e);
		}
	}
	
	/**
	 * Return the port provided in the URI, or the default port for the protocol in case the
	 * protocol is known
	 * @param uri URI 
	 * @return The port provided in the URI, the default port for this protocol (80 for HTTP, 443 for HTTPS), or -1 if undefined.
	 */
	private static int getPort(URI uri) {
		if (uri.getPort() != -1) {
			return uri.getPort();
		} else {
			if ("http".equalsIgnoreCase(uri.getScheme())) {
				return 80;
			} else if ("https".equalsIgnoreCase(uri.getScheme())) {
				return 443;
			}
		}
		return -1;
	}
	
	private static Object unmarshalAs(String xmlStr, Class<?>... classes) throws JAXBException, IOException {
		
		JAXBContext jc = JAXBContext.newInstance(classes);
		
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return unmarshaller.unmarshal(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8)));
	}	
}
