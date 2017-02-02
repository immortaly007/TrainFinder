package com.basdado.trainfinder.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.config.ConfigService;
import com.basdado.trainfinder.config.OpenStreetMapConfiguration;
import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.OsmRailwayMap;
import com.basdado.trainfinder.model.OsmRailwayMap.OsmRailwayMapNode;
import com.basdado.trainfinder.util.CoordinateUtil;
import com.basdado.trainfinder.model.Station;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;

@Singleton
public class TrainRoutingController {
	
	private static final String RAILWAY_PATH_CACHE_KEY = "railwayPathCache";
	
	private static final Logger logger = LoggerFactory.getLogger(TrainRoutingController.class);
	
	@Inject private ConfigService configService;
	@Inject private CacheManager cacheManager;
	
	private OsmRailwayMap railwayMap;
	private Map<String, Boolean> nodesAddedNearStation;
	
	private Cache<String, List<LatLonCoordinate>> pathCache;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		
		OpenStreetMapConfiguration osmConfig = configService.getOpenStreetMapConfiguration();
		
		OsmXmlReader xmlReader;
		try {
			final String railRoadFile = osmConfig.getRailroadFile();
			logger.info("Reading railway map at: " + railRoadFile);
			xmlReader = new OsmXmlReader(railRoadFile, true);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found: " + osmConfig.getRailroadFile(), e);
		}
		
		this.railwayMap = new OsmRailwayMap();
		xmlReader.setHandler(new OsmRailwayMapReader(railwayMap));
		try {
			xmlReader.read();
		} catch (OsmInputException e) {
			throw new IllegalArgumentException("Error in OSM file", e);
		}
		nodesAddedNearStation = new HashMap<>();
		
		logger.info("Railway map was read succesfully, using " + railwayMap.getNodes().size() + " nodes");
		
