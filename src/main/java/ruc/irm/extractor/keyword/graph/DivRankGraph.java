package ruc.irm.extractor.keyword.graph;

/**
 * DivRank: the Interplay of Prestige and Diversity in Information Networks
 * <p/>
 * <p>
 * User: xiatian
 * Date: 3/10/13 1:26 PM
 */
public class DivRankGraph {
    /**
     * 节点的标签
     */
    public final String[] labels;

    //初始向量
    public double[] V;

    //personalized PageRank： 每个节点的偏好度，当为均匀分配时，退化为默认的PageRank
    public double[] distributionOnV;

    //转移矩阵， 第i列表示节点i指向其它节点的权重分配。即元素(i,j)表示节点j指向节点i的权重
    public final double[][] MATRIX;

    public DivRankGraph(String[] labels,
                        double[] distributionOnV,
                        double[][] MATRIX) {
        this.labels = labels;
        this.distributionOnV = distributionOnV;
        this.MATRIX = MATRIX;

        this.V = new double[labels.length];
        double initValue = 1.0 / this.labels.length;
        for (int i = 0; i < V.length; i++)
            this.V[i] = initValue;
    }

    private double edgeWeight(int from, int to) {
        return MATRIX[to][from];
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

    /**
     * 点态估计当前时刻的转移概率
     *
     * @param from
     * @param to
     * @return
     */
    private double dynamicEdgeWeight(int from, int to) {
        double lambda = 0.85;
        double p0 = edgeWeight(from, to);
        double D = DT(from);

        return (1 - lambda) * distributionOnV[to] + lambda * p0 * V[to] / D;
    }

    public String printMatrix() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < MATRIX.length; i++) {
            sb.append(i).append("\t").append(labels[i]).append("\t\t[");
            for (int j = 0; j < MATRIX.length && j < 10; j++) {
                sb.append(MATRIX[i][j]).append("\t");
            }
            sb.append("]\n");
        }

        return sb.toString();
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

    public void quickSort() {
        shuffle(); // to guard against worst-case
        quickSort(0, V.length - 1);
    }

    ////////////////////////////////////////////////////////////
    //  以下为快速排序算法
    ////////////////////////////////////////////////////////////

    // quickSort a[left] to a[right]
    public void quickSort(int left, int right) {
        if (right <= left)
            return;
        int i = partition(left, right);
        quickSort(left, i - 1);
        quickSort(i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private int partition(int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(V[++i], V[right]))
                // find item on left to swap
                ; // a[right] acts as sentinel
            while (less(V[right], V[--j]))
                // find item on right to swap
                if (j == left)
                    break; // don't go out-of-bounds
            if (i >= j)
                break; // check if pointers cross
            exchange(i, j); // swap two elements into place
        }
        exchange(i, right); // swap with partition element
        return i;
    }

    // is x < y ?       //为了从大到小排列，重新调整了less函数
    public boolean less(Number x, Number y) {
        return x.doubleValue() > y.doubleValue();
    }

    // exchange a[i] and a[j]
    private void exchange(int i, int j) {
        double swap = V[i];
        V[i] = V[j];
        V[j] = swap;

        String swap2 = labels[i];
        labels[i] = labels[j];
        labels[j] = swap2;
    }

    // shuffle the array a[]
    private void shuffle() {
        int N = V.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N - i)); // between i and N-1
            exchange(i, r);
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
