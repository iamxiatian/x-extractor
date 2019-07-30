package ruc.irm.extractor.keyword;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hankcs.hanlp.HanLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinang.conf.Configuration;
import ruc.irm.extractor.commons.ChineseStopKeywords;
import ruc.irm.extractor.commons.ExtractConf;
import ruc.irm.extractor.keyword.divrank.ClusterWordDivGraph;
import ruc.irm.extractor.keyword.divrank.PositionWordDivGraph;
import ruc.irm.extractor.keyword.graph.*;
import ruc.irm.extractor.nlp.SegmentFactory;

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
public class TextRankExtractor implements KeywordExtractor {
    private Logger LOG = LoggerFactory.getLogger(TextRankExtractor.class);

    private float alpha = 0.1f;
    private float beta = 0.9f;
    private float gamma = 0.0f;
    private float lambda = 30.0f;
    private int maxReadWordCount = 2000;
    private boolean useHanlpMethod = true;
    private boolean mergeNeighbor = true;

    public enum GraphType {
        TextRank, //传统的TextRank方法
        PositionRank, //词语位置加权
        NingJianfei, //融合 Word2vec 与 TextRank 的关键词抽取研究
        ClusterRank, //词向量聚类加权
        ClusterPositionRank, //词向量聚类+位置加权
        PositionDivRank, //词语位置加权+DivRank
        ClusterDivRank, //词向量聚类加权+DivRank
    }

    private GraphType graphType;

    private Configuration conf = null;

    public TextRankExtractor(GraphType type) {
        this(ExtractConf.create(), type);
    }

    public TextRankExtractor(Configuration c, GraphType type) {
        this.conf = (c == null) ? ExtractConf.create() : c;

        this.useHanlpMethod = this.conf.getBoolean("extractor.keyword.hanlp", false);
        this.maxReadWordCount = this.conf.getInt("extractor.keyword.valid.word.count", 2000);

        this.alpha = this.conf.getFloat("extractor.keyword.alpha", 0.1f);
        this.beta = this.conf.getFloat("extractor.keyword.beta", 0.9f);
        this.gamma = this.conf.getFloat("extractor.keyword.gamma", 0.0f);
        this.lambda = this.conf.getFloat("extractor.keyword.lambda", 30.0f);

        this.mergeNeighbor = this.conf.getBoolean("extractor.keyword.merge.neighbor", false);
        this.graphType = type;
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
            WordGraph wordGraph = null;

            if(graphType == GraphType.TextRank) {
                wordGraph = new PositionWordGraph(1, 0, 0, true);
            } else if(graphType == GraphType.PositionRank) {
                wordGraph = new PositionWordGraph(0.33f, 0.34f, 0.33f, true);
            } else if(graphType == GraphType.NingJianfei){
                wordGraph = new EmbeddingWordGraph(alpha, beta, gamma, true);
            } else if(graphType == GraphType.ClusterRank){
                wordGraph = new ClusterWordGraph(0.5f, 0, 0.5f, topN, true);
            } else if(graphType == GraphType.PositionDivRank){
                wordGraph = new PositionWordDivGraph(0.33f, 0.34f, 0.33f, true);
            }else if(graphType == GraphType.ClusterDivRank){
                wordGraph = new ClusterWordDivGraph(0.5f, 0, 0.5f, topN, true);
            } else {
                wordGraph = new ClusterWordGraph(0.33f, 0.34f, 0.33f, topN, true);
            }
            wordGraph.build(title, lambda);
            wordGraph.build(content, 1.0f);

            RankGraph g = wordGraph.makeRankGraph();
            g.iterateCalculation(20, 0.85f);
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
                String merged = merge(word, wordGraph, keywords);
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

    public static void main(String[] args) {
        String title = "维基解密否认斯诺登接受委内瑞拉庇护";
        String content = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";

        Configuration conf = new Configuration();
        TextRankExtractor extractor = new TextRankExtractor(GraphType.ClusterRank);
        System.out.println(extractor.extractAsString(title, content, 5));
    }
}
