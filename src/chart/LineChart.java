package chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class LineChart extends Chart {
    private static final Color[] COLORS = {Color.BLACK, Color.GRAY, Color.DARK_GRAY, Color.LIGHT_GRAY};

    private final double step;
    private final double[][] lines;

    public LineChart(String xLabel, String yLabel, double step, double[][] lines) {
        super(xLabel, yLabel);
        this.step = step;
        this.lines = lines;
    }

    @Override
    protected double getXMin() {
        return 0;
    }

    @Override
    protected double getXMax() {
        return this.step * this.lines[0].length;
    }

    @Override
    protected double getYMin() {
        return Math.floor(this.min() / this.step()) * this.step();
    }

    @Override
    protected double getYMax() {
        return this.getYMin() + Y_TICKS * this.step();
    }

    @Override
    protected void drawData(Graphics2D g) {
        g.setStroke(new BasicStroke(2));



        for (int i = 0; i < this.lines.length; i++) {
            int[] xs = new int[this.lines[i].length];
            int[] ys = new int[this.lines[i].length];

            for (int k = 0; k < this.lines[i].length; k++) {
                xs[k] = this.toX((k + 1) * this.step);
                ys[k] = this.toY(this.lines[i][k]);
            }

            g.setColor(COLORS[i % COLORS.length]);
            g.drawPolyline(xs, ys, this.lines[i].length);
        }
    }

    private double step() {
        double raw = (this.max() - this.min()) / Y_TICKS;
        double power = Math.pow(10, Math.floor(Math.log10(raw)));

        for (double factor : new double[]{1, 2, 2.5, 5, 10}) {
            double step = factor * power;

            if (Math.floor(this.min() / step) * step + Y_TICKS * step >= this.max()) {
                return step;
            }
        }

        return 20 * power;
    }

    private double min() {
        double min = Double.POSITIVE_INFINITY;

        for (double[] line : this.lines) {
            for (double value : line) {
                min = Math.min(min, value);
            }
        }

        return min;
    }

    private double max() {
        double max = Double.NEGATIVE_INFINITY;

        for (double[] line : this.lines) {
            for (double value : line) {
                max = Math.max(max, value);
            }
        }

        return max;
    }
}
