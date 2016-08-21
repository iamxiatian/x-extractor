package ruc.irm.xextractor.nlp;

import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.nlp.impl.AnsjSegment;
import ruc.irm.xextractor.nlp.impl.HanSegment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public final class SegmentFactory {
    private static Segment hanSegment = null;
    private static Segment ansjSegment = null;
    /**
     * 分词处理的参数可以通过conf传递
     *
     * @param conf
     * @return
     */
    public static final Segment getSegment(Configuration conf) {
        if(conf==null || conf.get("segment.name", "ansj").equalsIgnoreCase("ansj")) {
            return getAnsjSegment(conf);
        } else {
            return getHanSegment(conf);
        }
    }


    private static final Segment getHanSegment(Configuration conf) {
        if (hanSegment == null) {
            hanSegment = new HanSegment(conf);
            try {
                hanSegment.loadUserDefinedWords("/new_wiki_words.dic.gz");
                hanSegment.loadUserDefinedWords("/new_tag_words.dic.gz");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            hanSegment.setConfiguration(conf);
        }

        return hanSegment;
    }


    private static final Segment getAnsjSegment(Configuration conf) {
        if (ansjSegment == null) {
            ansjSegment = new AnsjSegment(conf);
            try {
                ansjSegment.loadUserDefinedWords("/new_wiki_words.dic.gz");
                ansjSegment.loadUserDefinedWords("/new_tag_words.dic.gz");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ansjSegment.setConfiguration(conf);
        }

        return ansjSegment;
    }

}
