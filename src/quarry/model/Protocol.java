package quarry.model;

import quarry.Truck;
import quarry.network.QueueingSystem;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

public class Protocol {
    private final PrintStream out;
    private List<QueueingSystem> network;

    public Protocol() {
        this(null);
    }

    public Protocol(PrintStream out) {
        this.out = out;
    }

    public void start(List<QueueingSystem> network) {
        this.network = network;

        if (this.out == null) {
            return;
        }

        StringBuilder header = new StringBuilder(String.format("%8s  %-12s %4s", "t, хв", "Подія", "Ваг."));
        for (QueueingSystem system : this.network) {
            header.append(String.format("  %-13s", system.getName()));
        }
        header.append(String.format("  %11s", "t наст., хв"));

        this.out.println(header);
        this.printLine(0, "Початок", "–");
    }

    public void event(double t, QueueingSystem from, QueueingSystem to, Truck truck) {
        if (this.out == null) {
            return;
        }

        this.printLine(t, from.getName() + " -> " + to.getName(), String.valueOf(truck.getId()));
    }

    private void printLine(double t, String event, String truck) {
        StringBuilder line = new StringBuilder(String.format(Locale.US, "%8.3f  %-12s %4s", t, event, truck));
        double tNext = Double.POSITIVE_INFINITY;

        for (QueueingSystem system : this.network) {
            line.append(String.format("  %-13s", this.state(system)));
            tNext = Math.min(tNext, system.getTNext());
        }
        line.append(String.format(Locale.US, "  %11.3f", tNext));

        this.out.println(line);
    }

    private String state(QueueingSystem system) {
        String state = this.ids(system.getInService());
        List<Truck> queue = system.getQueue();

        if (!queue.isEmpty()) {
            state += " [" + this.ids(queue) + "]";
        }

        return state.isEmpty() ? "–" : state;
    }

    private String ids(List<Truck> trucks) {
        StringBuilder ids = new StringBuilder();

        for (Truck truck : trucks) {
            if (!ids.isEmpty()) {
                ids.append(",");
            }
            ids.append(truck.getId());
        }

        return ids.toString();
    }
}