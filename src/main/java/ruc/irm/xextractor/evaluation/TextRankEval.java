package ruc.irm.xextractor.evaluation;

import com.google.common.base.Splitter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.commons.ExtractConf;
import ruc.irm.xextractor.keyword.TextRankExtractor;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: xiatian
 * Date: 3/11/13 12:29 AM
 */
@Deprecated
public class TextRankEval {
    private Configuration conf = ExtractConf.create();

    /**
     * 所有人工指定的关键词集合
     */
    private Set<String> keywordBag = new HashSet<String>();
    private int keywordTotal = 0;

    public TextRankEval() {
        //测试指定参数的运算结果
        conf.setFloat("extractor.keyword.alpha", 0.33f);
        conf.setFloat("extractor.keyword.beta", 0.34f);
        conf.setFloat("extractor.keyword.gamma", 0.33f);
        conf.setFloat("extractor.keyword.lambda", 5.0f);
        conf.setBoolean("extractor.keyword.merge.neighbor", true);
    }

    /**
     * 计算一个目录下所有XML文件的准确率抽取的精度
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void evaluateXmlDataSet(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        SaxHandler handler = new SaxHandler();
        saxParser.parse(xmlFile, handler);
        EvalResult result = handler.getResult();
        System.out.println(result.precision + "\t" + result.recall + "\t" + result.f);
        System.out.println(result.precision / handler.getArticleCount() + "\t" + result.recall / handler.getArticleCount() + "\t" + result.f / handler.getArticleCount());
    }

    /**
     * 计算网站提供的关键词和自动抽取的关键词的准确率
     *
     * @param title
     * @param content
     * @return
     */
    private EvalResult evaluate(String title, String content, String tags) {
        // if(!title.contains("柳传志")) return new EvalResult(0,0,0);
        List<String> keywords = Splitter.on(",").splitToList(tags);
        //真正的关键词抽取处理
        TextRankExtractor extractor = new TextRankExtractor(TextRankExtractor.GraphType.WeightedRank);
        int topN = 10;
        List<String> extractedKeywords = extractor.extractAsList(title, content, topN);

        int matched = 0;
        int extractedLabelCount = 0;
        for (String keyword : extractedKeywords) {
            extractedLabelCount++;
            if (keywords.contains(keyword)) {
                matched++;
            }
        }

        System.out.println(title);
        System.out.println("\torigin:" + keywords);
        System.out.println("\textracted:" + extractedKeywords);
        double recall = matched * 1.0 / keywords.size();
        double precision = matched * 1.0 / extractedLabelCount;
        if (matched == 0) {
            return new EvalResult(0, 0, 0.0);
        } else {
            return new EvalResult(precision, recall, 2 * recall * precision / (recall + precision));
        }
    }



    public class SaxHandler extends DefaultHandler {
        private StringBuilder buffer = new StringBuilder();

        private String url;
        private String title;
        private String content;
        private String tags;
        private EvalResult result = null;
        private int articles = 0;

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
                throws SAXException {
            buffer.setLength(0);
        }

        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (qName) {
                case "url":
                    this.url = buffer.toString();
                    break;
                case "title":
                    this.title = buffer.toString();
                    break;
                case "content":
                    this.content = buffer.toString();
                    break;
                case "tags":
                    this.tags = buffer.toString();
                    break;
                case "article":
                    articles++;
                    System.out.println(url);
                    EvalResult r = evaluate(title, content, tags);
                    if (result == null)
                        result = r;
                    else
                        result.add(r);
                    System.out.println();
            }
        }

        public void characters(char ch[], int start, int length)
                throws SAXException {
            buffer.append(new String(ch, start, length));
        }

        public void ignorableWhitespace(char ch[], int start, int length)
                throws SAXException {
        }

        public EvalResult getResult() {
            return this.result;
        }

        public int getArticleCount() {
            return articles;
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        TextRankEval eval = new TextRankEval();
        File xmlFile = new File("data/articles.xml");
        eval.evaluateXmlDataSet(xmlFile);
    }

}
