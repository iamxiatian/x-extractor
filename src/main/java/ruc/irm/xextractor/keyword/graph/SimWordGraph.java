package ruc.irm.xextractor.keyword.graph;

import ruc.irm.xextractor.algorithm.Word2Vec;
import smile.clustering.KMeans;
import smile.clustering.XMeans;
import smile.math.distance.EuclideanDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 融合Word2Vec的关键词抽取
 *
 * User: xiatian
 */
public class SimWordGraph extends WordGraph {
    //词语的覆盖影响力因子
    private float paramAlpha = 0.33f;

    //词语的位置影响力因子
    private float paramBeta = 0.34f;

    //词语的频度影响力因子
    private float paramGamma = 0.33f;

    private Word2Vec word2Vec = null;

    private int maxK = 5;

    public SimWordGraph() {
        super();
        this.word2Vec = Word2Vec.getInstance("/home/xiatian/data/wiki/word2vec.bin");
    }

    public SimWordGraph(float alpha, float beta, float gamma, int maxK, boolean linkBack) {
        this();

        this.paramAlpha = alpha;
        this.paramBeta = beta;
        this.paramGamma = gamma;
        this.linkBack = linkBack;
        this.maxK = maxK;
    }


    @Override
    public PageRankGraph makePageRankGraph() {
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
                totalClusterImportance += word2Vec.similarity(wordFrom, w);
            }

            for (int j = 0; j < words.length; j++) {
                String wordTo = words[j];
                WordNode nodeTo = wordNodeMap.get(wordTo);

                if (adjacentWords.containsKey(wordTo)) {
                    //计算i到j的转移概率
                    double partA = 1.0f / adjacentWords.size();
                    double partB = nodeTo.getImportance() / totalImportance;
                    //double partC = nodeTo.getCount() * 1.0f / totalOccurred;

                    double partC = word2Vec.similarity(wordFrom, wordTo)/totalClusterImportance;

                    matrix[j][i] =paramAlpha*partA + paramBeta*partB + paramGamma*partC;
                }
            }
        }

        return new PageRankGraph(words, values, matrix);
    }

}
