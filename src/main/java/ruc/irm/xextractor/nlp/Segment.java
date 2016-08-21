package ruc.irm.xextractor.nlp;

import com.google.common.base.Joiner;
import org.zhinang.conf.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 分词和词性标记的接口，目前有两类实现：复旦大学分词程序和Stanford NLP
 *
 * @author: xiatian
 * @date: 4/26/12
 */
public interface Segment {

    public void setConfiguration(Configuration configuration);

    /**
     * 切分句子，切分结果按照词语存入List中
     *
     * @param sentence
     * @return
     */
    public List<String> segment(String sentence) ;

    public default String segmentAsString(String sentence) {
        return Joiner.on(" ").join(segment(sentence));
    }

    /**
     * 切分句子并进行词性标记
     *
     * @param sentence
     * @return
     * @throws SegmentException
     */
    public List<SegWord> tag(String sentence);

    public default String tagAsString(String sentence){
        StringBuilder sb = new StringBuilder();
        for (SegWord word : tag(sentence)) {
            sb.append(word.word + "/" + word.pos + " ");
        }
        return sb.toString();
    }

    /**
     * 插入一条用户自定义词语
     *
     * @param word
     * @param pos
     * @param freq
     */
    public void insertUserDefinedWord(String word, String pos, int freq);

    public Entities findEntities(String sentence, boolean allowDuplicated);

    public static final class Entities {
        private Collection<String> persons = null;

        private Collection<String> organizations = null;

        private Collection<String> spaces = null;

        public Entities(boolean allowDuplicated) {
            if (allowDuplicated) {
                persons = new ArrayList<String>();
                organizations = new ArrayList<String>();
                spaces = new ArrayList<String>();
            } else {
                persons = new HashSet<String>();
                organizations = new HashSet<String>();
                spaces = new HashSet<String>();
            }
        }

        public Collection<String> getPersons() {
            return persons;
        }

        public Collection<String> getOrganizations() {
            return organizations;
        }

        public Collection<String> getSpaces() {
            return spaces;
        }

        public void addPerson(String person) {
            persons.add(person);
        }

        public void addOrganization(String organization) {
            organizations.add(organization);
        }

        public void addSpace(String space) {
            spaces.add(space);
        }

        @Override
        public String toString() {
            return "Entities{" +
                    "persons=" + persons +
                    ", organizations=" + organizations +
                    ", spaces=" + spaces +
                    '}';
        }
    }

}
