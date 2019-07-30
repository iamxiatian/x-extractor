package ruc.irm.extractor.keyword.graph;

import ruc.irm.extractor.keyword.RankGraph;

/**
 * PageRank的节点表示和计算, 支持PPR(Personal PageRank)，PPR通过distributionOnV体现。
 * <p/>
 * <p>
 * User: xiatian
 * Date: 3/10/13 1:26 PM
 */
public class PageRankGraph extends RankGraph {

    public PageRankGraph(String[] labels,
                         double[] distributionOnV,
                         double[][] MATRIX) {
        super(labels, distributionOnV, MATRIX);
    }

    /**
     * 计算PageRank
     */
    public void iterateCalculation(int iterateCount, double dampFactor) {
        double[] nextTimeV = new double[V.length];

        int iterators = 0;
        while (iterators++ < iterateCount) {
            for (int i = 0; i < V.length; i++) {
                double accumulate = 0;
                for (int j = 0; j < V.length; j++) {
                    accumulate += edgeWeight(j, i) * V[j];
                }

                nextTimeV[i] = (1 - dampFactor) * distributionOnV[i] + dampFactor * accumulate;
            }

            V = nextTimeV;
        }
    }

    public static void main(String[] args) {
        //大数据、Web挖掘中PageRank的实例计算
        PageRankGraph graph = new PageRankGraph(new String[]{"A", "B", "C", "D"}, new double[]{0.25f, 0.25f, 0.25f, 0.25f}, new double[][]{{0, 0.5f, 0, 0}, {1.0f / 3.0f, 0, 0, 0.5f}, {1.0f / 3.0f, 0, 1, 0.5f}, {1.0f / 3.0f, 0.5f, 0, 0}});
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
