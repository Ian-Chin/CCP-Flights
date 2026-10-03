package airport;

final class Request {

    enum Type { LANDING, TAKEOFF }

    final Plane plane;
    final Type type;
    Gate gate;
    boolean granted;

    Request(Plane plane, Type type) {
        this.plane = plane;
        this.type = type;
    }
}
