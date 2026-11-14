package quarry;

public class Truck {
    private final int id;
    private final int capacity;
    private final int excavator;

    public Truck(int id, int capacity, int excavator) {
        this.id = id;
        this.capacity = capacity;
        this.excavator = excavator;
    }

    public int getId() {
        return this.id;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public int getExcavator() {
        return this.excavator;
    }
}
