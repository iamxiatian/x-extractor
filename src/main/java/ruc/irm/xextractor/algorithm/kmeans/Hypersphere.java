package ruc.irm.xextractor.algorithm.kmeans;

import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

class Hypersphere extends Point {
    private KMeansClusterer clusterer = null;

    /** 当前超球面的半径 */
	private double radius;
	private LinkedList<Integer> instances;
	private Hypersphere[] children = null;
	private double[] sumOfPoints;
	static int COUNT = 0, ALL_COUNT = 0;
	
	Hypersphere(KMeansClusterer clusterer){
		super(new double[clusterer.getDimension()]);

        this.clusterer = clusterer;
		instances = new LinkedList<Integer>();
		sumOfPoints = new double[clusterer.getDimension()];
	}
	
	void addInstance(int index){
		instances.add(index);
		double[] features = clusterer.instance(index).getFeatures();
		for(int i = 0; i < clusterer.getDimension(); i++){
			sumOfPoints[i] += features[i];
		}
	}
	
	void endAdding(){
		int size = instances.size();
		for(int i = 0; i < clusterer.getDimension(); i++){
			this.features[i] = this.sumOfPoints[i] / size;
		}
		this.radius = Point.euclideanDistance(this, clusterer.instance(this.getFarthestPoint(this)));
	}
	
	int size(){
		return instances.size();
	}

    /**
     * 计算点p在该簇中与所有点的距离的最大值：即为该点到聚类中心的距离+半径
     */
	double maxDistance(Point p){
		return radius + Point.euclideanDistance(p, this);
	}

    /**
     * 计算点p在该簇中与所有点的距离的最小值：即为该点到聚类中心的距离-半径
     */
	double minDistance(Point p){
		return Point.euclideanDistance(p, this) - radius;
	}

	int isInSingleCluster(){
		ALL_COUNT++;
		PriorityQueue<Entry<Integer, Double>> maxPriorityQueue = new PriorityQueue<Entry<Integer, Double>>(clusterer.CENTERS.size(), new Comparator<Entry<Integer, Double>>(){
			public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2){
				double d1 = e1.getValue(), d2 = e2.getValue();
				if(d1 > d2){
					return 1;
				}
				if(d1 < d2){
					return -1;
				}
				return 0;
			}
		});
		PriorityQueue<Entry<Integer, Double>> minPriorityQueue = new PriorityQueue<Entry<Integer, Double>>(clusterer.CENTERS.size(), new Comparator<Entry<Integer, Double>>(){
			public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2){
				double d1 = e1.getValue(), d2 = e2.getValue();
				if(d1 > d2){
					return 1;
				}
				if(d1 < d2){
					return -1;
				}
				return 0;
			}
		});

		int index = 0;
		for(ClusteringCenter center : clusterer.CENTERS){
			maxPriorityQueue.add(new SimpleEntry<Integer, Double>(index, this.maxDistance(center)));
			minPriorityQueue.add(new SimpleEntry<Integer, Double>(index, this.minDistance(center)));
			index++;
		}
		Entry<Integer, Double> the = maxPriorityQueue.poll();
        Entry<Integer, Double> comp = null;
		index = the.getKey();
		double theDist = the.getValue();

		while((comp = minPriorityQueue.poll()) != null){
			int idx = comp.getKey();
			double dis = comp.getValue();
			if(theDist < dis){
				if(idx != index){
					COUNT++;
					return index;
				}
				else
					continue;
			}
			else{
				if(idx == index)
					continue;
				return -1;
			}
		}
		return -1;
	}

    /**
     * 从所有的点中，获取距离点p最远的点，计算一个簇的半径时需要使用该方法
     * @param p
     * @return
     */
	private int getFarthestPoint(Point p){
		double maxDist = 0.0;
		int maxIndex = -1;
		for(int i : this.instances){
			Point pp = clusterer.instance(i);
			double dist = Point.euclideanDistance(p, pp);
			if(dist >= maxDist){
				maxDist = dist;
				maxIndex = i;
			}
		}
		return maxIndex;
	}

    /**
     * 把当前的超球面分成两个子球面，并把所有的点根据距离子球面中心的距离远近，分到距离近的子球面中
     */
	Hypersphere[] split(){
        //挑选两个距离最远的点，作为两个子球面的中心点
		int firstCenter = this.getFarthestPoint(this);
		Point firstPoint = clusterer.instance(firstCenter);
		int secondCenter = this.getFarthestPoint(firstPoint);
		Point secondPoint = clusterer.instance(secondCenter);

		this.children = new Hypersphere[2];
		this.children[0] = new Hypersphere(clusterer);
		this.children[1] = new Hypersphere(clusterer);
		this.children[0].addInstance(firstCenter);
		this.children[1].addInstance(secondCenter);

		for(int i : this.instances){
			if(i == firstCenter || i == secondCenter)
				continue;
			Point p = clusterer.instance(i);
			double dist1 = Point.euclideanDistance(p, firstPoint),
					dist2 = Point.euclideanDistance(p, secondPoint);
			if(dist1 < dist2){
				this.children[0].addInstance(i);
			}
			else{
				this.children[1].addInstance(i);
			}
		}
		this.children[0].endAdding();
		this.children[1].endAdding();
		return this.children;
	}
	
	Hypersphere[] getChildren(){
		return this.children;
	}
	
	void locateAndAssign(Hypersphere hp){
		int clusterIndex = hp.isInSingleCluster();
		if(clusterIndex != -1){
			ClusteringCenter cc = clusterer.CENTERS.get(clusterIndex);
			for(int pi : hp.instances){
				cc.addPointToCluster(pi);
			}
			return;
		}
		if(hp.children == null){
			for(int pi : hp.instances){
				Point p = clusterer.instance(pi);
				double minDist = Double.MAX_VALUE;
				int minCenIndex = 0, index = 0;
				for(ClusteringCenter cc : clusterer.CENTERS){
					double dist = Point.euclideanDistance(p, cc);
					if(dist < minDist){
						minDist = dist;
						minCenIndex = index;
					}
					index++;
				}
				ClusteringCenter cen = clusterer.CENTERS.get(minCenIndex);
				cen.addPointToCluster(pi);
			}
		}
		else{
			for(Hypersphere chp : hp.children){
				locateAndAssign(chp);
			}
		}
	}


    private void buildBallTree(){
        if (size() <= clusterer.MAX_INSTANCE_NUM_NOT_SPLIT || children!=null) {
            return;
        }

        Hypersphere[] children = split();
        children[0].buildBallTree();
        children[1].buildBallTree();
    }

    /**
     * 根据k均值聚类的点集合，创建ball tree，基本原理参考：
     * http://blog.csdn.net/skyline0623/article/details/8154911
     *
     * @param clusterer
     * @return
     */
    public static Hypersphere buildBallTree(KMeansClusterer clusterer) {
        Hypersphere hp = new Hypersphere(clusterer);
        for(int index=0; index<clusterer.instances.size(); index++) {
            hp.addInstance(index);
        }
        hp.endAdding();
        hp.buildBallTree();
        return hp;
    }
}
