package ruc.irm.xextractor.keyword;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hankcs.hanlp.HanLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.commons.ChineseStopKeywords;
import ruc.irm.xextractor.commons.ExtractConf;
import ruc.irm.xextractor.nlp.SegmentFactory;

import java.io.IOException;
import java.util.*;

/**
 * Automatic keyword extractor interface.
 *
 * @ChangeLog:
 * 2016/04/29: 合并一起出现的关键词抽取结果，例如玉米、价格、指数，合并为一个玉米价格指数
 *
 * User: xiatian
 * Date: 3/31/13 6:15 PM
 */
public class KeywordExtractor {
    private Logger LOG = LoggerFactory.getLogger(KeywordExtractor.class);

    private float alpha = 0.1f;
    private float beta = 0.9f;
    private float gamma = 0.0f;
    private float lambda = 30.0f;
    private int maxReadWordCount = 2000;
    private boolean useHanlpMethod = true;
    private boolean mergeNeighbor = true;

    private Configuration configuration = null;

    public KeywordExtractor() {
        this(ExtractConf.create());
    }

    public KeywordExtractor(Configuration conf) {
        this.configuration = (conf == null) ? ExtractConf.create() : conf;

        this.useHanlpMethod = configuration.getBoolean("extractor.keyword.hanlp", false);
        this.maxReadWordCount = configuration.getInt("extractor.keyword.valid.word.count", 2000);

        this.alpha = configuration.getFloat("extractor.keyword.alpha", 0.1f);
        this.beta = configuration.getFloat("extractor.keyword.beta", 0.9f);
        this.gamma = configuration.getFloat("extractor.keyword.gamma", 0.0f);
        this.lambda = configuration.getFloat("extractor.keyword.lambda", 30.0f);

        this.mergeNeighbor = configuration.getBoolean("extractor.keyword.merge.neighbor", false);
    }

    /**
     * 设置人工指定的权重
     *
     * @param word
     * @param weight
     */
    public static void setSpecifiedWordWeight(String word, String pos, float weight) {
        SpecifiedWeight.setWordWeight(word, weight);

        //同时插入分词程序
        SegmentFactory.getSegment(ExtractConf.create()).insertUserDefinedWord(word, pos, 10);
    }

    public List<String> extractAsList(String title, String content, int topN) {
        List<String> keywords = new LinkedList<>();

        if (useHanlpMethod) {
            LOG.debug("use HanLP provided keyword extractor.");
            //use hanlp provided keyword extractor
            return HanLP.extractKeyword(content, topN);
        } else {
            //use improved text rank method proposed by xiatian
            WordGraph builder = new WordGraph(alpha, beta, gamma, true);
            builder.build(title, lambda);
            builder.build(content, 1.0f);

            PageRankGraph g = builder.makePageRankGraph();
            g.iterateCalculation(20, 0.15f);
            g.quickSort();
            int count = 0;
            int limit = topN;
            if (mergeNeighbor) {
                limit += 10; //多挑选10个候选关键词，以尽可能使合并后的关键词数量也能够取到topN个
            }
            for (int i = 0; i < g.labels.length && count < limit; i++) {
                String word = g.labels[i];
                if(!ChineseStopKeywords.isStopKeyword(word)) {
                    keywords.add(word);
                    count++;
                }
            }

            if (!mergeNeighbor) {
                return keywords;
            }

            //对抽取出的关键词，合并相邻出现的词语, 如玉米、价格、指数，合并为玉米价格指数
            List<String> filteredResult = new ArrayList<>();
            boolean changed = false;
            count = 0;
            while (keywords.size()>0){
                String word = keywords.remove(0);
                String merged = merge(word, builder, keywords);
                filteredResult.add(merged);
                if ((++count) == topN) {
                    break;
                }
            }

            return filteredResult;
        }
    }

    /**
     * 合并词语current相邻的词语
     *
     * @param current
     * @param graph
     * @param candidates
     * @return
     */
    private String merge(String current, WordGraph graph, List<String> candidates) {
        Set<String> rights = graph.getRightNeighbors(current);
        Set<String> lefts = graph.getLeftNeighbors(current);

        String mergedText = current;
        Set<String> mergedItems = new HashSet<>();
        for (String word : candidates) {
            if (rights.contains(word)) {
                mergedText = mergedText + word;
                rights = graph.getRightNeighbors(word); //该边右邻集合，不断向右合并
                mergedItems.add(word);
            } else if (lefts.contains(word)) {
                mergedText = word + mergedText;
                lefts = graph.getLeftNeighbors(word); //该边左邻集合，不断向左合并
                mergedItems.add(word);
            }
        }

        //删除已经合并掉的词语
        if (mergedItems.size() > 0) {
            candidates.removeAll(mergedItems);
        }
        return mergedText;
    }

    public String extractAsString(String title, String content, int topN) {
        StringBuilder sb = new StringBuilder();
        List<String> keywords = extractAsList(title, content, topN);
        for (String keyword : keywords) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(keyword);
        }
        return sb.toString();
    }

    private static void loadBiGram(String uri) throws IOException {
        neighbors = Maps.newConcurrentMap();
        List<String> lines = Resources.readLines(Resources.getResource(uri), Charsets.UTF_8);
        for (String line : lines) {
            String[] items = line.split("\t");
            if (items.length == 2) {
                Set<String> words = neighbors.getOrDefault(items[0], new HashSet<>());
                words.add(items[1]);
                neighbors.put(items[0], words);
            }
        }
    }

    private static Map<String, Set<String>> neighbors = null;
}
