/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basdado.trainfinder.util;

import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.basdado.trainfinder.config.ConfigService;
import com.basdado.trainfinder.ns.communicator.NSCommunicator;
import com.basdado.trainfinder.ns.communicator.NSCommunicatorConfiguration;


/**
 * Utility class that produces some resources such as loggers etc.
 * 
 */
public class Resources {

	@Inject ConfigService configService;
	
    @Produces
    public Logger produceLog(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
    }
    
    @Produces
    public CacheManager produceCacheManager(InjectionPoint injectionPoint) {
    	
    	CachingProvider cachingProvider = Caching.getCachingProvider();
    	CacheManager manager;
		try {
			manager = cachingProvider.getCacheManager( 
			    injectionPoint.getMember().getDeclaringClass().getClassLoader().getResource("ehcache.xml").toURI(), 
			    injectionPoint.getMember().getDeclaringClass().getClassLoader());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} 
    	
    	return manager;
    }

    @Produces
    public NSCommunicator produceNSCommunicator(InjectionPoint injectionPoint) {
    	
    	NSCommunicatorConfiguration nsCommunicatorConfig = new NSCommunicatorConfiguration() {
			
			@Override
			public String getUsername() {
				return configService.getNSAPIConfiguration().getUsername();
			}
			
			@Override
			public String getPassword() {
				return configService.getNSAPIConfiguration().getPassword();
			}
		};
		
		return new NSCommunicator(nsCommunicatorConfig);
    	
    }
}
