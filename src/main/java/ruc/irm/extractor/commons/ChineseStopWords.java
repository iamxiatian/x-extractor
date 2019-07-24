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
public class ChineseStopWords {
    private static Set<String> stopWords = null;
    private ChineseStopWords() {

    }

    private static Set<String> getStopWords() {
        if (stopWords == null) {
            try {
                stopWords = new HashSet<>();
                stopWords.addAll(Resources.readLines(Resources.getResource("stoplists/cn.txt"), Charsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stopWords;
    }

    public static boolean isStopWord(String word) {
        return getStopWords().contains(word);
    }
}
