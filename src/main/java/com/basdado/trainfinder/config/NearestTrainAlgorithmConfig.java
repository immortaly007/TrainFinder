package com.basdado.trainfinder.config;

import org.apache.commons.configuration2.Configuration;

public class NearestTrainAlgorithmConfig {

	private static final String PREFIX = "NearestTrainAlgorithm.";
	
	private final double distanceSigma;
	private final double bearingSigma;
	private final double positionWeight;
	private final double bearingWeight;
	
	public NearestTrainAlgorithmConfig(Configuration config) {
		distanceSigma = config.getDouble(PREFIX + "DistanceSigma");
		bearingSigma = config.getDouble(PREFIX + "BearingSigma");
		positionWeight = config.getDouble(PREFIX + "PositionWeight");
		bearingWeight = config.getDouble(PREFIX + "BearingWeight");
	}
	
	public double getDistanceSigma() {
		return distanceSigma;
	}
	
	public double getBearingSigma() {
		return bearingSigma;
	}
	
	public double getPositionWeight() {
		return positionWeight;
	}
	
	public double getBearingWeight() {
		return bearingWeight;
	}
}
