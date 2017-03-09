package com.basdado.trainfinder.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.basdado.trainfinder.model.OsmRailwayMap;
import com.basdado.trainfinder.model.OsmRailwayMap.OsmRailwayMapNode;

public class RailwayMapUtil {
	
	
	/**
	 * Uses Dijkstra algorithm to find the shortest path from the source node to one of the destination nodes. 
	 * @param sourceNodeId
	 * @param destNodes
	 * @return
	 */
	public static List<Long> calculateShortestPathBetween(OsmRailwayMap railwayMap, Long sourceNodeId, Set<Long> destNodes) {
		
//		Map<Long, Double> dist = new HashMap<>();
		Map<Long, Long> prev = new HashMap<>();
		PriorityHeap<Long> toVisit = new PriorityHeap<>();
		Set<Long> visited = new HashSet<>();
		Set<Long> toVisitSet = new HashSet<>();

		toVisit.add(sourceNodeId, 0.0);
		
		boolean reachedDestination = false;
		Long destNodeId = null;
		while(!reachedDestination) {
			
			// Find the nearest (node with the smallest distance) unchecked node
			double uDist = toVisit.peekRating();
			Long uId = toVisit.remove();
			toVisitSet.remove(uId);
			
			if (uId == null) {
				return null; // We checked all reachable nodes, but no path to a destination node was found
			}
			if (destNodes.contains(uId)) {
				reachedDestination = true;
				destNodeId = uId;
			} else {
			
				OsmRailwayMapNode u = railwayMap.getNode(uId);
				
				Map<Long, Double> connections = u.getConnections();
				for (Map.Entry<Long, Double> connection : connections.entrySet()) {
					Long cId = connection.getKey();
					Double cDist = connection.getValue();
					Double newConnectionDistance = uDist + cDist;
					boolean newShortestPath = false;
					if (toVisitSet.contains(cId)) {
						newShortestPath = toVisit.updateMinRating(cId, newConnectionDistance);
					} else if (!visited.contains(cId)) {
						toVisit.add(cId, newConnectionDistance);
						toVisitSet.add(cId);
						newShortestPath = true;
					}
					
					if (newShortestPath) {
						prev.put(cId, uId);
					}
				}
				
				visited.add(uId);
			}
		}
		
		LinkedList<Long> result = new LinkedList<>();
		Long curNodeId = destNodeId;
		while(result.isEmpty() || result.getFirst() != sourceNodeId) {
			result.addFirst(curNodeId);
			curNodeId = prev.get(curNodeId);
		}
		return result;		
	}

}
