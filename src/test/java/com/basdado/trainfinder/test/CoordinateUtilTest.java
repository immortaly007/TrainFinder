package com.basdado.trainfinder.test;

import org.junit.Assert;
import org.junit.Test;

import com.basdado.trainfinder.model.LatLonCoordinate;
import com.basdado.trainfinder.util.CoordinateUtil;

public class CoordinateUtilTest {
	
	
	@Test
	public void testInterpolate() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(10,  5);
		LatLonCoordinate c2 = new LatLonCoordinate(5, 10);
		
		LatLonCoordinate midPoint = CoordinateUtil.interpolate(c1, c2, 0.5);
		
		Assert.assertEquals(7.507063, midPoint.getLatitude(), 0.001);
		Assert.assertEquals(7.514379, midPoint.getLongitude(), 0.001);
		
	}
	
	@Test 
	public void testDistance() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(10,  5);
		LatLonCoordinate c2 = new LatLonCoordinate(5, 10);
		
		double dist = CoordinateUtil.dist(c1, c2);
		
		Assert.assertEquals(782779.0829048026, dist, 10.0);
		
	}
	
	@Test
	public void testDistance2() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(80.0001, -10.0001);
		LatLonCoordinate c2 = new LatLonCoordinate(63.302, 7.935);
		
		double dist = CoordinateUtil.dist(c1, c2);

        Assert.assertEquals(1939037.0, dist, 10.0);
	}
	
	@Test 
	public void testBearing() { 
		
		LatLonCoordinate c1 = new LatLonCoordinate(80.0001, -10.0001);
		LatLonCoordinate c2 = new LatLonCoordinate(63.302, 7.935);

        double b = CoordinateUtil.bearing(c1, c2);
        Assert.assertEquals(2.661709, b, 0.00001);

	}

	@Test 
	public void testCrossTrackDistanceNearest() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(1.0, 1.0);
		LatLonCoordinate c2 = new LatLonCoordinate(2.0, 1.0);
		LatLonCoordinate c3 = new LatLonCoordinate(0.0, 0.0);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c1, c3), d, 1.0);
	}
	

	@Test 
	public void testCrossTrackDistanceMiddle() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(0, 0);
		LatLonCoordinate c2 = new LatLonCoordinate(90, 90);
		LatLonCoordinate c3 = new LatLonCoordinate(80, 80);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        // TODO: Maybe being 10/1000 km (1%) off from some online service is a bit much?
        Assert.assertEquals(1098667, d, 10000.0);
	}
	
	
	@Test 
	public void testCrossTrackDistanceFurthest() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(1.0, 1.0);
		LatLonCoordinate c2 = new LatLonCoordinate(2.0, 1.0);
		LatLonCoordinate c3 = new LatLonCoordinate(3.0, 0.0);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c2, c3), d, 1.0);
	}
	
	@Test 
	public void testAlongTrackDistanceClosest() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(1.0, 1.0);
		LatLonCoordinate c2 = new LatLonCoordinate(2.0, 1.0);
		LatLonCoordinate c3 = new LatLonCoordinate(0.0, 0.0);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(0, d, 1.0);
	}
	
	@Test 
	public void testAlongTrackDistanceMiddle() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(0, 0);
		LatLonCoordinate c2 = new LatLonCoordinate(60, 60);
		LatLonCoordinate c3 = new LatLonCoordinate(40, 50);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(6620169, d, 1000.0);
	}
	
	@Test 
	public void testAlongTrackDistanceFurthest() {
		
		LatLonCoordinate c1 = new LatLonCoordinate(1.0, 1.0);
		LatLonCoordinate c2 = new LatLonCoordinate(2.0, 1.0);
		LatLonCoordinate c3 = new LatLonCoordinate(3.0, 1.0);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c1, c2), d, 1.0);
	}

}
