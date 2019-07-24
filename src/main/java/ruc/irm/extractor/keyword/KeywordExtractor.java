package ruc.irm.extractor.keyword;

import java.util.List;

/**
 * 关键词抽取接口
 *
 * @author Tian Xia
 * @date Oct 06, 2016 19:00
 */
public interface KeywordExtractor {
    public List<String> extractAsList(String title, String content, int topN);

    default public String extractAsString(String title, String content, int topN) {
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
}
