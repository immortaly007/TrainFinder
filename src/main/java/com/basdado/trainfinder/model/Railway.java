package com.basdado.trainfinder.model;

public class Railway {
	
	private final LatLonCoordinate[] nodes;
	private final double[] lengthUntil;
	private final Station from;
	private final Station to;
	
	public Railway(LatLonCoordinate[] nodes, double[] lengthUntil, Station from, Station to) {
		super();
		this.nodes = nodes;
		this.lengthUntil = lengthUntil;
		this.from = from;
		this.to = to;
	}

	public LatLonCoordinate[] getNodes() {
		return nodes;
	}

	public double[] getLengthUntil() {
		return lengthUntil;
	}

	public Station getFrom() {
		return from;
	}

	public Station getTo() {
		return to;
	}
	
}
