package ruc.irm.xextractor.evaluation;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import org.apache.commons.cli.*;
import org.xml.sax.SAXException;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.commons.ExtractConf;
import ruc.irm.xextractor.keyword.TextRankExtractor;
import static ruc.irm.xextractor.keyword.TextRankExtractor.GraphType.*;
import ruc.irm.xextractor.keyword.Word2VecKMeansExtractor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * 关键词抽取测试
 *
 * @author Tian Xia
 * @date Aug 21, 2016 18:29
 */
public class ExtractKeywordEvaluation {

    private static EvalResult evaluate(List<String> providedKeywords, List<String> extractedKeywords) {
        Set<String> originKeywords = Sets.newHashSet(providedKeywords);
        int matched = 0;
        int extractedLabelCount = 0;
        for (String keyword : extractedKeywords) {
            extractedLabelCount++;
            if (originKeywords.contains(keyword)) {
                matched++;
            }
        }

        double recall = matched * 1.0 / providedKeywords.size();
        double precision = matched * 1.0 / extractedLabelCount;
        if (matched == 0) {
            return new EvalResult(0, 0, 0.0);
        } else {
            return new EvalResult(precision, recall, 2 * recall * precision / (recall + precision));
        }
    }

    public static EvalResult[] evaluate(int topN) throws ParserConfigurationException, SAXException, IOException {
        Configuration conf = ExtractConf.create();
        TextRankExtractor positionWeightedExtractor = new TextRankExtractor(WeightedRank);

        TextRankExtractor ningExtractor = new TextRankExtractor(NingJianfei);

        TextRankExtractor clusterWeightedExtractor = new TextRankExtractor(ClusterRank);

        Word2VecKMeansExtractor word2vecExtractor = new Word2VecKMeansExtractor(conf);

        XmlArticleReader reader = new XmlArticleReader();
        File f = new File("data/articles.xml");

        reader.open(f);
        int articles = 0;
        EvalResult positionWeightedResult = new EvalResult(0, 0, 0);
        EvalResult ningResult = new EvalResult(0, 0, 0);
        EvalResult clusterWeightedResult = new EvalResult(0, 0, 0);

        EvalResult word2vecResult = new EvalResult(0, 0, 0);
        while (reader.hasNext()) {
            XmlArticleReader.Article article = reader.next();
            System.out.println("Process " + article.id + "...");
            articles++;
            //int  topN = 5; //article.tags.size();
            List<String> keywords1 = positionWeightedExtractor.extractAsList(article.title, article.content, topN);
            EvalResult evalResult = evaluate(article.tags, keywords1);
            positionWeightedResult.add(evalResult);

            List<String> keywords2 = ningExtractor.extractAsList(article.title, article.content, topN);
            evalResult = evaluate(article.tags, keywords2);
            ningResult.add(evalResult);

            List<String> keywords3 = clusterWeightedExtractor.extractAsList(article.title, article.content, topN);
            evalResult = evaluate(article.tags, keywords3);
            clusterWeightedResult.add(evalResult);

            List<String> keywords4 = word2vecExtractor.extractAsList(article.title, article.content, topN);
            evalResult = evaluate(article.tags, keywords4);
            word2vecResult.add(evalResult);

            //输出在聚类方式中成功，但在textrank方式中不存在的词语
//            StringBuilder clusterMatched = new StringBuilder();
//            for (String kw : keywords3) {
//                if(article.tags.contains(kw) && !keywords1.contains(kw) && !keywords2.contains(kw)) {
//                    clusterMatched.append(kw).append(" ");
//                    System.out.println("\t\tTags:\t " + article.tags);
//                    System.out.println("\t\tMixIn:\t " + keywords1);
//                    System.out.println("\t\tNot MixIn:\t " + keywords2);
//                    System.out.println("\t\tCluster:\t " + keywords3);
//                }
//            }
//            if(clusterMatched.length()>0) {
//                System.out.println(article.id + "\t" + article.title + "\t" + clusterMatched.toString());
//            }
        }
        positionWeightedResult.done(articles).setLabel("词语位置加权方法");
        ningResult.done(articles).setLabel("宁建飞方法");
        clusterWeightedResult.done(articles).setLabel("词向量聚类加权方法");
        word2vecResult.done(articles).setLabel("Word2Vec聚类方法");

        return new EvalResult[]{clusterWeightedResult, positionWeightedResult, ningResult, word2vecResult};
    }


    /**
     * 从文章数据集合中抽取出标签词作为新词加入分词库,之后分词程序在启动时,自动把该文件加入到自定义词典
     * 中, 以剔除分词错误.
     *
     * @param xmlArticleFile
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void generateTagWords(File xmlArticleFile) throws ParserConfigurationException, SAXException, IOException {
        System.out.println("从文章数据集中抽取出标签词作为新词加入分词库,以剔除分词错误.");

        XmlArticleReader reader = new XmlArticleReader();
        reader.open(xmlArticleFile);
        Set<String> tagWords = new HashSet<>();
        while (reader.hasNext()) {
            List<String> tags = reader.next().getTags();
            tagWords.addAll(tags);
        }

        File tagWordsFile = new File("src/main/resources/new_tag_words.dic.gz");
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(tagWordsFile));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(gzipOutputStream, Charsets.UTF_8));
        for (String word : tagWords) {
            writer.println(word);
        }
        writer.close();
        gzipOutputStream.close();
        System.out.println("新词保存到:" + tagWordsFile.getAbsolutePath());
    }

    /**
     * 该方法不再使用，请用Keyword.scala中的evaluate进行测试
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ParseException {
        for(EvalResult result: evaluate(5)) {
            System.out.println(result);
        }

        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        String formatString = "Usage";

        options.addOption(new Option("gtw", false, "根据标签词语生成新词文件"));
        options.addOption(new Option("eval", false, "遍历保存到XML中的测试文章,进行关键词抽取测试"));
        options.addOption(new Option("id", true, "对指定Id的文章进行关键词抽取，输出排序列表"));
        options.addOption("h", "help", false, "print help for the command.");

        CommandLine cmdLine = parser.parse(options, args);
        if (cmdLine.hasOption("h") || cmdLine.getOptions().length==0) {
            helpFormatter.printHelp(formatString, options);
            return;
        }

        if (cmdLine.hasOption("gtw")) {
            generateTagWords(new File("data/articles.xml"));
            return;
        } else if(cmdLine.hasOption("eval")) {
            evaluate(5);
        } else if (cmdLine.hasOption("id")) {
            int id = Integer.parseInt(cmdLine.getOptionValue("id"));
            Configuration conf = new Configuration();
            TextRankExtractor extractor = new TextRankExtractor(TextRankExtractor.GraphType.WeightedRank);

            Configuration conf2 = new Configuration();
            conf2.set("extractor.keyword.model", "position");
            TextRankExtractor extractor2 = new TextRankExtractor(TextRankExtractor.GraphType.ClusterRank);

            XmlArticleReader reader = new XmlArticleReader();
            File f = new File("data/articles.xml");

            reader.open(f);

            while (reader.hasNext()) {
                XmlArticleReader.Article article = reader.next();
                if (article.id == id) {
                    List<String> keywords = extractor.extractAsList(article.title, article.content, 1000);
                    List<String> keywords2 = extractor2.extractAsList(article.title, article.content, 1000);
                    System.out.println(article.id + "\t" + article.title + "\t" + article.tags);
                    for (int i=0; i<keywords.size(); i++) {
                        System.out.println(i + "\t" + keywords.get(i) + "\t" + keywords2.get(i));
                    }
                    break;
                }

            }
        }
    }
}
