package ruc.irm.extractor.keyword.graph;

import java.util.*;

/**
 * 词语位置加权实现的关键词词图
 *
 * 参数说明请参考：夏天. 词语位置加权TextRank的关键词抽取研究. 现代图书情报技术, 2013, 29(9): 30-34.
 * User: xiatian
 * Date: 3/10/13 4:07 PM
 */
public class PositionWordGraph extends WordGraph {
    //词语的覆盖影响力因子
    private float paramAlpha = 0.1f;

    //词语的位置影响力因子
    private float paramBeta = 0.8f;

    //词语的频度影响力因子
    private float paramGamma = 0.1f;


    public PositionWordGraph() {
        super();
    }

    public PositionWordGraph(float paramAlpha, float paramBeta, float paramGamma, boolean linkBack) {
        this();

        this.paramAlpha = paramAlpha;
        this.paramBeta = paramBeta;
        this.paramGamma = paramGamma;
        this.linkBack = linkBack;
    }

    @Override
    public PageRankGraph makeRankGraph() {
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

        for (i = 0; i < words.length; i++) {
            String wordFrom = words[i];

            WordNode nodeFrom = wordNodeMap.get(wordFrom);
            if (nodeFrom == null) {
                continue;
            }

            Map<String, Integer> adjacentWords = nodeFrom.getAdjacentWords();

            float totalImportance = 0.0f;    //相邻节点的节点重要性之和
            int totalOccurred = 0;       //相邻节点出现的总频度
            for (String w : adjacentWords.keySet()) {
                totalImportance += wordNodeMap.get(w).getImportance();
                totalOccurred += wordNodeMap.get(w).getCount();
            }

            for (int j = 0; j < words.length; j++) {
                String wordTo = words[j];
                WordNode nodeTo = wordNodeMap.get(wordTo);

                if (adjacentWords.containsKey(wordTo)) {
                    //计算i到j的转移概率
                    double partA = 1.0f / adjacentWords.size();
                    double partB = nodeTo.getImportance() / totalImportance;
                    double partC = nodeTo.getCount() * 1.0f / totalOccurred;
                    //matrix[j][i] = 0.33 * paramAlpha + 0.34 * paramBeta + 0.33 * paramGamma;
                    matrix[j][i] = partA * paramAlpha + partB * paramBeta + partC * paramGamma;
                }
            }
        }

        return new PageRankGraph(words, values, matrix);
    }

}
