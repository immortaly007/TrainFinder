package com.basdado.trainfinder.util.statistics;

public class NormalDistribution implements Distribution {
	
	private static final double INV_SQRT_PI = 1.0 / Math.sqrt(Math.PI);
	
	private final double mu;
	private final double sigma;
	private final double sigmaInv;
	
	
	public NormalDistribution() {
		this(0, 1);
	}
	
	public NormalDistribution(double mu, double sigma) {
		this.mu = mu;
		this.sigma = sigma;
		this.sigmaInv = 1.0 / sigma;
	}

	public double getMu() {
		return mu;
	}
	
	public double getSigma() {
		return sigma;
	}
	
	@Override
	public double density(double value) {
		return sigmaInv  * phi((value - mu) * sigmaInv);
	}
	
	private double phi(double x) {
		return Math.pow(Math.E, -(x * x)) * INV_SQRT_PI;
	}

	@Override
	public double densityNormalizer() {
		return sigmaInv;
	}
	
}
