package com.basdado.trainfinder.model;

import java.util.Arrays;

import com.basdado.trainfinder.util.CoordinateUtil;

public class Railway {
	
	private final LatLng[] nodes;
	private final double[] lengthUntil;
	private final Station from;
	private final Station to;
	
	public Railway(LatLng[] nodes, double[] lengthUntil, Station from, Station to) {
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
	
	public LatLng getNodePosition(int index) {
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
	public PositionAndBearing calculatePositionAndBearingForProgress(double f) {
		
		double distanceTraveled = f * getLength();
		int binSearchResult = Arrays.binarySearch(lengthUntil, distanceTraveled);
		if (binSearchResult >= 0) { // We are exactly at some node, so we can return it.
			int nextNodeId = binSearchResult + 1;
			int thisNodeId = binSearchResult;
			if (nextNodeId >= nodes.length) {
				thisNodeId--;
				nextNodeId--;
			}
			return new PositionAndBearing(nodes[binSearchResult], bearing(thisNodeId, nextNodeId));
		} else { // Time to interpolate
			int nextNodeId = -binSearchResult;
			if (nextNodeId >= nodes.length) {
				return new PositionAndBearing(nodes[nodes.length - 1], bearing(nodes.length - 1, nodes.length));
			}
			int previousNodeId = nextNodeId - 1; // > 0, because binSearchResult < 0.
			
			double distanceToPreviousNode = lengthUntil[previousNodeId];
			double distanceToNextNode = lengthUntil[nextNodeId];
			
			double nodeProgress = (distanceTraveled - distanceToPreviousNode) / (distanceToNextNode - distanceToPreviousNode);
			
			LatLng pos = CoordinateUtil.interpolate(nodes[previousNodeId], nodes[nextNodeId], nodeProgress);
			double bearing = bearing(previousNodeId, nextNodeId);
			
			return new PositionAndBearing(pos, bearing);
		}
		
	}
	
	private double bearing(int node1Id, int node2Id) {
		LatLng node1 = nodes[node1Id];
		LatLng node2 = nodes[node2Id];
		return CoordinateUtil.bearing(node1, node2);
	}
	
	public Railway reversed() {
		
		LatLng[] reversedNodes = new LatLng[nodes.length];
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
