package ruc.irm.extractor.commons;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 中文停用词表
 *
 * @author Tian Xia
 * @date Apr 29, 2016 17:53
 */
public class ChineseStopKeywords {
    private static Set<String> stopKeywords = null;
    private ChineseStopKeywords() {

    }

    private static Set<String> getStopKeywords() {
        if (stopKeywords == null) {
            try {
                stopKeywords = new HashSet<>();
                stopKeywords.addAll(Resources.readLines(Resources.getResource("stoplists/cn_keywords.txt"), Charsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stopKeywords;
    }

    public static boolean isStopKeyword(String word) {
        return ChineseStopWords.isStopWord(word) || getStopKeywords().contains(word);
    }
}
