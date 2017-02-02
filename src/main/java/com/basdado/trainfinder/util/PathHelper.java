package com.basdado.trainfinder.util;

import java.util.List;

import com.basdado.trainfinder.model.LatLonCoordinate;

public class PathHelper {
	
	/**
	 * Returns the point at f% of the path.
	 * @param path A path (list of coordinates) along which the travel takes place. May not be null or empty.
	 * @param f fraction of the path that has been traveled.
	 * @return A point along the path at f% of the completed path.
	 */
	public static LatLonCoordinate getPointAt(List<LatLonCoordinate> path, double f) {
		
		if (path == null || path.isEmpty()) throw new IllegalArgumentException("The path may not be empty");
		if (path.size() == 1) return path.get(0);
		if (f <= 0) return path.get(0);
		if (f >= 1) return path.get(path.size() - 1); 
		
		final double pathLength = getLength(path);
		double traveled = pathLength * f;
		
		double t = 0;
		LatLonCoordinate u = null;
		for (LatLonCoordinate v : path) {
			if (u != null) {
				double partDist = CoordinateUtil.dist(u, v);
				if (t + partDist >= traveled) {
					return CoordinateUtil.interpolate(u, v, (traveled - t) / partDist);
				} else {
					t += partDist;
				}
			}
		}
		
		return path.get(path.size() - 1); 
		
	}
	
	public static double getLength(List<LatLonCoordinate> path) {
		double pathLength = 0;
		LatLonCoordinate v = null;
		for (LatLonCoordinate u : path) {
			if (v != null) {
				pathLength += CoordinateUtil.dist(u, v);
			}
			v = u;
		}
		return pathLength;
	}
}
