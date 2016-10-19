package ruc.irm.xextractor.keyword.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 一个词语，包含文本在句子中
 * User: xiatian
 * Date: 3/10/13 3:03 PM
 */
public class WordNode {

    public Set<String> getRightNeighbors() {
        return rightNeighbors;
    }

    public Set<String> getLeftNeighbors() {
        return leftNeighbors;
    }

    /**该节点后面相邻的词语集合 */
    private Set<String> rightNeighbors = new HashSet<>();
    /** 该节点前面相邻的节点集合*/
    private Set<String> leftNeighbors = new HashSet<>();

    /**
     * 词语的名称
     */
    private String name;

    /**
     * 词性
     */
    private String pos;

    /**
     * 词语在文本中出现的数量
     */
    private int count;

    /**
     * 词语的重要性，如果在标题中出现，为λ(λ>1, 默认为5）, ,否则为1
     */
    private double importance = 1;

    /**
     * 当前节点所指向的节点名称及其出现次数
     */
    private Map<String, Integer> adjacentWords = new HashMap<String, Integer>();

    public WordNode() {
    }

    public WordNode(String name, String pos, int count, double importance) {
        this.name = name;
        this.pos = pos;
        this.count = count;
        this.importance = importance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getImportance() {
        return importance;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

    public Map<String, Integer> getAdjacentWords() {
        return adjacentWords;
    }

    public void setAdjacentWords(Map<String, Integer> adjacentWords) {
        this.adjacentWords = adjacentWords;
    }

    public void addAdjacentWord(String word) {
        if (adjacentWords.containsKey(word)) {
            adjacentWords.put(word, adjacentWords.get(word) + 1);
        } else {
            adjacentWords.put(word, 1);
        }

    }

    public void addLeftNeighbor(String word) {
        leftNeighbors.add(word);
    }

    public void addRightNeighbor(String word) {
        rightNeighbors.add(word);
    }

    @Override
    public String toString() {
        return "WordNode{" +
                "name='" + name + '\'' +
                ", features='" + pos + '\'' +
                ", count=" + count +
                ", importance=" + importance +
                '}';
    }
}
