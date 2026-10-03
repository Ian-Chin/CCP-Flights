package airport;

final class Gate {

    private final int id;
    private Plane occupant;

    Gate(int id) {
        this.id = id;
    }

    String name() {
        return "Gate-" + id;
    }

    boolean isFree() {
        return occupant == null;
    }

    Plane occupant() {
        return occupant;
    }

    void assign(Plane plane) {
        occupant = plane;
    }

    void release() {
        occupant = null;
    }
}
