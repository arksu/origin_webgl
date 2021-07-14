package com.origin.utils.voronoi;

public class Event implements Comparable <Event>{
	
	public static int SITE_EVENT = 0;
	public static int CIRCLE_EVENT = 1;
	
	Point p;
	int type;
	Parabola arc;
	
	public Event (Point p, int type) {
		this.p = p;
		this.type = type;
		arc = null;
	}
	
	public int compareTo(Event other) {
		return this.p.compareTo(other.p);
	}

}
