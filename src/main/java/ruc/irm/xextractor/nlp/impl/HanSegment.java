package ruc.irm.xextractor.nlp.impl;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.nlp.SegWord;
import ruc.irm.xextractor.nlp.Segment;

import java.util.ArrayList;
import java.util.List;

/**
 * HanLP implementation
 *
 * @Author: <a href="xiat@ruc.edu.cn">Summer XIA</a>
 * @Date: 11/25/12 5:53 PM
 */
public class HanSegment implements Segment {
    private static final Logger LOG = LoggerFactory.getLogger(HanSegment.class);

    private Configuration configuration = null;

    /**
     * if set "entity.find.crf" to true in configuration, then, crf method will be used to find entities.
     * @param configuration
     */
    public HanSegment(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public List<String> segment(String sentence) {
        List<Term> terms = HanLP.segment(sentence);
        List<String> results = new ArrayList<String>();
        for (Term term : terms) {
            results.add(term.word);
        }
        return results;
    }

    @Override
    public List<SegWord> tag(String sentence) {
        List<Term> terms = HanLP.segment(sentence);

        List<SegWord> results = new ArrayList<SegWord>();
        for (Term term : terms) {
            results.add(new SegWord(term.word, term.nature.name()));
        }

        return results;
    }

    @Override
    public void insertUserDefinedWord(String word, String pos, int freq) {
        //增加新词
        try {
            CustomDictionary.add(word, pos + " " + freq);
        } catch (Exception e) {
            LOG.error("add user defined word error.", e);
        }
    }

    @Override
    public Entities findEntities(String sentence, boolean allowDuplicated) {
        Entities entities = new Entities(allowDuplicated);

        List<Term> terms = HanLP.segment(sentence);
        for (Term term : terms) {
            if (term.word.length() < 2) {
                continue;
            }
            if (term.nature.name().startsWith("nr")) {
                entities.addPerson(term.word);
            } else if (term.nature.name().startsWith("nt")) {
                entities.addOrganization(term.word);
            } else if (term.nature.name().startsWith("ns")) {
                if (term.word.endsWith("大学") || term.word.endsWith("学院")) {
                    entities.addOrganization(term.word);
                } else {
                    entities.addSpace(term.word);
                }
            }
        }
        return entities;
    }
}
