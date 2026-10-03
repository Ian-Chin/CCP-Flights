package airport;

final class Config {

    static final int TOTAL_PLANES = 6;
    static final int GATE_COUNT = 3;
    static final int MAX_PLANES_ON_GROUND = 3;
    static final int MAX_PASSENGERS = 50;
    static final int EMERGENCY_PLANE_ID = 6;

    static final int LANDING_MS = 1000;
    static final int TAXI_MS = 500;
    static final int DOCKING_MS = 500;
    static final int DISEMBARK_MS = 3000;
    static final int SUPPLIES_MS = 1000;
    static final int CLEANING_MS = 1500;
    static final int REFUEL_MS = 2000;
    static final int EMBARK_MS = 3000;
    static final int TAKEOFF_MS = 1000;

    private Config() {
    }
}
