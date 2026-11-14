package quarry.network;

import quarry.Truck;

public class Device {
    private Truck truck;
    private double tNext = Double.POSITIVE_INFINITY;

    public boolean isFree() {
        return this.truck == null;
    }

    public void start(Truck truck, double tNext) {
        this.truck = truck;
        this.tNext = tNext;
    }

    public Truck finish() {
        Truck done = truck;
        this.truck = null;

        this.tNext = Double.POSITIVE_INFINITY;

        return done;
    }

    public Truck getTruck() {
        return this.truck;
    }

    public double getTNext() {
        return this.tNext;
    }
}
