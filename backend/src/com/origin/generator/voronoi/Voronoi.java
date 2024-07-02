package com.origin.generator.voronoi;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Voronoi {

    public List<Point> sites;
    public List<Edge> edges;
    PriorityQueue<Event> events;
    Parabola root;

    double width = 1;
    double height = 1;

    double ycurr;

    public Voronoi(List<Point> sites) {
        this.sites = sites;
        edges = new ArrayList<>();
        generateVoronoi();
    }

    private void generateVoronoi() {

        events = new PriorityQueue<>();
        for (Point p : sites) {
            events.add(new Event(p, Event.SITE_EVENT));
        }

        while (!events.isEmpty()) {
            Event e = events.remove();
            ycurr = e.p.y;
            if (e.type == Event.SITE_EVENT) {
                handleSite(e.p);
            } else {
                handleCircle(e);
            }
        }

        ycurr = width + height;

        endEdges(root);

        for (Edge e : edges) {
            if (e.neighbor != null) {
                e.start = e.neighbor.end;
                e.neighbor = null;
            }
        }
    }

    private void endEdges(Parabola p) {
        if (p.type == Parabola.IS_FOCUS) {
//            p = null;
            return;
        }

        double x = getXofEdge(p);
        p.edge.end = new Point(x, p.edge.slope * x + p.edge.yint);
        edges.add(p.edge);

        endEdges(p.child_left);
        endEdges(p.child_right);

//        p = null;
    }

    private void handleSite(Point p) {

        if (root == null) {
            root = new Parabola(p);
            return;
        }

        Parabola par = getParabolaByX(p.x);
        if (par.event != null) {
            events.remove(par.event);
            par.event = null;
        }

        Point start = new Point(p.x, getY(par.point, p.x));
        Edge el = new Edge(start, par.point, p);
        Edge er = new Edge(start, p, par.point);
        el.neighbor = er;
        er.neighbor = el;
        par.edge = el;
        par.type = Parabola.IS_VERTEX;

        Parabola p0 = new Parabola(par.point);
        Parabola p1 = new Parabola(p);
        Parabola p2 = new Parabola(par.point);

        par.setLeftChild(p0);
        par.setRightChild(new Parabola());
        par.child_right.edge = er;
        par.child_right.setLeftChild(p1);
        par.child_right.setRightChild(p2);

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void handleCircle(Event e) {

        Parabola p1 = e.arc;
        Parabola xl = Parabola.getLeftParent(p1);
        Parabola xr = Parabola.getRightParent(p1);
        Parabola p0 = Parabola.getLeftChild(xl);
        Parabola p2 = Parabola.getRightChild(xr);

        assert p0 != null;
        if (p0.event != null) {
            events.remove(p0.event);
            p0.event = null;
        }
        assert p2 != null;
        if (p2.event != null) {
            events.remove(p2.event);
            p2.event = null;
        }

        Point p = new Point(e.p.x, getY(p1.point, e.p.x));

        xl.edge.end = p;
        xr.edge.end = p;
        edges.add(xl.edge);
        edges.add(xr.edge);

        Parabola higher = new Parabola();
        Parabola par = p1;
        while (par != root) {
            par = par.parent;
            if (par == xl) higher = xl;
            if (par == xr) higher = xr;
        }
        higher.edge = new Edge(p, p0.point, p2.point);

        Parabola gparent = p1.parent.parent;
        if (p1.parent.child_left == p1) {
            if (gparent.child_left == p1.parent) gparent.setLeftChild(p1.parent.child_right);
            if (gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_right);
        } else {
            if (gparent.child_left == p1.parent) gparent.setLeftChild(p1.parent.child_left);
            if (gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_left);
        }

//        Point op = p1.point;
        p1.parent = null;
//        p1 = null;

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    private void checkCircleEvent(Parabola b) {

        Parabola lp = Parabola.getLeftParent(b);
        Parabola rp = Parabola.getRightParent(b);

        if (lp == null || rp == null) return;

        Parabola a = Parabola.getLeftChild(lp);
        Parabola c = Parabola.getRightChild(rp);

        if (a == null || c == null || a.point == c.point) return;

        if (ccw(a.point, b.point, c.point) != 1) return;

        Point start = getEdgeIntersection(lp.edge, rp.edge);
        if (start == null) return;

        double dx = b.point.x - start.x;
        double dy = b.point.y - start.y;
        double d = Math.sqrt((dx * dx) + (dy * dy));
        if (start.y + d < ycurr) return;

        Point ep = new Point(start.x, start.y + d);
        Event e = new Event(ep, Event.CIRCLE_EVENT);
        e.arc = b;
        b.event = e;
        events.add(e);
    }

    public int ccw(Point a, Point b, Point c) {
        double area2 = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return 0;
    }

    private Point getEdgeIntersection(Edge a, Edge b) {

        if (b.slope == a.slope && b.yint != a.yint) return null;

        double x = (b.yint - a.yint) / (a.slope - b.slope);
        double y = a.slope * x + a.yint;

        return new Point(x, y);
    }

    private double getXofEdge(Parabola par) {
        Parabola left = Parabola.getLeftChild(par);
        Parabola right = Parabola.getRightChild(par);

        Point p = left.point;
        Point r = right.point;

        double dp = 2 * (p.y - ycurr);
        double a1 = 1 / dp;
        double b1 = -2 * p.x / dp;
        double c1 = (p.x * p.x + p.y * p.y - ycurr * ycurr) / dp;

        double dp2 = 2 * (r.y - ycurr);
        double a2 = 1 / dp2;
        double b2 = -2 * r.x / dp2;
        double c2 = (r.x * r.x + r.y * r.y - ycurr * ycurr) / dp2;

        double a = a1 - a2;
        double b = b1 - b2;
        double c = c1 - c2;

        double disc = b * b - 4 * a * c;
        double x1 = (-b + Math.sqrt(disc)) / (2 * a);
        double x2 = (-b - Math.sqrt(disc)) / (2 * a);

        double ry;
        if (p.y > r.y) ry = Math.max(x1, x2);
        else ry = Math.min(x1, x2);

        return ry;
    }

    private Parabola getParabolaByX(double xx) {
        Parabola par = root;
        double x;
        while (par.type == Parabola.IS_VERTEX) {
            x = getXofEdge(par);
            if (x > xx) par = par.child_left;
            else par = par.child_right;
        }
        return par;
    }

    private double getY(Point p, double x) {
        double dp = 2 * (p.y - ycurr);
        double a1 = 1 / dp;
        double b1 = -2 * p.x / dp;
        double c1 = (p.x * p.x + p.y * p.y - ycurr * ycurr) / dp;
        return (a1 * x * x + b1 * x + c1);
    }
}
