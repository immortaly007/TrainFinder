package com.basdado.trainfinder.model;

import java.util.Arrays;

import com.basdado.trainfinder.util.CoordinateUtil;

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

	public Station getFrom() {
		return from;
	}

	public Station getTo() {
		return to;
	}
	
	/**
	 * @return The number of nodes in this railway.
	 */
	public int getNodeCount() {
		return nodes.length;
	}
	
	/**
	 * @return The total length (in meters) of this railway.
	 */
	public double getLength() {
		return lengthUntil[lengthUntil.length - 1];
	}
	
	public LatLonCoordinate getNodePosition(int index) {
		return nodes[index];
	}
	
	public double getDistanceUntil(int index) {
		return lengthUntil[index];
	}
	
	/**
	 * Calculates the position (coordinate) if the train has progressed for f% along this railway.
	 * @param f The percentage [0, 1] the train has progressed along this Railway.
	 * @return The position (coordinate).
	 */
	public LatLonCoordinate calculatePositionForProgress(double f) {
		
		double distanceTraveled = f * getLength();
		int binSearchResult = Arrays.binarySearch(lengthUntil, distanceTraveled);
		if (binSearchResult >= 0) { // We are exactly at some node, so we can return it.
			return nodes[binSearchResult];
		} else { // Time to interpolate
			int nextNodeId = -binSearchResult;
			if (nextNodeId >= nodes.length) {
				return nodes[nodes.length - 1];
			}
			int previousNodeId = nextNodeId - 1; // > 0, because binSearchResult < 0.
			
			double distanceToPreviousNode = lengthUntil[previousNodeId];
			double distanceToNextNode = lengthUntil[nextNodeId];
			
			double nodeProgress = (distanceTraveled - distanceToPreviousNode) / (distanceToNextNode - distanceToPreviousNode);
			
			return CoordinateUtil.interpolate(nodes[previousNodeId], nodes[nextNodeId], nodeProgress);
		}
		
	}
	
	public Railway reversed() {
		
		LatLonCoordinate[] reversedNodes = new LatLonCoordinate[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			reversedNodes[nodes.length - 1 - i] = nodes[i];
		}
		
		double[] reversedLengthUntil = new double[lengthUntil.length];
		double totalLength = lengthUntil[lengthUntil.length - 1];
		for (int i = 0; i < lengthUntil.length; i++) {
			reversedLengthUntil[lengthUntil.length - 1 - i] = totalLength - lengthUntil[i];
		}
		
		return new Railway(reversedNodes, reversedLengthUntil, to, from);
		
	}
}
