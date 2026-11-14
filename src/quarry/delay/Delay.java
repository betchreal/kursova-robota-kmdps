package quarry.delay;

import quarry.Truck;

public abstract class Delay {
    private final double for20;
    private final double for50;

    protected Delay(double for20, double for50) {
        this.for20 = for20;
        this.for50 = for50;
    }

    protected double valueFor(Truck truck) {
        return truck.getCapacity() == 20 ? this.for20 : this.for50;
    }

    public abstract double get(Truck truck);
}
