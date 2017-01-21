package com.basdado.trainfinder.ns.communicator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.ns.exception.NSException;
import com.basdado.trainfinder.ns.model.DepartureInfoResponse;
import com.basdado.trainfinder.ns.model.NSErrorResponse;
import com.basdado.trainfinder.ns.model.StationInfoResponse;

@Stateless
public class NSCommunicator {
	
	private static final String NS_HOST = "webservices.ns.nl";
	private static final int NS_PORT = 443;
	
	private static final String STATION_LIST_REQUEST_URL = "https://webservices.ns.nl/ns-api-stations-v2";
	private static final String DEPARTURES_REQUEST_URL = "https://webservices.ns.nl/ns-api-avt?station=${stationCode}";
	
	Logger logger = LoggerFactory.getLogger(NSCommunicator.class);
	
	@Inject NSCommunicatorConfiguration config;
	
	public StationInfoResponse getStations() throws NSException {
		
		return doNSGetRequest(STATION_LIST_REQUEST_URL, StationInfoResponse.class);
	}
	
	public DepartureInfoResponse getDepartures(String stationCode) throws NSException {
		
		return doNSGetRequest(DEPARTURES_REQUEST_URL.replace("${stationCode}", stationCode), DepartureInfoResponse.class);
	}
	
	
	private <T> T doNSGetRequest(String requestUrl, Class<T> responseClass) throws NSException {
		
		HttpHost nsHost = new HttpHost(NS_HOST, NS_PORT, "https");
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(nsHost), 
				new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
		
		
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build()) {
			
			HttpGet httpGet = new HttpGet(requestUrl);
			
			logger.info("Executing request: " + httpGet.getRequestLine());
			
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				
				String responseBody = EntityUtils.toString(response.getEntity());
				
				logger.info("Response: " + responseBody);
				
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
	
	private static Object unmarshalAs(String xmlStr, Class<?>... classes) throws JAXBException, IOException {
		
		JAXBContext jc = JAXBContext.newInstance(classes);
		
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return unmarshaller.unmarshal(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8)));
	}
	
	
}
