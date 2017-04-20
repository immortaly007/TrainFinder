package com.basdado.trainfinder.util.statistics;

public interface Distribution {

	public double density(double value);
	
	/**
	 * @return Normalizes the density result, such that when it is multiplied with this value, the maximum density value you can get is 1.0.
	 */
	public double densityNormalizer();
}
