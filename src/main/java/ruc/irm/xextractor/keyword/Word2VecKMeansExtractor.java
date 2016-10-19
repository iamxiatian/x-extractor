package ruc.irm.xextractor.keyword;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.algorithm.Word2Vec;
import ruc.irm.xextractor.commons.ExtractConf;
import ruc.irm.xextractor.nlp.SegWord;
import ruc.irm.xextractor.nlp.Segment;
import ruc.irm.xextractor.nlp.SegmentFactory;
import smile.clustering.KMeans;
import smile.clustering.XMeans;
import smile.math.distance.EuclideanDistance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 基于word2vec和k均值聚类的关键词抽取
 *
 * @ChangeLog:
 * 2016/04/29: 合并一起出现的关键词抽取结果，例如玉米、价格、指数，合并为一个玉米价格指数
 *
 * User: xiatian
 * Date: 3/31/13 6:15 PM
 */
public class Word2VecKMeansExtractor implements KeywordExtractor {
    private Logger LOG = LoggerFactory.getLogger(Word2VecKMeansExtractor.class);

    private boolean mergeNeighbor = true;

    private Configuration conf = null;

    private Segment segment = null;
    private Word2Vec word2Vec = null;

    public Word2VecKMeansExtractor() {
        this(ExtractConf.create());
    }

    public Word2VecKMeansExtractor(Configuration c) {
        this.conf = (c == null) ? ExtractConf.create() : c;

        this.mergeNeighbor = this.conf.getBoolean("extractor.keyword.merge.neighbor", false);
        String modelFile = this.conf.get("extractor.keyword.word2vec.file", "./word2vec.bin");
        if(!new File(modelFile).exists()) {
            throw new RuntimeException("Word2vec model file does not exists! model filename:" + modelFile);
        }
        this.word2Vec = Word2Vec.getInstance(modelFile);
        this.segment = SegmentFactory.getSegment(conf);
        segment.tag("启动分词程序");
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

    public static double euclideanDistance(double[] p1, double[] p2){
        double[] p = new double[p1.length];
        for(int i = 0; i < p1.length; ++i)
            p[i] = p1[i] - p2[i];
        double sum = 0.0;
        for(int i = 0; i < p1.length; ++i){
            sum += Math.pow(p[i], 2.0);
        }
        return Math.sqrt(sum);
    }

    /**
     * 对文本的word2vec词向量进行k-means聚类，取距离聚类中心最近的词语作为聚类结果返回。
     *
     * @param text
     * @param kOrMaxK: 如果采用k-means，则为聚类数量，如果采用X-Means，则为最大可能的聚类数量
     * @return
     */
    public String[] clustering(String text, int kOrMaxK) {
        List<SegWord> words = segment.tag(text);
        List<String> instanceNames = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();

        int lastPosition = -1;
        for (int i = 0; i < words.size(); i++) {
            SegWord segWord = words.get(i);

            if ("w".equalsIgnoreCase(segWord.pos) || "null".equalsIgnoreCase(segWord.pos)) {
                continue;
            }

            if (segWord.word.length() >= 2 && (segWord.pos.startsWith("n") || segWord.pos.startsWith("adj") || segWord.pos.startsWith("v"))) {
                float[] vector = word2Vec.getWordVector(segWord.word);
                if (vector != null) {
                    instanceNames.add(segWord.word);
                    vectors.add(IntStream.range(0, vector.length).mapToDouble(idx -> vector[idx]).toArray());
                }
            }
        }

        double[][] data = new double[vectors.size()][];
        int idx = 0;
        for (double[] vector : vectors) {
            data[idx++] = vector;
        }

        //List<String> labels = new ArrayList<>();
        //KMeans kmeans = new KMeans(data, k, 20);
        KMeans kmeans = new XMeans(data, kOrMaxK*2);

        double[][] centroids = kmeans.centroids();
        int clusterNumber = centroids.length;
        double[] centerDistances = new double[clusterNumber];
        Arrays.fill(centerDistances, Double.MAX_VALUE);

        String[] labels = new String[clusterNumber];
        int[] clusterLabel = kmeans.getClusterLabel();

        for (int i=0; i<clusterLabel.length; i++) {
            int c = clusterLabel[i];

            //double distance = euclideanDistance(centroids[c], data[i]);
            double distance = new EuclideanDistance().d(centroids[c], data[i]);
            if (distance < centerDistances[c]) {
                centerDistances[c] = distance;
                labels[c] = instanceNames.get(i);
            }
        }

        return labels;
    }

    public List<String> extractAsList(String title, String content, int topN) {
        return Lists.newArrayList(clustering(title + "\n" + content, topN));
    }


    public static void main(String[] args) {
        String title = "维基解密否认斯诺登接受委内瑞拉庇护";
        String content = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";

        Configuration conf = new Configuration();
        Word2VecKMeansExtractor extractor = new Word2VecKMeansExtractor(conf);
        System.out.println(extractor.extractAsList(title, content, 5));
    }
}
