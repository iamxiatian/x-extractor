package ruc.irm.xextractor.keyword.graph;

/**
 * PageRank的节点表示和计算
 * <p/>
 * User: xiatian
 * Date: 3/10/13 1:26 PM
 */
public class PageRankGraph {
    /**
     * 节点的标签
     */
    public final String[] labels;

    //初始向量
    public double[] V;

    //转移矩阵
    public final double[][] MATRIX;

    public PageRankGraph(String[] labels, double[] v, double[][] MATRIX) {
        this.labels = labels;
        V = v;
        this.MATRIX = MATRIX;
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
     * @param taxRatio      抽税的百分比，一般取值为0.15
     */
    public void iterateCalculation(int iterateCount, double taxRatio) {
        double[] copyV = new double[V.length];

        int iterators = 0;
        while (iterators++ < iterateCount) {
            for (int i = 0; i < V.length; i++) {
                copyV[i] = 0;

                for (int j = 0; j < V.length; j++) {
                    copyV[i] += MATRIX[i][j] * V[j];
                }
                copyV[i] = (1 - taxRatio) * copyV[i] + taxRatio / V.length;
            }

            double[] temp = V;
            V = copyV;
            copyV = temp;
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
