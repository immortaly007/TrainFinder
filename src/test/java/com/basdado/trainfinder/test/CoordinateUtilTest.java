package com.basdado.trainfinder.test;

import org.junit.Assert;
import org.junit.Test;

import com.basdado.trainfinder.model.LatLng;
import com.basdado.trainfinder.util.CoordinateUtil;

public class CoordinateUtilTest {
	
	
	@Test
	public void testInterpolate() {
		
		LatLng c1 = new LatLng(10,  5);
		LatLng c2 = new LatLng(5, 10);
		
		LatLng midPoint = CoordinateUtil.interpolate(c1, c2, 0.5);
		
		Assert.assertEquals(7.507063, midPoint.getLatitude(), 0.001);
		Assert.assertEquals(7.514379, midPoint.getLongitude(), 0.001);
		
	}
	
	@Test 
	public void testDistance() {
		
		LatLng c1 = new LatLng(10,  5);
		LatLng c2 = new LatLng(5, 10);
		
		double dist = CoordinateUtil.dist(c1, c2);
		
		Assert.assertEquals(782779.0829048026, dist, 10.0);
		
	}
	
	@Test
	public void testDistance2() {
		
		LatLng c1 = new LatLng(80.0001, -10.0001);
		LatLng c2 = new LatLng(63.302, 7.935);
		
		double dist = CoordinateUtil.dist(c1, c2);

        Assert.assertEquals(1939037.0, dist, 10.0);
	}
	
	@Test 
	public void testBearing() { 
		
		LatLng c1 = new LatLng(80.0001, -10.0001);
		LatLng c2 = new LatLng(63.302, 7.935);

        double b = CoordinateUtil.bearing(c1, c2);
        Assert.assertEquals(2.661709, b, 0.00001);

	}

	@Test 
	public void testCrossTrackDistanceNearest() {
		
		LatLng c1 = new LatLng(1.0, 1.0);
		LatLng c2 = new LatLng(2.0, 1.0);
		LatLng c3 = new LatLng(0.0, 0.0);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c1, c3), d, 1.0);
	}
	

	@Test 
	public void testCrossTrackDistanceMiddle() {
		
		LatLng c1 = new LatLng(0, 0);
		LatLng c2 = new LatLng(90, 90);
		LatLng c3 = new LatLng(80, 80);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        // TODO: Maybe being 10/1000 km (1%) off from some online service is a bit much?
        Assert.assertEquals(1098667, d, 10000.0);
	}
	
	
	@Test 
	public void testCrossTrackDistanceFurthest() {
		
		LatLng c1 = new LatLng(1.0, 1.0);
		LatLng c2 = new LatLng(2.0, 1.0);
		LatLng c3 = new LatLng(3.0, 0.0);
		
        double d = CoordinateUtil.crossTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c2, c3), d, 1.0);
	}
	
	@Test 
	public void testAlongTrackDistanceClosest() {
		
		LatLng c1 = new LatLng(1.0, 1.0);
		LatLng c2 = new LatLng(2.0, 1.0);
		LatLng c3 = new LatLng(0.0, 0.0);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(0, d, 1.0);
	}
	
	@Test 
	public void testAlongTrackDistanceMiddle() {
		
		LatLng c1 = new LatLng(0, 0);
		LatLng c2 = new LatLng(60, 60);
		LatLng c3 = new LatLng(40, 50);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(6620169, d, 1000.0);
	}
	
	@Test 
	public void testAlongTrackDistanceFurthest() {
		
		LatLng c1 = new LatLng(1.0, 1.0);
		LatLng c2 = new LatLng(2.0, 1.0);
		LatLng c3 = new LatLng(3.0, 1.0);
		
        double d = CoordinateUtil.alongTrackDist(c1, c2, c3);
        
        Assert.assertEquals(CoordinateUtil.dist(c1, c2), d, 1.0);
	}

}
