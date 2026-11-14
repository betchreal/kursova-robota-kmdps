package quarry.delay;

import quarry.Truck;

public class ConstDelay extends Delay {

    public ConstDelay(double for20, double for50) {
        super(for20, for50);
    }

    @Override
    public double get(Truck truck) {
        return this.valueFor(truck);
    }
}
