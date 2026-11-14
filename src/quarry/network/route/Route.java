package quarry.network.route;

import quarry.Truck;
import quarry.network.QueueingSystem;

public class Route {
    private final QueueingSystem target;

    public Route(QueueingSystem target) {
        this.target = target;
    }

    public QueueingSystem getTarget() {
        return target;
    }

    public boolean isBlocked(Truck truck) {
        return false;
    }
}
