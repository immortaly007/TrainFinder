package com.basdado.trainfinder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.basdado.trainfinder.util.CoordinateUtil;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmRailwayMap {
	
	private Map<Long, OsmRailwayMapNode> nodes;
	
	public OsmRailwayMap() {
		nodes = new HashMap<>();
	}
	
	/**
	 * Add a node
	 * @param node
	 */
	public void addNode(OsmNode node) {
		addNode(node.getId(), new OsmRailwayMapNode(new LatLonCoordinate(node.getLatitude(), node.getLongitude())));
	}
	
	public void addNode(Long id, OsmRailwayMapNode node) {
		nodes.put(id, node);
	}
	
	/**
	 * Adds an OsmWay to this railway map, adding connections for all nodes within the way.
	 * Throws a RuntimeException if the way contains a node that could not be found.
	 * @param way
	 */
	public void addWay(OsmWay way) {
		
		if (way == null || way.getNumberOfNodes() == 0) return;
		
		List<Long> osmNodeIds = new ArrayList<>();
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			osmNodeIds.add(way.getNodeId(i));
		}
		
		addWay(osmNodeIds);
		
	}
	
	public void addWay(List<Long> way) {
		
		long lastNodeId = way.get(0);
		OsmRailwayMapNode lastNode = nodes.get(lastNodeId);
		
		for (long nodeId : way) {
			
			if (nodeId == lastNodeId) continue;
			
			OsmRailwayMapNode node = nodes.get(nodeId);
			
			Validate.notNull(lastNode, "Last node not found, nodeId " + lastNodeId);
			Validate.notNull(node, "Node not found, nodeId " + nodeId);
			
			// Connect the last node and the current node
			double dist = CoordinateUtil.dist(lastNode.getPosition(), node.getPosition());
			lastNode.addConnection(nodeId, dist);
			node.addConnection(lastNodeId, dist);
			
			// Update the last node
			lastNodeId = nodeId;
			lastNode = node;
		}
	}
	
	/**
	 * Removes nodes that are not part of ways
	 */
	public void clean() {
		
		for(Iterator<Map.Entry<Long, OsmRailwayMapNode>> nodeIterator = nodes.entrySet().iterator(); nodeIterator.hasNext();) {
			Map.Entry<Long, OsmRailwayMapNode> nodeEntry = nodeIterator.next();
			if (!nodeEntry.getValue().hasConnections()) {
				nodeIterator.remove();
			}
		}
	}
	
	public Map<Long, OsmRailwayMapNode> getNodes() {
		return Collections.unmodifiableMap(nodes);
	}
	
	public OsmRailwayMapNode getNode(Long id) {
		return nodes.get(id);
	}
	
	/**
	 * Returns all nodes that are within maxDistance of the given position.
	 * @param pos Coordinate
	 * @param maxDistance The maximum distance nodes may have.
	 * @return
	 */
	public List<Long> findNodesNear(LatLonCoordinate pos, double maxDistance) {
		
		List<Pair<Long, Double>> nearbyNodes = new ArrayList<>();
		
		for (Map.Entry<Long, OsmRailwayMapNode> nodeEntry: nodes.entrySet()) {
			double dist = CoordinateUtil.dist(pos, nodeEntry.getValue().getPosition());
			if (dist < maxDistance) {
				nearbyNodes.add(new ImmutablePair<>(nodeEntry.getKey(), dist));
			}
		}
		
		return nearbyNodes.stream()
				.sorted(Comparator.comparing(Pair<Long,Double>::getRight)) // Sort by distance
				.map(n -> n.getLeft()).collect(Collectors.toList()); // To list
	}
	
	/**
	 * @return The longest distance between two neighboring nodes on the railway map.
	 */
	public double getLongestNodeDist() {
		
		double maxDistance = 0;
		for (OsmRailwayMapNode node : nodes.values()) {
			for (Map.Entry<Long, Double> c : node.getConnections().entrySet()) {
				if (c.getValue() > maxDistance) {
					maxDistance = c.getValue();
				}
			}
		}
		return maxDistance;
	}
	
	
	public static class OsmRailwayMapNode {
		
		private final LatLonCoordinate position;
		Map<Long, Double> connections;
		
		public OsmRailwayMapNode(LatLonCoordinate position) {
			this.position = position;
			this.connections = new HashMap<>();
		}
		
		public LatLonCoordinate getPosition() {
			return position;
		}
		
		public Map<Long, Double> getConnections() {
			return connections;
		}
		
		public boolean isReachable(long nodeId) {
			return connections.containsKey(nodeId);
		}
		
		public void addConnection(long nodeId, double dist) {
			connections.put(nodeId, dist);
		}
		
		public void removeConnection(long nodeId) {
			connections.remove(nodeId);
		}
		
		public boolean hasConnections() {
			return !connections.isEmpty();
		}
	}
}