package quarry.network.route;

import quarry.Truck;
import quarry.network.QueueingSystem;

import java.util.function.Predicate;

public class BlockingRoute extends Route {
    private final Predicate<Truck> predicate;

    public BlockingRoute(QueueingSystem target, Predicate<Truck> predicate) {
        super(target);
        this.predicate = predicate;
    }

    @Override
    public boolean isBlocked(Truck truck) {
        return predicate.test(truck);
    }
}
