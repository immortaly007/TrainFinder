package com.basdado.trainfinder.config;


import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.AbsoluteNameLocationStrategy;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ConfigService {

	private final Logger logger = LoggerFactory.getLogger(ConfigService.class);
	
	private NSAPIConfiguration nsApiConfig;
	private OpenStreetMapConfiguration openStreetMapConfig;
	
	@PostConstruct
	public void init() {
		
		String configDir = System.getProperty("jboss.server.config.dir");
		
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<XMLConfiguration> defaultConfigBuilder =
			    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
			    .configure(params.xml()
			        .setFileName("DefaultConfiguration.xml")
			        .setLocationStrategy(new ClasspathLocationStrategy())
			        .setValidating(false)); 
		
		final XMLConfiguration defaultConfig;
		try {
			defaultConfig = defaultConfigBuilder.getConfiguration();
		} catch (ConfigurationException e) {
			throw new RuntimeException("Default configuration is not valid or not found. This shouldn't happen", e);
		}
		
		FileBasedConfigurationBuilder<XMLConfiguration> userConfigBuilder =
			    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
			    .configure(params.xml()
			        .setFileName(configDir + "/applications/trainfinder/Configuration.xml")
			        .setLocationStrategy(new AbsoluteNameLocationStrategy())
			        .setValidating(false)); 
		
		XMLConfiguration userConfig;
		try {
			userConfig = userConfigBuilder.getConfiguration();
		} catch (ConfigurationException e) {
			logger.warn("Could not load user configuration: " + e.getMessage(), e);
			userConfig = null;
		}
		
		NodeCombiner combiner = new MergeCombiner();
		
		CombinedConfiguration config = new CombinedConfiguration(combiner);
		config.addConfiguration(userConfig);
		config.addConfiguration(defaultConfig);
		
		nsApiConfig = new NSAPIConfiguration(config);
		openStreetMapConfig = new OpenStreetMapConfiguration(config);
		
	}
	
	public NSAPIConfiguration getNSAPIConfiguration() {
		return nsApiConfig;
	}
	
	public OpenStreetMapConfiguration getOpenStreetMapConfiguration() {
		return openStreetMapConfig;
	}
	
}
