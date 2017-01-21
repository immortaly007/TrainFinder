package com.basdado.trainfinder.config;

import org.apache.commons.configuration2.*;

import com.basdado.trainfinder.ns.communicator.NSCommunicatorConfiguration;

public class NSAPIConfiguration implements NSCommunicatorConfiguration {

	private final String username;
	private final String password;
	
	public NSAPIConfiguration(Configuration config) {
		this.username = config.getString("NSApi.Username");
		this.password = config.getString("NSApi.Password");
	}
	
	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

}
