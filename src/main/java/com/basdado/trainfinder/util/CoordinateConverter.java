package com.basdado.trainfinder.util;

import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.model.Vector3;

public class CoordinateConverter {
	
	public static final double RADIUS_EARTH = 6371008.8f;
	
	/**
	 * Converts a coordinate (as latitude and longitude) on Earth to a 3D vector (where the numbers are expressed in meters)
	 * @param coord A coordinate (latitude, longitude) on the Earth's surface
	 * @return A 3D vector representing the position on earth
	 */
	public static Vector3 toVector3f(LatLonCoordinate coord) {
		return toVector3f(coord, 0, RADIUS_EARTH);
	}
	
	/**
	 * Converts a coordinate (as latitude and longitude) on a planet with the given radius and altitude above the surface to a 3D vector.
	 * @param coord The latitude-longitude coordinate on the planet
	 * @param alt The altitude above the planet surface
	 * @param rad The radius of the planet
	 * @return A 3D coordinate representing the given position
	 */
	public static Vector3 toVector3f(LatLonCoordinate coord, double alt, double rad) {
		// see: http://www.mathworks.de/help/toolbox/aeroblks/llatoecefposition.html
		double f  = 0f;                                                           // flattening
		double ls = Math.atan((1 - f) * (1 - f) * Math.tan(coord.getLatitude())); // lambda
		
		double x = rad * Math.cos(ls) * Math.cos(coord.getLongitude()) + alt * Math.cos(coord.getLatitude()) * Math.cos(coord.getLongitude());
		double y = rad * Math.cos(ls) * Math.sin(coord.getLongitude()) + alt * Math.cos(coord.getLatitude()) * Math.sin(coord.getLongitude());
		double z = rad * Math.sin(ls) + alt * Math.sin(coord.getLatitude());
		
		return new Vector3(x, y, z);
	}
	
}
