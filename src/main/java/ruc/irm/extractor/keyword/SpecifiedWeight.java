package ruc.irm.extractor.keyword;

import java.util.HashMap;
import java.util.Map;

/**
 * 人工设置的权重，保持了特殊词语的权重信息
 * <p/>
 * User: xiatian
 * Date: 4/2/13 2:44 PM
 */
public class SpecifiedWeight {
    private static Map<String, Float> words = new HashMap<String, Float>();

    public static final void setWordWeight(String word, float weight) {
        words.put(word, weight);
    }

    public static final void removeWord(String word) {
        words.remove(word);
    }

    public static float getWordWeight(String word, float defaultValue) {
        if (words.containsKey(word)) {
            return words.get(word);
        } else {
            return defaultValue;
        }
    }
}
