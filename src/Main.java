import chart.Histogram;
import chart.LineChart;
import quarry.model.Inputs;
import quarry.model.Model;
import quarry.model.Protocol;
import quarry.model.Results;
import quarry.statistics.Statistics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

// verification
static final double T_MOD = 10000;
static final double T_0 = 1000;
static final int RUNS = 100;

// tactical planning
static final int PILOT_RUNS = 4;
static final double EPSILON = 0.01;

// strategic planning
static final double STRATEGIC_T_MOD = 1000000;
static final int STRATEGIC_RUNS = 3;
static final double LEVEL = 0.2;
static final int[][] PLAN = {
        {-1, -1, -1},
        {+1, -1, -1},
        {-1, +1, -1},
        {+1, +1, -1},
        {-1, -1, +1},
        {+1, -1, +1},
        {-1, +1, +1},
        {+1, +1, +1}
};

// regression analysis
static final int[][] TERMS = { {}, {0}, {1}, {2}, {0, 1}, {0, 2}, {1, 2}, {0, 1, 2} };
static final String[] NAMES = {"b0", "b1", "b2", "b3", "b12", "b13", "b23", "b123"};
static final double COCHRAN = 0.5157;
static final double STUDENT = 2.12;
static final double FISHER = 19.43;

void main() throws IOException {
    this.printProtocol();
    this.verify();
    this.estimate();
    this.tacticalPlanning();
    this.factorialExperiment();
}

void printProtocol() {
    System.out.println("Протокол подій");
    new Model(Inputs.BASE, 1).simulate(60, 0, new Protocol(System.out));
}

void verify() {
    List<Inputs> experiments = List.of(
            Inputs.BASE,
            new Inputs(10, 10, 2.5, 3, 5, 4, 1.5, 2),
            new Inputs(5, 10, 10, 3, 5, 4, 1.5, 2),
            new Inputs(5, 10, 2.5, 3, 10, 4, 1.5, 2),
            new Inputs(5, 10, 2.5, 3, 5, 4, 6, 2),
            new Inputs(0.001, 0.001, 2.5, 3, 5, 4, 1.5, 2),
            new Inputs(5, 10, 2.5, 3, 0.001, 0.001, 1.5, 2)
    );

    System.out.println("Верифікація, середні значення " + this.outputs().keySet());

    for (int i = 0; i < experiments.size(); i++) {
        List<Results> series = this.runSeries(experiments.get(i), RUNS, T_MOD);
        List<Double> means = new ArrayList<>();

        for (ToDoubleFunction<Results> output : this.outputs().values()) {
            means.add(new Statistics(this.outputValues(series, output)).getMean());
        }

        System.out.println("Експеримент " + (i + 1) + ": " + means);
    }
}

void estimate() throws IOException {
    List<Results> series = this.runSeries(Inputs.BASE, RUNS, T_MOD);
    int bins = (int) Math.ceil(1 + Math.log(RUNS) / Math.log(2));

    System.out.println("Статистичне оцінювання: середнє, СКВ, мінімум, максимум, частоти");

    for (Map.Entry<String, ToDoubleFunction<Results>> output : this.outputs().entrySet()) {
        Statistics statistics = new Statistics(this.outputValues(series, output.getValue()));
        int[] counts = statistics.histogram(bins);

        System.out.println(output.getKey() + ": "
                + statistics.getMean() + ", "
                + statistics.getStd() + ", "
                + statistics.getMin() + ", "
                + statistics.getMax() + ", "
                + Arrays.toString(counts));

        new Histogram(output.getKey(), statistics.getMin(), statistics.getMax(), counts)
                .save("charts/histogram_" + output.getKey() + ".png");
    }
}

void tacticalPlanning() throws IOException {
    for (Map.Entry<String, ToDoubleFunction<Results>> response : this.responses().entrySet()) {
        double[][] start = this.runningMeans(response.getValue(), 10, 5000);
        double[][] whole = this.runningMeans(response.getValue(), 1000, 1000000);

        Statistics last = new Statistics(this.lastValues(whole));
        double epsilon = EPSILON * last.getMean();
        double runs = 20 * Math.pow(last.getStd() / epsilon, 2);

        System.out.println(response.getKey()
                + ": T = " + this.convergenceTime(whole, 1000)
                + ", σ = " + last.getStd()
                + ", ε = " + epsilon
                + ", p = " + runs);

        new LineChart("Час, хв", response.getKey(), 10, start)
                .save("charts/start_" + response.getKey() + ".png");
        new LineChart("Час, хв", response.getKey(), 1000, whole)
                .save("charts/whole_" + response.getKey() + ".png");
    }
}

void factorialExperiment() {
    List<ToDoubleFunction<Results>> responses = new ArrayList<>(this.responses().values());
    double[][] means = new double[responses.size()][PLAN.length];
    double[][] variances = new double[responses.size()][PLAN.length];

    for (int j = 0; j < PLAN.length; j++) {
        List<Results> series = this.runSeries(this.levels(PLAN[j]), STRATEGIC_RUNS, STRATEGIC_T_MOD);

        for (int r = 0; r < responses.size(); r++) {
            Statistics statistics = new Statistics(this.outputValues(series, responses.get(r)));
            means[r][j] = statistics.getMean();
            variances[r][j] = statistics.getStd() * statistics.getStd();
        }

        System.out.println("Експеримент " + (j + 1) + " " + Arrays.toString(PLAN[j])
                + ": y1 = " + means[0][j] + ", D1 = " + variances[0][j]
                + ", y2 = " + means[1][j] + ", D2 = " + variances[1][j]);
    }

    // regression analysis
    for (int r = 0; r < responses.size(); r++) {
        this.regression(means[r], variances[r]);
    }
}

