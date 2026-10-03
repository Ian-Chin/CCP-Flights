package airport;

final class Plane extends Thread {

    private final int id;
    private final boolean emergency;
    private final ControlTower tower;
    private final RefuelTruck truck;
    private final Statistics stats;
    private final int arrivingPassengers;
    private final int departingPassengers;

    Plane(int id, boolean emergency, ControlTower tower, RefuelTruck truck, Statistics stats,
            int arrivingPassengers, int departingPassengers) {
        super("Thread-Plane-" + id);
        this.id = id;
        this.emergency = emergency;
        this.tower = tower;
        this.truck = truck;
        this.stats = stats;
        this.arrivingPassengers = arrivingPassengers;
        this.departingPassengers = departingPassengers;
    }

    String name() {
        return "Plane-" + id;
    }

    boolean isEmergency() {
        return emergency;
    }

    @Override
    public void run() {
        try {
            Gate gate = land();
            Turnaround turnaround = dockAndService(gate);
            leaveGate(turnaround.gate());
            takeOff();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            say("Interrupted, simulation aborted.");
        }
    }

    private Gate land() throws InterruptedException {
        if (emergency) {
            say("MAYDAY! Fuel shortage. Requesting EMERGENCY Landing.");
        } else {
            say("Requesting Landing.");
        }
        long requestedAt = System.currentTimeMillis();
        Gate gate = tower.requestLanding(this);
        stats.recordLandingWait(this, System.currentTimeMillis() - requestedAt);

        say("Landing.");
        Thread.sleep(Config.LANDING_MS);
        say("Landed.");
        tower.vacateRunway();

        say("Coasting to " + gate.name() + ".");
        Thread.sleep(Config.TAXI_MS + Config.DOCKING_MS);
        say("Docked at " + gate.name() + ".");
        return gate;
    }

    private Turnaround dockAndService(Gate gate) throws InterruptedException {
        Turnaround turnaround = new Turnaround(this, gate);
        new PassengerGroup(turnaround, stats, arrivingPassengers, departingPassengers).start();
        new CleaningCrew(turnaround).start();
        say("Requesting refuel truck at " + gate.name() + ".");
        truck.requestRefuel(turnaround);

        turnaround.awaitReadyForDeparture();
        say("Boarded, cleaned and refuelled. Ready to depart.");
        return turnaround;
    }

    private void leaveGate(Gate gate) throws InterruptedException {
        say("Undocking from " + gate.name() + ".");
        Thread.sleep(Config.DOCKING_MS);
        tower.releaseGate(gate);
        say("Coasting to runway.");
        Thread.sleep(Config.TAXI_MS);
    }

    private void takeOff() throws InterruptedException {
        say("Requesting Taking off.");
        long requestedAt = System.currentTimeMillis();
        tower.requestTakeoff(this);
        stats.recordTakeoffWait(this, System.currentTimeMillis() - requestedAt);

        say("Taking off.");
        Thread.sleep(Config.TAKEOFF_MS);
        say("Airborne. Leaving Asia Pacific Airport airspace with " + departingPassengers + " passengers.");
        stats.planeServed();
        tower.reportDeparted();
    }

    private void say(String message) {
        Logger.log(name() + ": " + message);
    }
}
