package ruc.irm.extractor.keyword.divrank;

import ruc.irm.extractor.keyword.RankGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * DivRank: the Interplay of Prestige and Diversity in Information Networks
 * <p/>
 * <p>
 * User: xiatian
 * Date: 3/10/13 1:26 PM
 */
public class DivRankGraph extends RankGraph {


    public DivRankGraph(String[] labels,
                        double[] distributionOnV,
                        double[][] MATRIX) {
        super(labels, distributionOnV, MATRIX);
    }

    /**
     * 计算D_T(u)
     *
     * @param u
     * @return
     */
    private double DT(int u) {
        double sum = 0;
        for (int v = 0; v < V.length; v++) {
            sum += edgeWeight(u, v) * V[v];
        }
        return sum;
    }

    private Map<String, Double> weightCache = new HashMap<>();

    /**
     * 点态估计当前时刻的转移概率
     *
     * @param from
     * @param to
     * @return
     */
    private double dynamicEdgeWeight(int from, int to) {
        String key = from + "-" + to;
        if(weightCache.containsKey(key)) {
            return weightCache.get(key);
        } else {
            double lambda = 0.85;
            double p0 = edgeWeight(from, to);
            double D = DT(from);
            double value = (1 - lambda) * distributionOnV[to] + lambda * p0 * V[to] / D;
            weightCache.put(key, value);
            return value;
        }
    }

    /**
     * 计算PageRank
     *
     * @param iterateCount: 迭代次数
     * @param dumpFactor    阻尼系数，一般取值为0.85
     */
    public void iterateCalculation(int iterateCount, double dumpFactor) {
        double[] nextTimeV = new double[V.length];

        int iterators = 0;
        while (iterators++ < iterateCount) {
            //清空缓存
            weightCache.clear();

            for (int i = 0; i < V.length; i++) {
                double accumulate = 0;
                for (int j = 0; j < V.length; j++) {
                    //accumulate += edgeWeight(j, i) * V[j];
                    accumulate += dynamicEdgeWeight(j, i) * V[j];
                }

                //nextTimeV[i] = (1 - dumpFactor) * distributionOnV[i] + dumpFactor * accumulate;
                nextTimeV[i] = accumulate;
            }

            V = nextTimeV;
        }
    }

    public static void main(String[] args) {
        //大数据、Web挖掘中PageRank的实例计算
        DivRankGraph graph = new DivRankGraph(new String[]{"A", "B", "C", "D"}, new double[]{0.25f, 0.25f, 0.25f, 0.25f}, new double[][]{{0, 0.5f, 0, 0}, {1.0f / 3.0f, 0, 0, 0.5f}, {1.0f / 3.0f, 0, 1, 0.5f}, {1.0f / 3.0f, 0.5f, 0, 0}});
        //PageRankGraph graph = new PageRankGraph(new String[]{"A", "B", "C", "D"}, new float[]{0.25f, 0.25f, 0.25f, 0.25f}, new float[][]{{0, 9.0f/10, 4.0f/5f, 18.0f/20.0f}, {199.0f/347.0f, 0, 0, 1.0f/20.0f}, {98.0f /347.0f, 1.0f/10f, 0, 1.0f/20f}, {49.0f / 347.0f, 0, 1.0f/5.0f, 0}});

        int it = 1;
        while (it++ < 50) {
            graph.iterateCalculation(1, 0.2f);
            System.out.print("[");
            for (double v : graph.V) {
                System.out.print(v + "\t");
            }
            System.out.println("]");
        }
    }

}
