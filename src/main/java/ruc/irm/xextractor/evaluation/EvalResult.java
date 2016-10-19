package ruc.irm.xextractor.evaluation;

/**
 * 保存测试结果的P/R/F值
 */
public final class EvalResult {
    public double precision;
    public double recall;
    public double f;

    public EvalResult setLabel(String label) {
        this.label = label;
        return this;
    }

    public String label; //聚类结果的标记，便于区分

    public EvalResult(double precision, double recall, double fvalue) {
        this.precision = precision;
        this.recall = recall;
        this.f = fvalue;
    }

    public void add(EvalResult r) {
        this.precision += r.precision;
        this.recall += r.recall;
        this.f += r.f;
    }

    public double getF() {
        if (precision <= 0 || recall <= 0) return 0;
        return 2 * precision * recall / (precision + recall);
    }

    /**
     * @param articleCount
     */
    public EvalResult done(int articleCount) {
        this.precision /=articleCount;
        this.recall /= articleCount;
        this.f /= articleCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label + ":\t"
                + "P: " + precision + "\t"
                + "R: " + recall + "\t"
                + "F: " + f);

        return sb.toString();
    }
}