package com.basdado.trainfinder.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basdado.trainfinder.config.ConfigService;
import com.basdado.trainfinder.config.OpenStreetMapConfiguration;
import com.basdado.trainfinder.exception.PathFindingException;
import com.basdado.trainfinder.model.LatLng;
import com.basdado.trainfinder.model.OsmRailwayMap;
import com.basdado.trainfinder.model.OsmRailwayMap.OsmRailwayMapNode;
import com.basdado.trainfinder.model.Railway;
import com.basdado.trainfinder.model.Station;
import com.basdado.trainfinder.util.CoordinateUtil;
import com.basdado.trainfinder.util.RailwayMapUtil;

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
	
	private static final String RAILWAY_CACHE_KEY = "railwayPathCache";
	
	private static final Logger logger = LoggerFactory.getLogger(TrainRoutingController.class);
	
	@Inject private ConfigService configService;
	@Inject private CacheManager cacheManager;
	
	private OsmRailwayMap railwayMap;
	private Map<String, Boolean> nodesAddedNearStation;
	
	private Cache<String, Railway> railwayCache;
	
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
		
		railwayCache = (Cache<String, Railway>)
				cacheManager.getCache(RAILWAY_CACHE_KEY, String.class, Railway.class);
	}
	
	public Railway getRailway(Station from, Station to) throws PathFindingException {
		
		if (nodesAddedNearStation.get(from.getCode()) == null || !nodesAddedNearStation.get(from.getCode())) {
			addNodesOnTracksNear(from.getLocation());
			nodesAddedNearStation.put(from.getCode(), true);
		}
		
		if (nodesAddedNearStation.get(to.getCode()) == null || !nodesAddedNearStation.get(to.getCode())) {
			addNodesOnTracksNear(to.getLocation());
			nodesAddedNearStation.put(to.getCode(), true);
		}
		
		Railway res = railwayCache.get(getCacheKey(from, to));
		if (res == null) {
			// Try to get it from the inverse path:
			Railway inverseRes = railwayCache.get(getCacheKey(to, from));
			if (inverseRes != null) {
				res = inverseRes.reversed();
			}
		}
		if (res == null) {
			// Actually calculate it
			logger.info("Calculating path from station " + from.getShortName() + "(" + from.getCode() + ") to " + to.getShortName() + "(" + to.getCode() + ").");
			List<Long> pathNodes = calculateShortestPathBetween(from.getLocation(), to.getLocation());
			if (pathNodes != null) {
				res = generateRailway(pathNodes, from, to);
				railwayCache.put(getCacheKey(from, to), res);
			}
		}
		
		return res;
	}
	
	private String getCacheKey(Station from, Station to) {
		return from.getCode() + "-" + to.getCode();
	}
	
	private List<Long> calculateShortestPathBetween(LatLng from, LatLng to) throws PathFindingException {
		
		List<Long> sourceNodes = findNodesNear(from);
		if (sourceNodes.isEmpty()) {
			sourceNodes = addNodesOnTracksNear(from);
		}
		if (sourceNodes.isEmpty()) {
			throw new PathFindingException("Could not find railway nodes near from coordinate " + from);
		}
		List<Long> destNodes = findNodesNear(to);
		if (destNodes.isEmpty()) {
			destNodes = addNodesOnTracksNear(to);
		}
		if (destNodes.isEmpty()) {
			throw new PathFindingException("Could not find railway nodes near to coordinate " + to);
		}
		
		// First try to find a path directly from the closest node to "from" to the closest node to "to".
		List<Long> pathNodes = RailwayMapUtil.calculateShortestPathBetween(railwayMap, sourceNodes.get(0), new HashSet<>(Arrays.asList(destNodes.get(0))));
		if (pathNodes != null && !pathNodes.isEmpty()) {
			return pathNodes;
		}
		
		// Otherwise, try any combination of nodes
		for (Long sourceNodeId : sourceNodes) {

			pathNodes = RailwayMapUtil.calculateShortestPathBetween(railwayMap, sourceNodeId, new HashSet<>(Arrays.asList(destNodes.get(0))));
			if (pathNodes != null && !pathNodes.isEmpty()) {
				return pathNodes;
			}
		}
		
		throw new PathFindingException("Could not find a path from " + from + " to " + to);
	}
	
	private Railway generateRailway(List<Long> pathNodes, Station from, Station to) {
		
		LatLng[] path = new LatLng[pathNodes.size()];
		double[] distanceUntil = new double[pathNodes.size()];
		Long previousPathNodeId = null;
		OsmRailwayMapNode previousPathNode = null;
		int i = 0;
		
		for (Long pathNodeId : pathNodes) {
			
			OsmRailwayMapNode pathNode = railwayMap.getNode(pathNodeId);
			path[i] = pathNode.getPosition();
			if (i == 0) {
				distanceUntil[i] = 0;
			} else {
				double distanceToPreviousNode = pathNode.isConnectedTo(previousPathNodeId) ?
						pathNode.getDistanceToConnectedNode(previousPathNodeId) :
						CoordinateUtil.dist(pathNode.getPosition(), previousPathNode.getPosition());
				
				distanceUntil[i] = distanceUntil[i - 1] + distanceToPreviousNode;
			}
			
			previousPathNodeId = pathNodeId;
			previousPathNode = pathNode;
			i++;
		}
		
		return new Railway(path, distanceUntil, from, to);
	}
	
	

	
	private List<Long> findNodesNear(LatLng pos) {
		List<Long> preferredNodes = railwayMap.findNodesNear(pos, configService.getOpenStreetMapConfiguration().getPreferredStationToTrackDistance());
//		if (preferredNodes.isEmpty()) {
//			return railwayMap.findNodesNear(pos, configService.getOpenStreetMapConfiguration().getMaxStationToTrackDistance());
//		} else {
			return preferredNodes;
//		}
	}
	
	private List<Long> addNodesOnTracksNear(LatLng pos) {
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
	private List<Long> addNodesOnTracksNear(LatLng pos, double maxDist) {
		
		double trackNodeDistance = railwayMap.getLongestNodeConnectionDistance() + maxDist;
		List<Long> nearbyTrackNodes = railwayMap.findNodesNear(pos, trackNodeDistance);
		
		// Triple: left = existing node 1, middle = existing node 2, right = new node. 
		List<Triple<Long, Long, Long>> addedNodes = new ArrayList<>();
		
		// Find all paths (lines) that pass nearby pos.
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
			
			railwayMap.removeConnection(node1Id, node2Id);
			railwayMap.addWay(Arrays.asList(node1Id, newNodeId, node2Id));
		}
		
		return addedNodes.stream().map(n -> n.getRight())
				.sorted(Comparator.comparing(a -> CoordinateUtil.angularDist(railwayMap.getNode(a).getPosition(), pos)))
				.collect(Collectors.toList());
		
	}
	
	private Long addNodeOnPath(LatLng c1, LatLng c2, LatLng pos) {
		
		double alongTrack = CoordinateUtil.alongTrackDist(c1, c2, pos);
		double trackLength = CoordinateUtil.dist(c1, c2);
		
		LatLng nodeOnPathPos = CoordinateUtil.interpolate(c1, c2, alongTrack / trackLength);
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
