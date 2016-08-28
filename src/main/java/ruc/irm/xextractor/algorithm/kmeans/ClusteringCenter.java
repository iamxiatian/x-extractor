package ruc.irm.xextractor.algorithm.kmeans;

import java.util.ArrayList;

class ClusteringCenter extends Point {
	private ArrayList<Integer> clusterPoints;
	private double[] sumOfPoints;
    private KMeansClusterer clusterer;

	ClusteringCenter(KMeansClusterer clusterer, Point p){
		super(p.features);

        this.clusterer = clusterer;
		clusterPoints = new ArrayList<Integer>();
		this.sumOfPoints = new double[this.dimension];
	}
	void addPointToCluster(int index){
		Point p = clusterer.instance(index);
		clusterPoints.add(index);
		double[] po = p.getFeatures();
		for(int i = 0; i < this.dimension; ++i){
			sumOfPoints[i] += po[i];
		}
	}
	
	ClusteringCenter getNewCenter(){
		double[] pos = new double[clusterer.getDimension()];
		for(int i = 0; i < this.dimension; ++i){
			pos[i] = sumOfPoints[i] / this.clusterPoints.size();
		}
		return new ClusteringCenter(clusterer, new Point(pos));
	}
	
	double evaluate(){
		double ret = 0.0;
		for(int in : clusterPoints){
			ret += Point.squareDistance(clusterer.instance(in), this);
		}
		return ret;
	}
	ArrayList<Integer> belongedPoints(){
		return new ArrayList<Integer>(this.clusterPoints);
	}
}
