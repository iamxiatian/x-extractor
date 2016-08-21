package ruc.irm.xextractor.nlp.impl;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhinang.conf.Configuration;
import ruc.irm.xextractor.nlp.SegWord;
import ruc.irm.xextractor.nlp.Segment;

import java.util.ArrayList;
import java.util.List;

/**
 * 切分器的Ansj实现，ansj：<a href="https://github.com/NLPchina/ansj_seg">https://github.com/NLPchina/ansj_seg</a>
 *
 * @Author: <a href="xiat@ruc.edu.cn">Summer XIA</a>
 * @Date: 11/25/12 5:53 PM
 */
public class AnsjSegment implements Segment {
    private static final Logger LOG = LoggerFactory.getLogger(AnsjSegment.class);

    private Configuration conf = null;

    /**
     * if set "entity.find.crf" to true in configuration, then, crf method will be used to find entities.
     * @param conf
     */
    public AnsjSegment(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public List<String> segment(String sentence) {
        List<String> results = new ArrayList<String>();
        Result result = ToAnalysis.parse(sentence);
        for (Term term : result.getTerms()) {
            results.add(term.getName());
        }
        return results;
    }

    @Override
    public List<SegWord> tag(String sentence) {
        Result result = ToAnalysis.parse(sentence);
        List<SegWord> results = new ArrayList<SegWord>();

        for (Term term : result.getTerms()) {
            results.add(new SegWord(term.getName(), term.getNatureStr()));
        }

        return results;
    }

    @Override
    public void insertUserDefinedWord(String word, String pos, int freq) {
        //增加新词
        try {
            UserDefineLibrary.insertWord(word, pos, freq);
        } catch (Exception e) {
            LOG.error("add user defined word error.", e);
        }
    }

    @Override
    public Entities findEntities(String sentence, boolean allowDuplicated) {
        Entities entities = new Entities(allowDuplicated);

        Result result = ToAnalysis.parse(sentence);
        for (Term term : result.getTerms()) {
            if (term.getName().length() < 2) {
                continue;
            }
            if (term.getNatureStr().startsWith("nr")) {
                entities.addPerson(term.getName());
            } else if (term.getNatureStr().startsWith("nt")) {
                entities.addOrganization(term.getName());
            } else if (term.getNatureStr().startsWith("ns")) {
                if (term.getName().endsWith("大学") || term.getName().endsWith("学院")) {
                    entities.addOrganization(term.getName());
                } else {
                    entities.addSpace(term.getName());
                }
            }
        }
        return entities;
    }
}
