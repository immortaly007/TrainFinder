package com.basdado.trainfinder.config;

import org.apache.commons.configuration2.Configuration;

public class NSAPIConfiguration {

	private final String username;
	private final String password;
	
	public NSAPIConfiguration(Configuration config) {
		this.username = config.getString("NSApi.Username");
		this.password = config.getString("NSApi.Password");
	}
	
	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

}
