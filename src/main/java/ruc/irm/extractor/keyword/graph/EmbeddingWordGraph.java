package ruc.irm.extractor.keyword.graph;

import ruc.irm.extractor.algorithm.Word2Vec;

import java.util.Map;

/**
 * 融合Word2Vec的关键词抽取
 *
 * User: xiatian
 */
public class EmbeddingWordGraph extends WordGraph {
    //词语的覆盖影响力因子
    private float paramAlpha = 0.1f;

    //词语的位置影响力因子
    private float paramBeta = 0.8f;

    //词语的频度影响力因子
    private float paramGamma = 0.1f;

    private Word2Vec word2Vec = null;

    public EmbeddingWordGraph() {
        super();
        this.word2Vec = Word2Vec.getInstance("./word2vec.bin");
    }

    public EmbeddingWordGraph(float paramAlpha, float paramBeta, float paramGamma, boolean linkBack) {
        this();

        this.paramAlpha = paramAlpha;
        this.paramBeta = paramBeta;
        this.paramGamma = paramGamma;
        this.linkBack = linkBack;
    }

    /**
     * 计算词图节点两两之间的word2vec相似度，保存到二维数组中
     * @return
     */
    private float[][] word2vecSimMatrix(String[] words) {
        float[][] matrix = new float[words.length][words.length];
        for(int i=0; i<words.length; i++) {
            matrix[i][i] = 0;
            for(int j=i+1; j<words.length; j++) {
                float sim = word2Vec.similarity(words[i], words[j]);
                if(sim==0) {
                    sim = 1.0f/words.length;
                }

                matrix[i][j] = sim;
                matrix[j][i] = sim;
            }
        }

        return matrix;
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

//        float[][] simMatrix = word2vecSimMatrix(words);
//        float[] word2vecScores = new float[words.length];
//        float word2vecTotalScore = 0;
//        for(i=0; i<words.length; i++) {
//            float lineTotal = 0;
//            for(int j=0; j<words.length; j++) {
//                lineTotal += simMatrix[i][j];
//            }
//            word2vecScores[i] = lineTotal;
//            word2vecTotalScore += lineTotal;
//        }

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
            float totalWord2VecScore = 0.0f;
            for (String w : adjacentWords.keySet()) {
                totalImportance += wordNodeMap.get(w).getImportance();
                totalOccurred += wordNodeMap.get(w).getCount();

                totalWord2VecScore += word2Vec.similarity(wordFrom, w);
            }

            for (int j = 0; j < words.length; j++) {
                String wordTo = words[j];
                WordNode nodeTo = wordNodeMap.get(wordTo);

                //根据宁建飞 刘降珍:《融合 Word2vec 与 TextRank 的关键词抽取研究》一文设置的权重
                if (adjacentWords.containsKey(wordTo)) {
                    //计算i到j的转移概率
                    double partA = 1.0f / adjacentWords.size();
                    //double partB = nodeTo.getImportance() / totalImportance;
                    //double partC = nodeTo.getCount() * 1.0f / totalOccurred;

                    //double partD = word2vecScores[j]/word2vecTotalScore;
                    float partD = word2Vec.similarity(wordFrom, wordTo)/totalWord2VecScore;

                    //double sim = word2Vec.similarity(wordFrom, wordTo);

                    //matrix[j][i] = partA * paramAlpha + partB * paramBeta + partC * paramGamma;
                    //matrix[j][i] = matrix[j][i]*0.9 + 0.1*partD;
                    //matrix[j][i] = matrix[j][i]*0.8 + 0.2*sim;
                    matrix[j][i] = 0.5*partA + 0.5*partD;
                }
            }
        }

        //合并Word2Vec的相似度计算结果
//        for (i = 0; i < words.length; i++) {
//            String word1 = words[i];
//
//            for(int j=1; j<words.length; j++) {
//                String word2 = words[j];
//                double sim = word2Vec.similarity(word1, word2);
//                matrix[i][j] = 0.8*matrix[i][j] + 0.2*sim;
//                matrix[j][i] = 0.8*matrix[j][i] + 0.2*sim;
//            }
//        }

        return new PageRankGraph(words, values, matrix);
    }

}
