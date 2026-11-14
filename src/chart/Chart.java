package chart;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public abstract class Chart {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 560;
    private static final int LEFT = 120;
    private static final int RIGHT = 60;
    private static final int TOP = 30;
    private static final int BOTTOM = 100;
    private static final int X_TICKS = 4;
    protected static final int Y_TICKS = 5;

    private final String xLabel;
    private final String yLabel;

    protected Chart(String xLabel, String yLabel) {
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    protected abstract double getXMin();

    protected abstract double getXMax();

    protected abstract double getYMin();

    protected abstract double getYMax();

    protected abstract void drawData(Graphics2D g);

    public void save(String path) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font(Font.SERIF, Font.PLAIN, 26));

        this.drawData(g);
        this.drawAxes(g);
        g.dispose();

        File file = new File(path);
        file.getAbsoluteFile().getParentFile().mkdirs();
        ImageIO.write(image, "png", file);
    }

    protected int toX(double x) {
        double share = (x - this.getXMin()) / (this.getXMax() - this.getXMin());
        return LEFT + (int) Math.round(share * (WIDTH - LEFT - RIGHT));
    }

    protected int toY(double y) {
        double share = (y - this.getYMin()) / (this.getYMax() - this.getYMin());
        return HEIGHT - BOTTOM - (int) Math.round(share * (HEIGHT - TOP - BOTTOM));
    }

    private void drawAxes(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(LEFT, HEIGHT - BOTTOM, WIDTH - RIGHT, HEIGHT - BOTTOM);
        g.drawLine(LEFT, TOP, LEFT, HEIGHT - BOTTOM);

        double xStep = (this.getXMax() - this.getXMin()) / X_TICKS;

        for (int i = 0; i <= X_TICKS; i++) {
            double x = this.getXMin() + i * xStep;
            String text = this.format(x, xStep);
            int position = this.toX(x);

            g.drawLine(position, HEIGHT - BOTTOM, position, HEIGHT - BOTTOM + 8);
            g.drawString(text, position - g.getFontMetrics().stringWidth(text) / 2, HEIGHT - BOTTOM + 36);
        }

        double yStep = (this.getYMax() - this.getYMin()) / Y_TICKS;

        for (int i = 0; i <= Y_TICKS; i++) {
            double y = this.getYMin() + i * yStep;
            String text = this.format(y, yStep);
            int position = this.toY(y);

            g.drawLine(LEFT - 8, position, LEFT, position);
            g.drawString(text, LEFT - 14 - g.getFontMetrics().stringWidth(text), position + 9);
        }

        g.drawString(this.xLabel, (LEFT + WIDTH - RIGHT - g.getFontMetrics().stringWidth(this.xLabel)) / 2, HEIGHT - 22);

        AffineTransform transform = g.getTransform();
        g.rotate(-Math.PI / 2);
        g.drawString(this.yLabel, -(TOP + HEIGHT - BOTTOM + g.getFontMetrics().stringWidth(this.yLabel)) / 2, 30);
        g.setTransform(transform);
    }

    private String format(double value, double step) {
        int digits = step >= 1 ? 0 : (int) Math.ceil(-Math.log10(step)) + 1;
        return String.format(Locale.ROOT, "%." + digits + "f", value);
    }
}