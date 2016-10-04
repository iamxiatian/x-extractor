package ruc.irm.xextractor.evaluation;

final class EvalResult {
        public double precision;
        public double recall;
        public double f;

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
    }