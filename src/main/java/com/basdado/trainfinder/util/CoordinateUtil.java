package com.basdado.trainfinder.util;

import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Vector3;

public class CoordinateUtil {
	
	public static final double MIN_LONGITUDE = -180;
	public static final double MAX_LONGITUDE = 180;
	public static final double MIN_LATITUDE = -90;
	public static final double MAX_LATITUDE = 90;
	
	public static final double RADIUS_EARTH = 6371008.8f;
	public static final double HALF_PI = Math.PI * 0.5;
	
	/**
	 * Converts a coordinate (as latitude and longitude) on Earth to a 3D vector (where the numbers are expressed in meters)
	 * @param coord A coordinate (latitude, longitude) on the Earth's surface
	 * @return A 3D vector representing the position on earth
	 */
	public static Vector3 toVector3(LatLonCoordinate coord) {
		return toVector3(coord, 0, RADIUS_EARTH);
	}
	
	/**
	 * Converts a coordinate (as latitude and longitude) on a planet with the given radius and altitude above the surface to a 3D vector.
	 * @param coord The latitude-longitude coordinate on the planet
	 * @param alt The altitude above the planet surface
	 * @param rad The radius of the planet
	 * @return A 3D coordinate representing the given position
	 */
	public static Vector3 toVector3(LatLonCoordinate coord, double alt, double rad) {
		// see: http://www.mathworks.de/help/toolbox/aeroblks/llatoecefposition.html
		double lat = Math.toRadians(coord.getLatitude());
		double lon = Math.toRadians(coord.getLongitude());
		
		double f  = 0f;                                               // flattening
		double ls = Math.atan((1.0 - f) * (1.0 - f) * Math.tan(lat)); // lambda
		
		double x = rad * Math.cos(ls) * Math.cos(lon) + alt * Math.cos(lat) * Math.cos(lon);
		double y = rad * Math.cos(ls) * Math.sin(lon) + alt * Math.cos(lat) * Math.sin(lon);
		double z = rad * Math.sin(ls) + alt * Math.sin(lat);
		
		return new Vector3(x, y, z);
	}
	
	/**
	 * Linearly interpolates the coordinate c1 towards c2 over the great circle path (orthodrome),
	 * with a fraction of f.
	 * @param c1 The first coordinate (start)
	 * @param c2 The second coordinate (end)
	 * @param f The fraction traveled between c1 and c2.
	 * @return The coordinate that lies at f% between c1 and c2 when traveling the shortest surface path 
	 * between those points (great circle path or orthodrome).
	 */
	public static LatLonCoordinate interpolate(LatLonCoordinate c1, LatLonCoordinate c2, double f) {
		
		// See: http://www.movable-type.co.uk/scripts/latlong.html
		double delta = angularDist(c1, c2);
		double a = Math.sin((1.0 - f)* delta) / Math.sin(delta);
		double b = Math.sin(f*delta) / Math.sin(delta);
		
	    double lat1 = Math.toRadians(c1.getLatitude());
	    double lon1 = Math.toRadians(c1.getLongitude());
	    double lat2 = Math.toRadians(c2.getLatitude());
	    double lon2 = Math.toRadians(c2.getLongitude());
		
		
		double x = a * Math.cos(lat1) * Math.cos(lon1) + b * Math.cos(lat2) * Math.cos(lon2);
		double y = a * Math.cos(lat1) * Math.sin(lon1) + b * Math.cos(lat2) * Math.sin(lon2);
		double z = a * Math.sin(lat1) + b * Math.sin(lat2);
		double lati = Math.atan2(z, Math.sqrt(x * x + y * y));
		double loni = Math.atan2(y, x);
		
		return new LatLonCoordinate(Math.toDegrees(lati), Math.toDegrees(loni));
	}
	
	/**
	 * The angular distance (Radians traveled) between c1 and c2.
	 * @param c1 The first coordinate (start)
	 * @param c2 The second coordinate (end)
	 * @return The angular distance between c1 and c2.
	 */
	public static double angularDist(LatLonCoordinate c1, LatLonCoordinate c2) {
		
	    double dLat = Math.toRadians(c2.getLatitude()-c1.getLatitude());
	    double dLng = Math.toRadians(c2.getLongitude()-c1.getLongitude());
	    
	    double sinLatDiv2 = Math.sin(dLat/2);
	    double sinLngDiv2 = Math.sin(dLng/2);
	    
	    double a = sinLatDiv2 * sinLatDiv2 +
	               Math.cos(Math.toRadians(c1.getLatitude())) * Math.cos(Math.toRadians(c2.getLatitude())) *
	               sinLngDiv2 * sinLngDiv2;
	    return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}
	
