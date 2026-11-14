package quarry.statistics;

public class Statistics {
    private final double[] values;
    private final double mean;
    private final double std;

    public Statistics(double[] values) {
        this.values = values;
        this.mean = this.computeMean();
        this.std = this.computeStd();
    }

    private double computeMean() {
        double sum = 0;

        for (double value : this.values) {
            sum += value;
        }

        return sum / this.values.length;
    }

    private double computeStd() {
        double sum = 0;

        for (double value : this.values) {
            sum += (value - this.mean) * (value - this.mean);
        }

        return Math.sqrt(sum / (this.values.length - 1));
    }

    public int[] histogram(int bins) {
        double min = this.getMin();
        double width = (this.getMax() - min) / bins;
        int[] counts = new int[bins];

        for (double value : this.values) {
            int i = (int) ((value - min) / width);
            counts[Math.min(i, bins - 1)]++;
        }

        return counts;
    }

    public double getMean() {
        return this.mean;
    }

    public double getStd() {
        return this.std;
    }

    public double getMin() {
        double min = Double.POSITIVE_INFINITY;

        for (double value : this.values) {
            min = Math.min(min, value);
        }

        return min;
    }

    public double getMax() {
        double max = Double.NEGATIVE_INFINITY;

        for (double value : this.values) {
            max = Math.max(max, value);
        }

        return max;
    }
}
