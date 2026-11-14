package quarry.model;

public class Inputs {
    public static final Inputs BASE = new Inputs(5, 10, 2.5, 3, 5, 4, 1.5, 2);

    private final double load20;
    private final double load50;
    private final double travel20;
    private final double travel50;
    private final double unload20;
    private final double unload50;
    private final double return20;
    private final double return50;

    public Inputs(
            double load20, double load50,
            double travel20, double travel50,
            double unload20, double unload50,
            double return20, double return50
    ) {
        this.load20 = load20;
        this.load50 = load50;
        this.travel20 = travel20;
        this.travel50 = travel50;
        this.unload20 = unload20;
        this.unload50 = unload50;
        this.return20 = return20;
        this.return50 = return50;
    }

    public double getLoad20() {
        return this.load20;
    }

    public double getLoad50() {
        return this.load50;
    }

    public double getTravel20() {
        return this.travel20;
    }

    public double getTravel50() {
        return this.travel50;
    }

    public double getUnload20() {
        return this.unload20;
    }

    public double getUnload50() {
        return this.unload50;
    }

    public double getReturn20() {
        return this.return20;
    }

    public double getReturn50() {
        return this.return50;
    }
}
