package com.origin.utils.voronoi;

public class Parabola {
	
	public static int IS_FOCUS = 0;
	public static int IS_VERTEX = 1;
	
	int type;
	Point point;
	Edge edge;
	Event event;
	
	Parabola parent;
	Parabola child_left;
	Parabola child_right;
	
	public Parabola () {
		type = IS_VERTEX;
	}
	
	public Parabola (Point p) {
		point = p;
		type = IS_FOCUS;
	}

	public void setLeftChild (Parabola p) {
		child_left = p;
		p.parent = this;
	}

	public void setRightChild (Parabola p) {
		child_right = p;
		p.parent = this;
	}

	public static Parabola getLeft(Parabola p) {
		return getLeftChild(getLeftParent(p));
	}
	
	public static Parabola getRight(Parabola p) {
		return getRightChild(getRightParent(p));
	}
	
	public static Parabola getLeftParent(Parabola p) {
		Parabola parent = p.parent;
		if (parent == null) return null;
		Parabola last = p;
		while (parent.child_left == last) {
			if(parent.parent == null) return null;
			last = parent;
			parent = parent.parent;
		}
		return parent;
	}
	
	public static Parabola getRightParent(Parabola p) {
		Parabola parent = p.parent;
		if (parent == null) return null;
		Parabola last = p;
		while (parent.child_right == last) {
			if(parent.parent == null) return null;
			last = parent;
			parent = parent.parent;
		}
		return parent;
	}
	
	public static Parabola getLeftChild(Parabola p) {
		if (p == null) return null;
		Parabola child = p.child_left;
		while(child.type == IS_VERTEX) child = child.child_right;
		return child;
	}
	
	public static Parabola getRightChild(Parabola p) {
		if (p == null) return null;
		Parabola child = p.child_right;
		while(child.type == IS_VERTEX) child = child.child_left;
		return child;	
	}
}
