package ruc.irm.xextractor.algorithm.kmeans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public class KMeansClusterer extends Clusterer {
	static int MAX_INSTANCE_NUM_NOT_SPLIT = 5;
	static int TRY_TIMES = 10;

	ArrayList<ClusteringCenter> CENTERS = new ArrayList<ClusteringCenter>();
    ArrayList<ClusteringCenter> PRE_CENTERS;

    private Hypersphere ballTree;

	//map cluster center results to its evaluation  
    private ArrayList<Entry<ArrayList<ClusteringCenter>, Double>> RESULTS = new ArrayList<>(TRY_TIMES);

    private boolean timeToEnd(){
		if(PRE_CENTERS == null)
			return false;

		for(ClusteringCenter cc : CENTERS){
			if(!PRE_CENTERS.contains(cc)) {
				//说明质心有变化，没有达到提前结束的条件
				return false;
			}
		}
		return true;
	}
	
	//gives your dataset's path and this function will build the internal data structures.	
	public void loadData(String path) throws IOException{
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)))); 
		String line;
		while((line = r.readLine()) != null){
			String[] fs = line.split(" +");
			double[] features = new double[fs.length];
			int i = 0;
			for(String s : fs){
                features[i++] = Double.valueOf(s + ".0");
			}
			this.dimension = fs.length;
			this.instances.add(new Point(features));
		}
		r.close();

        ballTree = Hypersphere.buildBallTree(this);
    }

    double evaluate(ArrayList<ClusteringCenter> cens){
		double ret = 0.0;
		for(ClusteringCenter cc : cens){
			ret += cc.evaluate();
		}
		return ret;
	}
	
	/**
	 * @param k  the initial number of clustering centers
	 * @return an entry:the key is the result of clustering.The label starts from 0.The value is the evaluation of the clustering result
	 */
	public Entry<Integer[], Double> cluster(int k) {
		for(int t = 0; t < TRY_TIMES; t++){
		    //random pick the cluster centers
            Random rand = new Random();
			CENTERS.clear();
			if(PRE_CENTERS != null)
				PRE_CENTERS = null;
            for(int j = 0; j<k; j++) {
                CENTERS.add(new ClusteringCenter(this, instances.get(rand.nextInt(instances.size()))));
            }

			//iteration until convergence
			while(!timeToEnd()){
                ballTree.locateAndAssign(ballTree);
				PRE_CENTERS = new ArrayList<>(CENTERS);
				ArrayList<ClusteringCenter> newCenters = new ArrayList<>();
				for(ClusteringCenter cc : CENTERS){
					cc = cc.getNewCenter();
					newCenters.add(cc);
				}
				CENTERS = newCenters;
			}
			RESULTS.add(new SimpleEntry<>(PRE_CENTERS, evaluate(PRE_CENTERS)));
			Hypersphere.ALL_COUNT = 0;
			Hypersphere.COUNT = 0;
		}

		double minEvaluate = Double.MAX_VALUE;
		int minIndex = 0, i = 0;
		for(Entry<ArrayList<ClusteringCenter>, Double> entry : RESULTS){
			double e = entry.getValue();
			if(e < minEvaluate){
				minEvaluate = e;
				minIndex = i;
			}
			i++;
		}
		CENTERS = RESULTS.get(minIndex).getKey();
		double evaluate = RESULTS.get(minIndex).getValue();

		Integer[] ret = new Integer[instances.size()];
		for(int cNum = 0; cNum < CENTERS.size(); cNum++){
			ClusteringCenter cc = CENTERS.get(cNum);
			for(int pi : cc.belongedPoints()){
				ret[pi] = cNum;
			}
		}
		return new SimpleEntry<Integer[], Double>(ret, evaluate);
	}
	
	/**gives the evaluation and differential of each k in specific range.you can use these infos to choose a good k for your clustering
	 * @param startK  gives the start point of k for the our try on k(inclusive)
	 * @param endK    gives the end point(exclusive)
	 * @return Entry's key is the evaluation of clustering of each k.The value is the differential of the evaluations--evaluation of k(i) - evaluation of k(i+1) for i in range(startK, endK - 1)
	 * */
	public Entry<ArrayList<Double>, ArrayList<Double>> cluster(int startK, int endK) {
		ArrayList<Integer[]> results = new ArrayList<Integer[]>();
		ArrayList<Double> evals = new ArrayList<Double>();
		for(int k = startK; k < endK; k++){
			System.out.println("now k = " + k);
			Entry<Integer[], Double> en = cluster(k);
			results.add(en.getKey());
			evals.add(en.getValue());
		}
		
		ArrayList<Double> subs = new ArrayList<Double>();
		for(int i = 0; i < evals.size() - 1; i++){
			subs.add(evals.get(i) - evals.get(i + 1));
		}
		
		
		return new SimpleEntry<ArrayList<Double>, ArrayList<Double>>(evals, subs);
		
	}

    public static void main(String[] args) throws IOException {
        KMeansClusterer process = new KMeansClusterer();
        process.loadData("./test/cluster-example.txt");
        Entry<Integer[], Double> entry = process.cluster(3);
        for(Integer i: entry.getKey()) {
            System.out.print(i + " ");
        }
        System.out.println();
        System.out.println(entry.getValue());
    }
}