		pathCache = (Cache<String, List<LatLonCoordinate>>)(Cache<?,?>) // Some casting magic...
				cacheManager.getCache(RAILWAY_PATH_CACHE_KEY, String.class, List.class);
	}
	
	public List<LatLonCoordinate> getRailway(Station from, Station to) {
		
		if (nodesAddedNearStation.get(from.getCode()) == null || !nodesAddedNearStation.get(from.getCode())) {
			addNodesOnTracksNear(from.getLocation());
			nodesAddedNearStation.put(from.getCode(), true);
		}
		
		if (nodesAddedNearStation.get(to.getCode()) == null || !nodesAddedNearStation.get(to.getCode())) {
			addNodesOnTracksNear(to.getLocation());
			nodesAddedNearStation.put(to.getCode(), true);
		}
		
		List<LatLonCoordinate> res = pathCache.get(getCacheKey(from, to));
		if (res == null) {
			// Try to get it from the inverse path:
			List<LatLonCoordinate> inverseRes = pathCache.get(getCacheKey(to, from));
			if (inverseRes != null) {
				res = new ArrayList<LatLonCoordinate>(inverseRes);
				Collections.reverse(res);
			}
		}
		if (res == null) {
			// Actually calculate it
			logger.info("Calculating path from station " + from.getShortName() + "(" + from.getCode() + ") to " + to.getShortName() + "(" + to.getCode() + ").");
			res = getPath(from.getLocation(), to.getLocation());
			pathCache.put(getCacheKey(from, to), Collections.unmodifiableList(res));
			
		}
		
		return getPath(from.getLocation(), to.getLocation());
	}
	
	private String getCacheKey(Station from, Station to) {
		return from.getCode() + "-" + to.getCode();
	}
	
	private List<LatLonCoordinate> getPath(LatLonCoordinate from, LatLonCoordinate to) {
		
		Map<Long, OsmRailwayMapNode> nodes = railwayMap.getNodes();
		
		List<Long> sourceNodes = findNodesNear(from);
		if (sourceNodes.isEmpty()) {
			sourceNodes = addNodesOnTracksNear(from);
		}
		if (sourceNodes.isEmpty()) {
			throw new IllegalStateException("Could not find railway nodes near from coordinate " + from);
		}
		Set<Long> destNodes = new HashSet<>(findNodesNear(to));
		if (destNodes.isEmpty()) {
			destNodes = new HashSet<>(addNodesOnTracksNear(to));
		}
		if (destNodes.isEmpty()) {
			throw new IllegalStateException("Could not find railway nodes near to coordinate " + to);
		}
		
		for (Long sourceNodeId : sourceNodes) {
			List<Long> pathNodes = getPath(sourceNodeId, destNodes);			
			if (pathNodes != null) {
				return pathNodes.stream().map(p -> nodes.get(p).getPosition()).collect(Collectors.toList());
			}
		}
		
		return null;
	}
	
	/**
	 * Uses Dijkstra algorithm to find the shortest path from the source node to one of the destination nodes. 
	 * @param sourceNodeId
	 * @param destNodes
	 * @return
	 */
	private List<Long> getPath(Long sourceNodeId, Set<Long> destNodes) {
		
		Map<Long, Double> dist = new HashMap<>();
		Map<Long, Long> prev = new HashMap<>();
		Set<Long> toVisit = new HashSet<>(); // Should be replaced by a priority queue for more performance
		Set<Long> visited = new HashSet<>();

		dist.put(sourceNodeId, 0.0);
		toVisit.add(sourceNodeId);
		
		boolean reachedDestination = false;
		Long destNodeId = null;
		while(!reachedDestination) {
			
			// Find the nearest (node with the smallest distance) unchecked node
			Long uId = null;
			Double uDist = Double.MAX_VALUE;
			for (Long nodeId : toVisit) {
				Double nodeDist = dist.get(nodeId);
				Validate.notNull(nodeDist, "All nodes in the \"toVisit\" set should have a distance, but distance not found");
				
				if (nodeDist < uDist) {
					uId = nodeId;
					uDist = nodeDist;
				}
			}
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
					Double curConnectionDistance = dist.get(cId);
					if (curConnectionDistance == null || curConnectionDistance > newConnectionDistance) {
						dist.put(cId, newConnectionDistance);
						prev.put(cId, uId);
					}
					
					if (!visited.contains(cId)) {
						toVisit.add(cId);
					}
				}
				
				toVisit.remove(uId);
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
	
	private List<Long> findNodesNear(LatLonCoordinate pos) {
		List<Long> preferredNodes = railwayMap.findNodesNear(pos, configService.getOpenStreetMapConfiguration().getPreferredStationToTrackDistance());
//		if (preferredNodes.isEmpty()) {
//			return railwayMap.findNodesNear(pos, configService.getOpenStreetMapConfiguration().getMaxStationToTrackDistance());
//		} else {
			return preferredNodes;
//		}
	}
	
	private List<Long> addNodesOnTracksNear(LatLonCoordinate pos) {
		return addNodesOnTracksNear(pos, configService.getOpenStreetMapConfiguration().getPreferredStationToTrackDistance());
	}
	
	/**
	 * Adds nodes on the tracks near the given position (to make navigation easier).
	 * Returns the IDs of the added nodes if any. If no IDs are returned, then
	 * no track were found near the given position within maxDist.
	 * 
	 * @param pos
	 * @param maxDist
	 * @return
	 */
	private List<Long> addNodesOnTracksNear(LatLonCoordinate pos, double maxDist) {
		
		double trackNodeDistance = railwayMap.getLongestNodeDist() + maxDist;
		List<Long> nearbyTrackNodes = railwayMap.findNodesNear(pos, trackNodeDistance);
		
		List<Triple<Long, Long, Long>> addedNodes = new ArrayList<>();
		
		// Find all paths that pass nearby pos.
		for (Long nodeId : nearbyTrackNodes) {
			OsmRailwayMapNode node = railwayMap.getNode(nodeId);
			for (Long neighborNodeId : node.getConnections().keySet()) {
				if (nodeId < neighborNodeId) { // To prevent calculating this twice for the same path
					OsmRailwayMapNode neighborNode = railwayMap.getNode(neighborNodeId);
					
					double trackDist = CoordinateUtil.crossTrackDist(node.getPosition(), neighborNode.getPosition(), pos);
					if (trackDist < maxDist) { // This track passes by close enough, so let's add a new node:
						
						long newNodeId = addNodeOnPath(node.getPosition(), neighborNode.getPosition(), pos);
						addedNodes.add(new ImmutableTriple<>(nodeId, neighborNodeId, newNodeId));
						
					}
					
				}
			}
		}
		
		// Fix the connections
		for (Triple<Long, Long, Long> addedNode : addedNodes) {
			
			Long node1Id = addedNode.getLeft();
			Long node2Id = addedNode.getMiddle();
			Long newNodeId = addedNode.getRight();
			
			OsmRailwayMapNode node1 = railwayMap.getNode(node1Id);
			OsmRailwayMapNode node2 = railwayMap.getNode(node2Id);
			
			node1.removeConnection(node2Id);
			node2.removeConnection(node1Id);
			
			railwayMap.addWay(Arrays.asList(node1Id, newNodeId, node2Id));
		}
		
		return addedNodes.stream().map(n -> n.getRight()).collect(Collectors.toList());
		
	}
	
	private Long addNodeOnPath(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate pos) {
		
		double alongTrack = CoordinateUtil.alongTrackDist(c1, c2, pos);
		double trackLength = CoordinateUtil.dist(c1, c2);
		
		LatLonCoordinate nodeOnPathPos = CoordinateUtil.interpolate(c1, c2, alongTrack / trackLength);
		long newNodeId = 0;
		// Find the first free node index
		while(railwayMap.getNode(newNodeId) != null) {
			newNodeId++; 
		}
		
		railwayMap.addNode(newNodeId, new OsmRailwayMapNode(nodeOnPathPos));
		
		return newNodeId;
	}

	
	private class OsmRailwayMapReader implements OsmHandler {

		private final OsmRailwayMap target;
		
		public OsmRailwayMapReader(OsmRailwayMap target) {
			this.target = target;
		}
		
		@Override
		public void complete() throws IOException {
			target.clean();
		}

		@Override
		public void handle(OsmBounds bounds) throws IOException {
			// We don't care about bounds for the train map
		}

		@Override
		public void handle(OsmNode node) throws IOException {
			target.addNode(node);
			
		}

		@Override
		public void handle(OsmWay way) throws IOException {
			
			boolean isRailway = false;
			for (int i = 0; i < way.getNumberOfTags(); i++) {
				OsmTag tag = way.getTag(i);
				if ("railway".equals(tag.getKey()) && "rail".equals(tag.getValue())) {
					isRailway = true;
					break;
				}
			}
			
			if (isRailway) {
				target.addWay(way);
			}
			
		}

		@Override
		public void handle(OsmRelation arg0) throws IOException {
			// Not important for railway map
		}
		
	}
}
