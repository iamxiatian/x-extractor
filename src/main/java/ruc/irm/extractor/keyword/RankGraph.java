package ruc.irm.extractor.keyword;

/**
 * 用于迭代计算的图，支持PageRankGraph和DivRankGraph两类
 */
public abstract class RankGraph {
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

    public RankGraph(String[] labels,
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

    protected double edgeWeight(int from, int to) {
        return MATRIX[to][from];
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
     * @param iterateCount 迭代次数
     * @param dampFactor   阻尼系数，一般取值为0.85
     */
    public abstract void iterateCalculation(int iterateCount, double dampFactor);

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
}
