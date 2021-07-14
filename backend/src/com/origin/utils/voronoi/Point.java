package com.origin.utils.voronoi;

public class Point implements Comparable <Point>{
	
	public double x;
	public double y;
	
	public Point (double x0, double y0) {
		x = x0;
		y = y0;
	}
	
	public int compareTo (Point other) {
		if (this.y == other.y) {
			if (this.x == other.x) return 0;
			else if (this.x > other.x) return 1;
			else return -1;
		}
		else if (this.y > other.y) {
			return 1;
		}
		else {
			return -1;
		}
	}
}
