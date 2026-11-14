package quarry.delay;

import quarry.Truck;
import java.util.Random;

public class ExpDelay extends Delay {
    private final Random random;

    public ExpDelay(double for20, double for50, Random random) {
        super(for20, for50);
        this.random = random;
    }

    @Override
    public double get(Truck truck) {
        double r = 1 - this.random.nextDouble();
        return -this.valueFor(truck) * Math.log(r);
    }
}
