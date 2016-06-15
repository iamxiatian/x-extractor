package ruc.irm.xextractor.keyword;

import org.zhinang.conf.Configuration;
import org.zhinang.util.ds.KeyValuePair;
import ruc.irm.xextractor.nlp.SegWord;
import ruc.irm.xextractor.nlp.Segment;
import ruc.irm.xextractor.nlp.SegmentFactory;

import java.util.*;

/**
 * 关键词词图
 *
 * 参数说明请参考：夏天. 词语位置加权TextRank的关键词抽取研究. 现代图书情报技术, 2013, 29(9): 30-34.
 * User: xiatian
 * Date: 3/10/13 4:07 PM
 */
public class WordGraph {
    private Segment segment = null;

    //是否在后面的词语中加上指向前面词语的链接关系
    private boolean linkBack = true;

    //词语的覆盖影响力因子
    private float paramAlpha = 0.1f;

    //词语的位置影响力因子
    private float paramBeta = 0.8f;

    //词语的频度影响力因子
    private float paramGamma = 0.1f;

    /**
     * 如果读取的单词数量超过该值，则不再处理以后的内容，以避免文本过长导致计算速度过慢
     */
    private int maxReadableWordCount = Integer.MAX_VALUE;

    /**
     * 读取的词语的数量
     */
    private int readWordCount = 0;

    private Map<String, WordNode> wordNodeMap = new HashMap<String, WordNode>();

    public WordGraph() {
        this.segment = SegmentFactory.getSegment(new Configuration());
    }

    public WordGraph(float paramAlpha, float paramBeta, float paramGamma, boolean linkBack) {
        this();

        this.paramAlpha = paramAlpha;
        this.paramBeta = paramBeta;
        this.paramGamma = paramGamma;
        this.linkBack = linkBack;
    }

    /**
     * 直接通过传入的词语和重要性列表构建关键词图
     * @param wordsWithImportance
     */
    public void build(List<KeyValuePair<String, Double>> wordsWithImportance) {
        int lastPosition = -1;
        for (int i = 0; i < wordsWithImportance.size(); i++) {
            String word = wordsWithImportance.get(i).getKey();
            double importance = wordsWithImportance.get(i).getValue();

            WordNode wordNode = wordNodeMap.get(word);

            //如果已经读取了最大允许的单词数量，则忽略后续的内容
            readWordCount++;
            if (readWordCount > maxReadableWordCount) {
                return;
            }

            if (wordNode == null) {
                //如果额外指定了权重，则使用额外指定的权重代替函数传入的权重
                double specifiedWeight = SpecifiedWeight.getWordWeight(word, 0.0f);
                if (specifiedWeight < importance) {
                    specifiedWeight = importance;
                }
                wordNode = new WordNode(word, "IGNORE", 0, specifiedWeight);
                wordNodeMap.put(word, wordNode);
            } else if (wordNode.getImportance() < importance) {
                wordNode.setImportance(importance);
            }

            wordNode.setCount(wordNode.getCount() + 1);

            //加入邻接点
            if (lastPosition >= 0) {
                WordNode lastWordNode = wordNodeMap.get(wordsWithImportance.get(lastPosition).getKey());
                lastWordNode.addAdjacentWord(word);

                if (linkBack) {
                    //加入逆向链接
                    wordNode.addAdjacentWord(lastWordNode.getName());
                }

                if (lastPosition == i - 1) {
                    if(wordNode.getPos().startsWith("n") &&
                            (lastWordNode.getPos().equals("adj") || lastWordNode.getPos().startsWith("n"))) {
                        wordNode.addLeftNeighbor(lastWordNode.getName());
                        lastWordNode.addRightNeighbor(wordNode.getName());
                    }
                }
            }
            lastPosition = i;
        }
    }

    public Set<String> getLeftNeighbors(String word) {
        return wordNodeMap.get(word).getLeftNeighbors();
    }

    public Set<String> getRightNeighbors(String word) {
        if(wordNodeMap.get(word).getPos().equals("nr")) {
            //相当于不合并人名后面的词语
            return new HashSet<>();
        } else {
            return wordNodeMap.get(word).getRightNeighbors();
        }
    }

    public void build(String text, float importance) {
        List<SegWord> words = segment.tag(text);

        int lastPosition = -1;
        for (int i = 0; i < words.size(); i++) {
            SegWord segWord = words.get(i);

            if ("w".equalsIgnoreCase(segWord.pos) || "null".equalsIgnoreCase(segWord.pos)) {
                continue;
            }

            if (segWord.word.length() >= 2 && (segWord.pos.startsWith("n") || segWord.pos.startsWith("adj") || segWord.pos.startsWith("v"))) {
                WordNode wordNode = wordNodeMap.get(segWord.word);

                //如果已经读取了最大允许的单词数量，则忽略后续的内容
                readWordCount++;
                if (readWordCount > maxReadableWordCount) {
                    return;
                }

                if (wordNode == null) {
                    //如果额外指定了权重，则使用额外指定的权重代替函数传入的权重
                    float specifiedWeight = SpecifiedWeight.getWordWeight(segWord.word, 0.0f);
                    if (specifiedWeight < importance) {
                        specifiedWeight = importance;
                    }

                    if(segWord.pos.equals("ns") || segWord.equals("nr")) {
                        specifiedWeight = specifiedWeight*1.3f;
                    } else if (segWord.pos.startsWith("v")) {
                        specifiedWeight *= 0.8f;
                    }
                    wordNode = new WordNode(segWord.word, segWord.pos, 0, specifiedWeight);
                    wordNodeMap.put(segWord.word, wordNode);
                } else if (wordNode.getImportance() < importance) {
                    wordNode.setImportance(importance);
                }

                wordNode.setCount(wordNode.getCount() + 1);

                //加入邻接点
                if (lastPosition >= 0) {
                    WordNode lastWordNode = wordNodeMap.get(words.get(lastPosition).word);
                    lastWordNode.addAdjacentWord(segWord.word);

                    if (linkBack) {
                        //加入逆向链接
                        wordNode.addAdjacentWord(lastWordNode.getName());
                    }

                    if (lastPosition == i - 1) {
                        if(wordNode.getPos().startsWith("n") &&
                                (lastWordNode.getPos().equals("adj") || lastWordNode.getPos().startsWith("n"))) {
                            wordNode.addLeftNeighbor(lastWordNode.getName());
                            lastWordNode.addRightNeighbor(wordNode.getName());
                        }
                    }
                }
                lastPosition = i;
            }

//            if(segWord.pos.equals("PU")) {
//                lastPosition = -1; //以句子为单位
//            }
        }

    }

    /**
     * 该步处理用于删除过多的WordNode，仅保留出现频次在前100的词语，以便加快处理速度
     */
    private void shrink() {

    }

    public PageRankGraph makePageRankGraph() {
        final String[] words = new String[wordNodeMap.size()];
        double[] values = new double[wordNodeMap.size()];
        final double[][] matrix = new double[wordNodeMap.size()][wordNodeMap.size()];

        int i = 0;
        for (Map.Entry<String, WordNode> entry : wordNodeMap.entrySet()) {
            words[i] = entry.getKey();
            values[i] = 1.0f / wordNodeMap.size();
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
                    matrix[j][i] = partA * paramAlpha + partB * paramBeta + partC * paramGamma;
                }
            }
        }

        return new PageRankGraph(words, values, matrix);
    }

    /**
     * 设置最大可以读取的词语数量
     *
     * @param maxReadableWordCount
     */
    public void setMaxReadableWordCount(int maxReadableWordCount) {
        this.maxReadableWordCount = maxReadableWordCount;
    }
}
