package com.basdado.trainfinder.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Vector3 {
	
	private final double x;
	private final double y;
	private final double z;
	
	public Vector3() {
		this.x = this.y = this.z = 0;
	}
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
	
	public Vector3 dot(Vector3 v) {
		return new Vector3(this.x * v.x, this.y * v.y, this.z * v.z);
	}
	
	public Vector3 add(Vector3 v) {
		return new Vector3(this.x + v.x, this.y + v.y, this.z + v.z);
	}
	
	public Vector3 sub(Vector3 v) {
		return new Vector3(this.x - v.x, this.y - v.y, this.z - v.z);
	}
	
	public double magnitude() {
		return (double)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null || !(obj instanceof Vector3)) return false;
		if (this == obj) return true;
		
		Vector3 v = (Vector3)obj;
		return this.x == v.x && this.y == v.y && this.z == v.z;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(x)
				.append(y)
				.append(z)
				.build();
	}
}
