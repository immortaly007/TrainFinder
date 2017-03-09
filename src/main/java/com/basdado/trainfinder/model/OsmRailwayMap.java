package com.basdado.trainfinder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.basdado.trainfinder.util.CoordinateUtil;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmRailwayMap {
	
	private static final double TILE_SIZE = 0.1;
	
	private final Map<Long, OsmRailwayMapNode> nodes;
	private final GeoGrid<Set<Long>> grid;
	
	/**
	 * Cache for the longest distance between two nodes on the map.
	 */
	private double longestNodeConnectionDistance;
	/**
	 * Cache for the two nodes between which the longest distance on the map exists.
	 */
	private Pair<Long, Long> longestNodeConnection;
	
	// TODO add acceleration structure on the nodes to improve the performance of #findNodesNear()
	// e.g. construct a grid, and store which node are in each grid tile. Then you only have to search nodes in the nearest grid tiles.
	
	public OsmRailwayMap() {
		nodes = new HashMap<>();
		grid = new GeoGrid<>(TILE_SIZE);
		longestNodeConnectionDistance = 0;
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
		
		// Update the grid
		Set<Long> tile = grid.getTileAt(node.getPosition());
		if (tile == null) {
			tile = new HashSet<>();
			grid.setTileAt(node.getPosition(), tile);
		}
		tile.add(id);
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
			if (dist > longestNodeConnectionDistance) {
				longestNodeConnectionDistance = dist;
				longestNodeConnection = Pair.of(nodeId, lastNodeId);
			}
			
			// Update the last node
			lastNodeId = nodeId;
			lastNode = node;
		}
	}
	
	public void removeConnection(Long node1Id, Long node2Id) {
		
		nodes.get(node1Id).removeConnection(node2Id);
		nodes.get(node2Id).removeConnection(node1Id);
		
		if (longestNodeConnection.equals(Pair.of(node1Id, node2Id)) || longestNodeConnection.equals(Pair.of(node2Id, node1Id))) {
			updateLongestNodeDist();
		}
	}
	
	/**
	 * Removes nodes that are not part of ways (don't have any connections).
	 */
	public void clean() {
		
		List<Long> nodesToRemove = new LinkedList<>();
		for(Map.Entry<Long, OsmRailwayMapNode> nodeEntry : nodes.entrySet()) {
			if (!nodeEntry.getValue().hasConnections()) {
				nodesToRemove.add(nodeEntry.getKey());
			}
		}
		
		for (Long nodeId : nodesToRemove) {
			removeNode(nodeId);
		}
	}
	
	public void removeNode(Long nodeId) {
		OsmRailwayMapNode node = nodes.remove(nodeId);
		if (node != null) {
			Set<Long> nodeTile = grid.getTileAt(node.getPosition());
			nodeTile.remove(nodeId);
		}
		for (Long connectedNodeId : node.getConnections().keySet()) {
			OsmRailwayMapNode connectedNode = nodes.get(connectedNodeId);
			connectedNode.removeConnection(nodeId);
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
		
		int centerX = grid.calculateHorizontalTileIdx(pos);
		int centerY = grid.calculateVerticalTileIdx(pos);
		
		int minTileX = centerX - 1;
		while (grid.getDistanceToTile(pos, minTileX, centerY) < maxDistance) {
			minTileX --;
		}
		
		int maxTileX = centerX + 1;
		while (grid.getDistanceToTile(pos, maxTileX, centerY) < maxDistance) {
			maxTileX++;
		}
		
		int minTileY = centerY - 1;
		while (grid.getDistanceToTile(pos, centerX, minTileY) < maxDistance) {
			minTileY --;
		}
		
		int maxTileY = centerY + 1;
		while (grid.getDistanceToTile(pos, centerX, maxTileY) < maxDistance) {
			maxTileY++;
		}
		
		List<Pair<Long, Double>> nearbyNodes = new ArrayList<>();
		
		for (int x = minTileX + 1; x < maxTileX; x++) {
			for (int y = minTileY + 1; y < maxTileY; y++) {
				
				Set<Long> tile = grid.getTile(x, y);
				if (tile == null || tile.isEmpty()) {
					continue;
				} else {
					
					if (grid.getDistanceToTile(pos, x, y) < maxDistance) {
						for (Long nodeId : tile) {
							OsmRailwayMapNode node = getNode(nodeId);
							double dist = CoordinateUtil.dist(pos, node.getPosition());
							if (dist < maxDistance) {
								nearbyNodes.add(new ImmutablePair<>(nodeId, dist));
							}
						}
					}
				}
			}
		}
		
		return nearbyNodes.stream()
				.sorted(Comparator.comparing(Pair<Long,Double>::getRight)) // Sort by distance
				.map(n -> n.getLeft()).collect(Collectors.toList()); // To list
	}
	
	public double getLongestNodeConnectionDistance() {
		return longestNodeConnectionDistance;
	}
	
	/**
	 * @return The longest distance between two neighboring nodes on the railway map.
	 */
	public void updateLongestNodeDist() {
		
		longestNodeConnectionDistance = 0;
		for (Map.Entry<Long, OsmRailwayMapNode> nodeEntry : nodes.entrySet()) {
			OsmRailwayMapNode node = nodeEntry.getValue();
			for (Map.Entry<Long, Double> c : node.getConnections().entrySet()) {
				if (c.getKey() > nodeEntry.getKey() && // Only check connections in ascending order
						c.getValue() > longestNodeConnectionDistance) {
					longestNodeConnectionDistance = c.getValue();
					longestNodeConnection = Pair.of(nodeEntry.getKey(), c.getKey());
				}
			}
		}
		
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
			return Collections.unmodifiableMap(connections);
		}
		
		public Double getDistanceToConnectedNode(long connectedNodeId) {
			return connections.get(connectedNodeId);
		}
		
		public boolean isReachable(long nodeId) {
			return connections.containsKey(nodeId);
		}
		
		protected void addConnection(long nodeId, double dist) {
			connections.put(nodeId, dist);
		}
		
		protected void removeConnection(long nodeId) {
			connections.remove(nodeId);
		}
		
		public boolean isConnectedTo(long otherNodeId) {
			return connections.containsKey(otherNodeId);
		}
		
		public boolean hasConnections() {
			return !connections.isEmpty();
		}
	}
	
}