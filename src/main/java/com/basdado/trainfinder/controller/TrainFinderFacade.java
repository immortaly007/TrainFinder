package com.basdado.trainfinder.controller;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.basdado.trainfinder.config.ConfigService;
import com.basdado.trainfinder.config.NearestTrainAlgorithmConfig;
import com.basdado.trainfinder.model.LatLng;
import com.basdado.trainfinder.model.Train;
import com.basdado.trainfinder.util.CoordinateUtil;
import com.basdado.trainfinder.util.PriorityHeap;
import com.basdado.trainfinder.util.statistics.Distribution;
import com.basdado.trainfinder.util.statistics.NormalDistribution;

@Stateless
public class TrainFinderFacade {
	
	@Inject private TrainStatusFacade trainStatusFacade;
	@Inject private ConfigService config;
	
	/**
	 * Used to map the distance of a train to it's plausibility
	 */
	private Distribution distanceDistribution;
	private Distribution bearingDistribution;
	
	@PostConstruct
	public void init() {
		NearestTrainAlgorithmConfig algorithmConfig = config.getNearestTrainAlgorithmConfig();
		distanceDistribution = new NormalDistribution(0, algorithmConfig.getDistanceSigma());
		bearingDistribution = new NormalDistribution(0, algorithmConfig.getBearingSigma());
	}
	
	public Train getNearestTrain(LatLng pos, double bearing) {
		
		Collection<Train> trains = trainStatusFacade.getCurrentTrains();
		PriorityHeap<Train> trainScores = new PriorityHeap<>();
				
		for (Train train: trains) {
			
			double trainPlausibility = calculateTrainPlausibility(train, pos, bearing);
			trainScores.add(train, 1.0 - trainPlausibility); // smallest score = first train returned
			
		}
		
		return trainScores.remove();
		
	}
	
	/**
	 * Calculates the plausibility of this train being the train some user with the given position and bearing is in. 
	 * @param train
	 * @param pos
	 * @param bearing
	 * @return The plausibility [0, 1) of this train being the train the user is in.
	 */
	private double calculateTrainPlausibility(Train train, LatLng pos, double bearing) {
		
		NearestTrainAlgorithmConfig algorithmConfig = config.getNearestTrainAlgorithmConfig();
		double positionWeight = algorithmConfig.getPositionWeight();
		double bearingWeight = algorithmConfig.getBearingWeight();
		
		double dist = CoordinateUtil.dist(pos, train.getPosition());
		double distPlaus = distanceDistribution.density(dist) * distanceDistribution.densityNormalizer();
		
		double bearingDiff = bearing // TODO bearing difference and rating, weighing the values
		
		
	}

}
