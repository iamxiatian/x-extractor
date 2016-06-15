package ruc.irm.xextractor.nlp;

import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.nlp.impl.HanSegment;

public final class SegmentFactory {
    private static Segment segment = null;

    /**
     * 分词处理的参数可以通过conf传递
     *
     * @param conf
     * @return
     */
    public static final Segment getSegment(Configuration conf) {
        return getHanSegment(conf);
    }


    private static final Segment getHanSegment(Configuration conf) {
        if (segment == null) {
            segment = new HanSegment(conf);
        } else {
            segment.setConfiguration(conf);
        }

        return segment;
    }
}
