package quarry.model;

import quarry.Truck;
import quarry.delay.ConstDelay;
import quarry.delay.Delay;
import quarry.delay.ExpDelay;
import quarry.network.PriorityQueueingSystem;
import quarry.network.QueueingSystem;
import quarry.network.route.BlockingRoute;
import quarry.network.route.Route;
import java.util.List;
import java.util.Random;

public class Model {
    private static final int N = 6;

    private final List<QueueingSystem> network;
    private double tCurr = 0;

    private Protocol protocol;

    public Model(Inputs inputs, long seed) {
        Random random = new Random(seed);

        Delay loading = new ExpDelay(inputs.getLoad20(), inputs.getLoad50(), random);
        Delay travelling = new ConstDelay(inputs.getTravel20(), inputs.getTravel50());
        Delay unloading = new ExpDelay(inputs.getUnload20(), inputs.getUnload50(), random);
        Delay returning = new ConstDelay(inputs.getReturn20(), inputs.getReturn50());

        QueueingSystem excavator1 = new QueueingSystem("СМО1", 1, loading);
        QueueingSystem excavator2 = new QueueingSystem("СМО2", 1, loading);
        QueueingSystem excavator3 = new QueueingSystem("СМО3", 1, loading);
        QueueingSystem travel = new QueueingSystem("СМО4", N, travelling);
        QueueingSystem crusher = new PriorityQueueingSystem("СМО5", 1, unloading, Truck::getCapacity);
        QueueingSystem back = new QueueingSystem("СМО6", N, returning);

        excavator1.addRoute(new Route(travel));
        excavator2.addRoute(new Route(travel));
        excavator3.addRoute(new Route(travel));
        travel.addRoute(new Route(crusher));
        crusher.addRoute(new Route(back));
        back.addRoute(new BlockingRoute(excavator1, truck -> truck.getExcavator() != 1));
        back.addRoute(new BlockingRoute(excavator2, truck -> truck.getExcavator() != 2));
        back.addRoute(new BlockingRoute(excavator3, truck -> truck.getExcavator() != 3));

        this.network = List.of(excavator1, excavator2, excavator3, travel, crusher, back);

        for (int k = 1; k <= 3; k++) {
            QueueingSystem excavator = this.network.get(k - 1);

            excavator.inAct(new Truck(2 * k - 1, 20, k), 0);
            excavator.inAct(new Truck(2 * k, 50, k), 0);
        }
    }

    public Results simulate(double tMod, double t0, Protocol protocol) {
        this.protocol = protocol;
        this.protocol.start(this.network);

        while (this.tCurr < tMod) {
            double tNext = this.findTNext();
            double tEnd = Math.min(tNext, tMod);

            this.doStatistics(tEnd, t0);
            this.tCurr = tEnd;

            if (tNext <= tMod) {
                this.transform();
            }
        }

        return new Results(this.network, tMod - t0);
    }

    private double findTNext() {
        double tNext = Double.POSITIVE_INFINITY;

        for (QueueingSystem system : this.network) {
            tNext = Math.min(tNext, system.getTNext());
        }

        return tNext;
    }

    private void doStatistics(double tEnd, double t0) {
        double from = Math.max(this.tCurr, t0);

        if (tEnd > from) {
            for (QueueingSystem system : this.network) {
                system.doStatistics(tEnd - from);
            }
        }
    }

    private void transform() {
        for (QueueingSystem system : this.network) {
            while (system.getTNext() == this.tCurr) {
                Truck truck = system.outAct(this.tCurr);
                QueueingSystem next = system.nextElement(truck);
                next.inAct(truck, this.tCurr);

                this.protocol.event(this.tCurr, system, next, truck);
            }
        }
    }
}
