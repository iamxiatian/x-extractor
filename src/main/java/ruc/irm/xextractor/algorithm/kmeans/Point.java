package ruc.irm.xextractor.algorithm.kmeans;

import java.util.ArrayList;

class Point {
	protected double[] features;
	protected int dimension;

	public Point(int size){
		features = new double[size];
		this.dimension = size;
	}
	public Point(double[] p){
		this.features = p;
		this.dimension = features.length;
	}
	int getDimension(){
		return this.dimension;
	}
	
	double[] getFeatures(){
		return features.clone();
	}
	
	public static double euclideanDistance(Point p1, Point p2){
		if(p1.features.length != p2.features.length)
			return -1.0;
		double[] p = new double[p1.features.length];
		for(int i = 0; i < p1.features.length; ++i)
			p[i] = p1.features[i] - p2.features[i];
		double sum = 0.0;
		for(int i = 0; i < p1.features.length; ++i){
			sum += Math.pow(p[i], 2.0);
		}
		return Math.sqrt(sum);
	}
	
	public static double squareDistance(Point p1, Point p2){
		if(p1.features.length != p2.features.length)
			return -1.0;
		double[] p = new double[p1.features.length];
		for(int i = 0; i < p1.features.length; ++i)
			p[i] = p1.features[i] - p2.features[i];
		double sum = 0.0;
		for(int i = 0; i < p1.features.length; ++i){
			sum += Math.pow(p[i], 2.0);
		}
		return sum;
	}
	
	public boolean equals(Object o){
		Point p = (Point)o;
		if(this.dimension != p.dimension)
			return false;
		for(int i = 0; i < this.dimension; i++)
			if(this.features[i] != p.features[i])
				return false;
		return true;
	}
	public static void main(String[] args){
		double[] a = {1.0, 2.0, 3.0, 4.0}, b = {2.0, 3.0, 4.0, 2.0};
		Point p1 = new Point(a);
        Point p2 = new Point(b);
        Point p3 = new Point(a);
		ArrayList<Point> list = new ArrayList<Point>();
		list.add(p1);
		list.add(p2);
		System.out.println(list.contains(p3));
	}
}
