package ruc.irm.xextractor.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 切分出的每一个词语
 *
 * @author xiatian
 */
public class SegWord {
    public String word;
    public String pos;

    public SegWord() {
    }

    public SegWord(String word, String pos) {
        this.word = word;
        this.pos = pos;
    }

    public static final List<SegWord> parse(String segedSentence) {
        List<SegWord> list = new ArrayList<SegWord>();
        StringTokenizer tokenizer = new StringTokenizer(segedSentence, " \t\r\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int pos = token.lastIndexOf("/");
            if (pos > 0) {
                list.add(new SegWord(token.substring(0, pos), token.substring(pos + 1)));
            } else {
                list.add(new SegWord(token, "U"));
            }
        }
        return list;
    }

    public String toString() {
        return word + "/" + pos;
    }
}
