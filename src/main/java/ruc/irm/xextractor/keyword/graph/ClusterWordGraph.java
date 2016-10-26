package ruc.irm.xextractor.keyword.graph;

import ruc.irm.xextractor.algorithm.Word2Vec;
import ruc.irm.xextractor.nlp.SegWord;
import smile.clustering.KMeans;
import smile.clustering.XMeans;
import smile.math.distance.EuclideanDistance;

import java.util.*;
import java.util.stream.IntStream;

/**
 * 融合Word2Vec的关键词抽取
 *
 * User: xiatian
 */
public class ClusterWordGraph extends WordGraph {
    //词语的覆盖影响力因子
    private float paramAlpha = 0.33f;

    //词语的位置影响力因子
    private float paramBeta = 0.34f;

    //词语的频度影响力因子
    private float paramGamma = 0.33f;

    private Word2Vec word2Vec = null;

    private int maxK = 5;

    public ClusterWordGraph() {
        super();
        this.word2Vec = Word2Vec.getInstance("/home/xiatian/data/wiki/word2vec.bin");
    }

    public ClusterWordGraph(float alpha, float beta, float gamma, int maxK, boolean linkBack) {
        this();

        this.paramAlpha = alpha;
        this.paramBeta = beta;
        this.paramGamma = gamma;
        this.linkBack = linkBack;
        this.maxK = maxK;
    }

    public double similarity(double[] vector1, double[] vector2) {
        float cosine = 0.0f;
        for (int i = 0; i < vector1.length; i++) {
            cosine += vector1[i] * vector2[i];
        }

        return cosine;
    }

    /**
     * 对图的wordNodeMap包含的词语对应的word2vec词向量进行k-means聚类，然后根据每个词语到质心的距离，计算该词语在簇中的重要性
     *
     * @return
     */
    public Map<String, Double> clustering() {
        List<String> instanceNames = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();

        int lastPosition = -1;

        for (String word: wordNodeMap.keySet()) {
            float[] vector = word2Vec.getWordVector(word);
            if (vector == null) {
                continue;
            }
            instanceNames.add(word);
            vectors.add(IntStream.range(0, vector.length)
                    .mapToDouble(idx -> vector[idx])
                    .toArray());
        }

        double[][] data = new double[vectors.size()][];
        int idx = 0;
        for (double[] vector : vectors) {
            data[idx++] = vector;
        }

        //List<String> labels = new ArrayList<>();
        //KMeans kmeans = new XMeans(data, maxK);
        int k = maxK==1?2:maxK; //至少要分为两簇
        KMeans kmeans = new XMeans(data, k);

        double[][] centroids = kmeans.centroids();
        int clusterNumber = centroids.length;
        int[] clusterLabel = kmeans.getClusterLabel();

        //记录每个实例的重要性：可以用距离来衡量
        double[] clusterInstDistances = new double[clusterLabel.length];
        //每个簇里面，所有实例到质心的总距离
        double[] clusterTotalDistances = new double[centroids.length];
        //每个簇所拥有的实例数量
        int[] clusterInstCounts = new int[centroids.length];

        for (int i=0; i<clusterLabel.length; i++) {
            int c = clusterLabel[i];
            clusterInstCounts[c] +=1;
            //double distance = euclideanDistance(centroids[c], data[i]);
            double distance = new EuclideanDistance().d(centroids[c], data[i]);
            //double distance = (1-similarity(centroids[c], data[i]));
            clusterInstDistances[i] = distance;
            clusterTotalDistances[c] += distance;
        }

        Map<String, Double> map = new HashMap<>();
        for(int i=0; i<clusterLabel.length; i++) {
            int c = clusterLabel[i];
            double v = clusterInstDistances[i]/clusterTotalDistances[c];
            v = v * clusterInstCounts[c]; //距离质心越远，携带的不同信息量越大，越重要。
            // v = (1-v) * clusterInstCounts[c];
            map.put(instanceNames.get(i), v);
        }

        return map;
    }


    @Override
    public PageRankGraph makePageRankGraph() {
        Map<String, Double> clusterImportanceMap = clustering();
        final String[] words = new String[wordNodeMap.size()];
        double[] values = new double[wordNodeMap.size()];
        final double[][] matrix = new double[wordNodeMap.size()][wordNodeMap.size()];

        int i = 0;
        double defaultValue = 1.0f / wordNodeMap.size();
        for (Map.Entry<String, WordNode> entry : wordNodeMap.entrySet()) {
            words[i] = entry.getKey();
            values[i] = defaultValue;
            i++;
        }

        //输出word2vec的重要性
        for (i = 0; i < words.length; i++) {
           String wordFrom = words[i];

            WordNode nodeFrom = wordNodeMap.get(wordFrom);
            if (nodeFrom == null) {
                continue;
            }

            Map<String, Integer> adjacentWords = nodeFrom.getAdjacentWords();

            float totalImportance = 0.0f;    //相邻节点的节点重要性之和
            int totalOccurred = 0;       //相邻节点出现的总频度


            double totalClusterImportance = 0.0;
            for (String w : adjacentWords.keySet()) {
                totalImportance += wordNodeMap.get(w).getImportance();
                totalOccurred += wordNodeMap.get(w).getCount();
                totalClusterImportance += clusterImportanceMap.getOrDefault(w, 1d);
            }

            for (int j = 0; j < words.length; j++) {
                String wordTo = words[j];
                WordNode nodeTo = wordNodeMap.get(wordTo);

                if (adjacentWords.containsKey(wordTo)) {
                    //计算i到j的转移概率
                    double partA = 1.0f / adjacentWords.size();
                    double partB = nodeTo.getImportance() / totalImportance;
                    //double partC = nodeTo.getCount() * 1.0f / totalOccurred;

                    double partC = clusterImportanceMap.getOrDefault(wordTo, 1d)/totalClusterImportance;

                    matrix[j][i] =paramAlpha*partA + paramBeta*partB + paramGamma*partC;
                }
            }
        }

        return new PageRankGraph(words, values, matrix);
    }

}
