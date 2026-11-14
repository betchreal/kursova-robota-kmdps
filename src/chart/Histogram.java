package chart;

import java.awt.Color;
import java.awt.Graphics2D;

public class Histogram extends Chart {
    private final double min;
    private final double max;
    private final int[] counts;

    public Histogram(String xLabel, double min, double max, int[] counts) {
        super(xLabel, "Частота");
        this.min = min;
        this.max = max;
        this.counts = counts;
    }

    @Override
    protected double getXMin() {
        return this.min;
    }

    @Override
    protected double getXMax() {
        return this.max;
    }

    @Override
    protected double getYMin() {
        return 0;
    }

    @Override
    protected double getYMax() {
        int highest = 0;

        for (int count : this.counts) {
            highest = Math.max(highest, count);
        }

        return 5 * Math.ceil((highest + 1) / 5.0);
    }

    @Override
    protected void drawData(Graphics2D g) {
        double width = (this.max - this.min) / this.counts.length;

        for (int i = 0; i < this.counts.length; i++) {
            int left = this.toX(this.min + i * width);
            int right = this.toX(this.min + (i + 1) * width);
            int top = this.toY(this.counts[i]);
            int bottom = this.toY(0);

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(left, top, right - left, bottom - top);
            g.setColor(Color.BLACK);
            g.drawRect(left, top, right - left, bottom - top);
        }
    }
}
