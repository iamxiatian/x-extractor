package ruc.irm.xextractor.evaluation;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.cli.*;
import org.xml.sax.SAXException;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.keyword.TextRankKeywordExtractor;
import ruc.irm.xextractor.keyword.Word2VecKeywordExtractor;

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

    private static void evaluate() throws ParserConfigurationException, SAXException, IOException {
        Configuration conf = new Configuration();
        TextRankKeywordExtractor textRankExtractor = new TextRankKeywordExtractor(conf);
        Word2VecKeywordExtractor word2vecExtractor = new Word2VecKeywordExtractor(conf);

        XmlArticleReader reader = new XmlArticleReader();
        File f = new File("data/articles.xml");

        reader.open(f);
        int articles = 0;
        EvalResult textRankResult = new EvalResult(0, 0, 0);
        EvalResult word2vecResult = new EvalResult(0, 0, 0);
        while (reader.hasNext()) {
            XmlArticleReader.Article article = reader.next();
            //if(article.id != 4) continue;
            articles++;
            int  topN = 5; //article.tags.size();
            List<String> keywords = textRankExtractor.extractAsList(article.title, article.content, topN);
            EvalResult evalResult = evaluate(article.tags, keywords);
            textRankResult.add(evalResult);

            keywords = word2vecExtractor.extractAsList(article.title, article.content, topN);
            EvalResult evalResult2 = evaluate(article.tags, keywords);
            word2vecResult.add(evalResult2);

            System.out.println(article.id + "\t" + article.title);
        }
        System.out.println("TextRank Result:\n "
                + "P: " + textRankResult.precision / articles + "\t"
                + "R: " + textRankResult.recall / articles + "\t"
                + "F: " + textRankResult.f / articles);
        System.out.println("Word2Vec Result:\n"
                + "P: " + word2vecResult.precision / articles + "\t"
                + "R: " + word2vecResult.recall / articles + "\t"
                + "F: " + word2vecResult.f / articles);
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

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, ParseException {
        evaluate();

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
            evaluate();
        } else if (cmdLine.hasOption("id")) {
            int id = Integer.parseInt(cmdLine.getOptionValue("id"));
            Configuration conf = new Configuration();
            TextRankKeywordExtractor extractor = new TextRankKeywordExtractor(conf);

            Configuration conf2 = new Configuration();
            conf2.set("extractor.keyword.model", "position");
            TextRankKeywordExtractor extractor2 = new TextRankKeywordExtractor(conf2);

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
