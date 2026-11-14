package quarry.model;

import quarry.network.QueueingSystem;
import java.util.List;

public class Results {
    private final double[] meanTrucks;
    private final double[] utilization;

    public Results(List<QueueingSystem> network, double period) {
        int n = network.size();

        this.meanTrucks = new double[n];
        this.utilization = new double[n];

        for (int i = 0; i < n; i++) {
            this.meanTrucks[i] = network.get(i).getSumQD() / period;
            this.utilization[i] = network.get(i).getSumD() / period;
        }
    }

    public double getMeanTrucks(int number) {
        return this.meanTrucks[number - 1];
    }

    public double getUtilization(int number) {
        return this.utilization[number - 1];
    }

    public double getTotalTrucks() {
        double total = 0;

        for (double value : this.meanTrucks) {
            total += value;
        }

        return total;
    }
}
