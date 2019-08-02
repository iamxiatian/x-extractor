package ruc.irm.extractor.evaluation;

import com.google.common.base.Splitter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取保存在XML中的测试文章数据集, 其格式为:
 * <pre>
 * <articles>
 *     <article>
 *         <url>http://www.example.com/1.html</url>
 *         <title>hello</title>
 *         <tags>hello,world</tags>
 *         <content>hello world.</content>
 *     </article>
 * </articles>
 * </pre>
 *
 * @author Tian Xia
 * @date Aug 21, 2016 18:11
 */
public class XmlArticleReader {
    private int position = 0;
    private int totalArticles = 0;
    private NodeList articleNodeList = null;

    public void open(File articleXmlFile) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(articleXmlFile);
        Element root = document.getDocumentElement();
        this.articleNodeList = root.getElementsByTagName("article");
        this.position = 0;
        this.totalArticles = articleNodeList.getLength();
    }

    public boolean hasNext() {
        //@TODO 为加快测试，只取100篇文章测试
        return position < totalArticles;// && position < 100;
    }

    public Article next() {
        position++;
        Element articleNode = (Element) articleNodeList.item(position - 1);
        String url = articleNode.getElementsByTagName("url").item(0).getTextContent();
        String tags = articleNode.getElementsByTagName("tags").item(0).getTextContent();
        String title = articleNode.getElementsByTagName("title").item(0).getTextContent();
        String content = articleNode.getElementsByTagName("content").item(0).getTextContent();

        //截取一部分
        if (content.length() > 3000)
            return new Article(position - 1, url, title, content.substring(0, 3000), tags);
        else
            return new Article(position - 1, url, title, content, tags);
    }

    public static class Article {
        public int id;
        public String url;
        public String title;
        public String content;
        public List<String> tags = new ArrayList<>();

        public int getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public List<String> getTags() {
            return tags;
        }

        private Article(int id, String url, String title, String content, String tagString) {
            this.id = id;
            this.url = url;
            this.title = title;
            this.content = content;
            this.tags = Splitter.on(",").splitToList(tagString);
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        XmlArticleReader reader = new XmlArticleReader();
        File f = new File("data/articles.xml");
        reader.open(f);

        while (reader.hasNext()) {
            Article article = reader.next();
            System.out.println(article.id + "\t" + article.title + "\t" + article.tags);
        }
    }

}