	/**
	 * Calculates the bearing (forward azimuth) which when followed in a straight line
	 * (along a great circle) takes you from c1 to c2.
	 * @param c1 The first coordinate (start)
	 * @param c2 The second coordinate (finish)
	 * @return The bearing (in radians)
	 */
	public static double bearing(LatLonCoordinate c1, LatLonCoordinate c2) {
		
		double c1lat = Math.toRadians(c1.getLatitude());
		double c1lon = Math.toRadians(c1.getLongitude());
		double c2lat = Math.toRadians(c2.getLatitude());
		double c2lon = Math.toRadians(c2.getLongitude());
		
		double y = Math.sin(c2lon-c1lon) * Math.cos(c2lat);
		double x = Math.cos(c1lat) * Math.sin(c2lat) -
		           Math.sin(c1lat) * Math.cos(c2lat) * Math.cos(c2lon-c1lon);
		return Math.atan2(y, x);
	}
	
	/**
	 * Calculates the angular distance (radians) of point c3 to the path (orthodrome) between c1 and c2. 
	 * Also known as the cross-track distance.
	 * @param c1 The start of the path
	 * @param c2 The end of the track
	 * @param c3 The point for which the distance to the path is needed
	 * @return 
	 */
	public static double angularCrossTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3) {
		double d13 = angularDist(c1, c3);
		double b13 = bearing(c1, c3);
		double b12 = bearing(c1, c2);
		double b23 = bearing(c2, c3);
		
		// Check if c3 is on a right angle to the path between c1 and c2, otherwise, return distance from c1. 
		if (!similarDirection(b12, b13)) {
			return angularDist(c1, c3);
		}
		if (similarDirection(b23, b12)) {
			return angularDist(c2, c3);
		}
		
		return Math.asin(Math.sin(d13) * Math.sin(b13-b12));
	}
	
	/**
	 * Returns true if the two bearings are in a "similar" direction,
	 * i.e. at most b1 is at most 90 degrees different from b2
	 * @param b1 Bearing 1, should be between 0 and 2 PI
	 * @param b2 Bearing 2, should be between 0 and 2 PI
	 * @return True iff b1 and b2 are within 90 degrees of each other
	 */
	private static boolean similarDirection(double b1, double b2) {
		return Math.min((2 * Math.PI) - Math.abs(b1 - b2), Math.abs(b1 - b2)) < HALF_PI;
	}
	
	/**
	 * Calculates the distance of point c3 to the path (orthodrome) between c1 and c2. Sometimes
	 * referred to as the cross-track distance.
	 * @param c1 The start of the path
	 * @param c2 The end of the path
	 * @param c3 The point for which the distance to the path is needed
	 * @param R The radius of the object along which the distance should be calculated
	 * @return The distance (great circle) between c3 and the path that spans between c1 and c2.
	 */
	public static double crossTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3, double R) {
		
		return angularCrossTrackDist(c1, c2, c3) * R;

	}
	
	/**
	 * Same as {@link #crossTrackDist(LatLonCoordinate, LatLonCoordinate, LatLonCoordinate, double)}, with R = Earths radius.
	 */
	public static double crossTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3) {
		return crossTrackDist(c1, c2, c3, RADIUS_EARTH);
	}
	
	/**
	 * @param c1 The start of the track
	 * @param c2 The end of the path
	 * @param c3 The point for which the closest point on the path between c1 and c2 should be found.
	 * @return The angular distance between c1 and the point on the path between c1 and c2 that lies closest to c3.
	 */
	public static double angularAlongTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3) {
		
		if (!similarDirection(bearing(c2, c1), bearing(c2, c3))) {
			return angularDist(c1, c2);
		}
		
		double d13 = angularDist(c1, c3);
		double dXt = angularCrossTrackDist(c1, c2, c3);
		
		return Math.acos(Math.cos(d13)/Math.cos(dXt));

	}
	
	/**
	 * @param c1 The start of the track
	 * @param c2 The end of the path
	 * @param c3 The point for which the closest point on the path between c1 and c2 should be found.
	 * @param 
	 * @return The distance between c1 and the point on the path between c1 and c2 that lies closest to c3.
	 */
	public static double alongTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3, double R) {
		return angularAlongTrackDist(c1, c2, c3) * R;
	}
	
	/**
	 * @param c1 The start of the track
	 * @param c2 The end of the path
	 * @param c3 The point for which the closest point on the path between c1 and c2 should be found.
	 * @return The distance between c1 and the point on the path between c1 and c2 that lies closest to c3.
	 */
	public static double alongTrackDist(LatLonCoordinate c1, LatLonCoordinate c2, LatLonCoordinate c3) {
		return alongTrackDist(c1, c2, c3, RADIUS_EARTH);
	}
	
	/**
	 * @param c1 The first coordinate
	 * @param c2 The second coordinate
	 * @return The distance (in meters) between point c1 and c2.
	 */
	public static double dist(LatLonCoordinate c1, LatLonCoordinate c2) {

	    return RADIUS_EARTH * angularDist(c1, c2);
	}
}
