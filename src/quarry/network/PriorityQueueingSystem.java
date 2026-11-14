package quarry.network;

import quarry.Truck;
import quarry.delay.Delay;

import java.util.function.ToIntFunction;

public class PriorityQueueingSystem extends QueueingSystem {
    private final ToIntFunction<Truck> priority;

    public PriorityQueueingSystem(
            String name,
            int channels,
            Delay delay,
            ToIntFunction<Truck> priority
    ) {
        super(name, channels, delay);
        this.priority = priority;
    }

    @Override
    protected void enqueue(Truck truck) {
        int priority = this.priority.applyAsInt(truck);
        int i = 0;

        while (i < this.queue.size() && this.priority.applyAsInt(this.queue.get(i)) >= priority) {
            i++;
        }

        this.queue.add(i, truck);
    }
}
