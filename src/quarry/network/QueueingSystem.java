package quarry.network;

import quarry.Truck;
import quarry.delay.Delay;
import quarry.network.route.Route;

import java.util.ArrayList;
import java.util.List;

public class QueueingSystem {
    private final String name;
    private final Delay delay;
    private final Device[] devices;

    protected final List<Truck> queue = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();

    private double sumQD;
    private double sumD;

    public QueueingSystem(String name, int channels, Delay delay) {
        this.name = name;
        this.delay = delay;

        this.devices = new Device[channels];
        for (int i = 0; i < channels; i++) {
            this.devices[i] = new Device();
        }
    }

    public void addRoute(Route route) {
        this.routes.add(route);
    }

    public double getTNext() {
        double tNext = Double.POSITIVE_INFINITY;

        for (Device device : this.devices) {
            tNext = Math.min(tNext, device.getTNext());
        }

        return tNext;
    }

    public void inAct(Truck truck, double t) {
        for (Device device : this.devices) {
            if (device.isFree()) {
                device.start(truck, t + this.delay.get(truck));
                return;
            }
        }

        this.enqueue(truck);
    }

    protected void enqueue(Truck truck) {
        this.queue.add(truck);
    }

    public Truck outAct(double t) {
        Device device = this.devices[0];

        for (Device d : this.devices) {
            if (d.getTNext() < device.getTNext()) {
                device = d;
            }
        }

        Truck done = device.finish();

        if (!this.queue.isEmpty()) {
            Truck next = this.queue.removeFirst();
            device.start(next, t + this.delay.get(next));
        }

        return done;
    }

    public QueueingSystem nextElement(Truck truck) {
        for (Route route : this.routes) {
            if (!route.isBlocked(truck)) {
                return route.getTarget();
            }
        }

        throw new IllegalStateException("All routes from " + this.name + " are blocked");
    }

    public void doStatistics(double delta) {
        List<Truck> inService = this.getInService();

        this.sumQD += (this.queue.size() + inService.size()) * delta;
        this.sumD += inService.size() * delta;
    }

    public List<Truck> getInService() {
        List<Truck> trucks = new ArrayList<>();

        for (Device device : this.devices) {
            if (!device.isFree()) {
                trucks.add(device.getTruck());
            }
        }

        return trucks;
    }

    public List<Truck> getQueue() {
        return new ArrayList<>(this.queue);
    }

    public String getName() {
        return this.name;
    }

    public double getSumQD() {
        return this.sumQD;
    }

    public double getSumD() {
        return this.sumD;
    }
}