// HELPERS
private List<Results> runSeries(Inputs inputs, int runs, double tMod) {
    List<Results> series = new ArrayList<>();
    Protocol withoutProtocol = new Protocol();

    for (int run = 1; run <= runs; run++) {
        Model model = new Model(inputs, run);
        series.add(model.simulate(tMod, T_0, withoutProtocol));
    }

    return series;
}

private Map<String, ToDoubleFunction<Results>> outputs() {
    Map<String, ToDoubleFunction<Results>> outputs = new LinkedHashMap<>();

    outputs.put("Середня кількість вантажівок біля екскаватора 1", results -> results.getMeanTrucks(1));
    outputs.put("Середня кількість вантажівок біля екскаватора 2", results -> results.getMeanTrucks(2));
    outputs.put("Середня кількість вантажівок біля екскаватора 3", results -> results.getMeanTrucks(3));
    outputs.put("Середня кількість вантажівок біля дробівки", results -> results.getMeanTrucks(5));
    outputs.put("Коефіцієнт завантаження екскаватора 1", results -> results.getUtilization(1));
    outputs.put("Коефіцієнт завантаження екскаватора 2", results -> results.getUtilization(2));
    outputs.put("Коефіцієнт завантаження екскаватора 3", results -> results.getUtilization(3));
    outputs.put("Коефіцієнт завантаження дробівки", results -> results.getUtilization(5));

    return outputs;
}

private double[] outputValues(List<Results> series, ToDoubleFunction<Results> output) {
    double[] values = new double[series.size()];

    for (int i = 0; i < series.size(); i++) {
        values[i] = output.applyAsDouble(series.get(i));
    }

    return values;
}

private double[][] runningMeans(ToDoubleFunction<Results> response, double step, double tMax) {
    int points = (int) (tMax / step);
    double[][] means = new double[PILOT_RUNS][points];
    Protocol withoutProtocol = new Protocol();

    for (int run = 0; run < PILOT_RUNS; run++) {
        Model model = new Model(Inputs.BASE, run + 1);

        for (int k = 0; k < points; k++) {
            means[run][k] = response.applyAsDouble(model.simulate((k + 1) * step, 0, withoutProtocol));
        }
    }

    return means;
}

private double convergenceTime(double[][] means, double step) {
    int last = -1;

    for (int k = 0; k < means[0].length; k++) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double mean = 0;

        for (double[] run : means) {
            min = Math.min(min, run[k]);
            max = Math.max(max, run[k]);
            mean += run[k] / means.length;
        }

        if (max - min >= EPSILON * mean) {
            last = k;
        }
    }

    return (last + 1 + 1) * step;
}

private double[] lastValues(double[][] means) {
    double[] values = new double[means.length];

    for (int run = 0; run < means.length; run++) {
        values[run] = means[run][means[run].length - 1];
    }

    return values;
}

private Map<String, ToDoubleFunction<Results>> responses() {
    Map<String, ToDoubleFunction<Results>> responses = new LinkedHashMap<>();

    responses.put("Вантажівки біля дробівки", results -> results.getMeanTrucks(5));
    responses.put("Завантаження екскаваторів",
            results -> (results.getUtilization(1) + results.getUtilization(2) + results.getUtilization(3)) / 3);

    return responses;
}

private Inputs levels(int[] x) {
    double loading = 1 + LEVEL * x[0];
    double unloading = 1 + LEVEL * x[1];
    double road = 1 + LEVEL * x[2];
    Inputs base = Inputs.BASE;

    return new Inputs(
            base.getLoad20() * loading, base.getLoad50() * loading,
            base.getTravel20() * road, base.getTravel50() * road,
            base.getUnload20() * unloading, base.getUnload50() * unloading,
            base.getReturn20() * road, base.getReturn50() * road
    );
}

private void regression(double[] means, double[] variances) {
    double[] b = this.coefficients(means);
    double noise = new Statistics(variances).getMean();

    // Cochran
    System.out.println("G = " + this.cochran(variances) + ", критичне " + COCHRAN + ", <D> = " + noise);

    // Student
    for (int k = 0; k < b.length; k++) {
        double t = Math.abs(b[k]) * Math.sqrt(PLAN.length * STRATEGIC_RUNS / noise);

        System.out.println(NAMES[k] + " = " + b[k] + ", t = " + t + (t < STUDENT ? " – незначущий" : ""));

        if (t < STUDENT) {
            b[k] = 0;
        }
    }

    // Fisher
    int significant = 0;
    double misfit = 0;

    for (double coefficient : b) {
        if (coefficient != 0) {
            significant++;
        }
    }

    for (int j = 0; j < PLAN.length; j++) {
        double predicted = 0;

        for (int k = 0; k < b.length; k++) {
            predicted += b[k] * this.term(PLAN[j], TERMS[k]);
        }

        misfit += Math.pow(means[j] - predicted, 2);
    }

    System.out.println("F = " + misfit / (PLAN.length - significant) / noise + ", критичне " + FISHER);
}

private int term(int[] x, int[] factors) {
    int value = 1;

    for (int factor : factors) {
        value *= x[factor];
    }

    return value;
}

private double[] coefficients(double[] means) {
    double[] b = new double[TERMS.length];

    for (int k = 0; k < TERMS.length; k++) {
        for (int j = 0; j < PLAN.length; j++) {
            b[k] += this.term(PLAN[j], TERMS[k]) * means[j] / PLAN.length;
        }
    }

    return b;
}

private double cochran(double[] variances) {
    double max = 0;
    double sum = 0;

    for (double variance : variances) {
        max = Math.max(max, variance);
        sum += variance;
    }

    return max / sum;
}
