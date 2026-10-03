package airport;

final class Turnaround {

    private final Plane plane;
    private final Gate gate;

    private boolean disembarked;
    private boolean cleaned;
    private boolean refuelled;
    private boolean boarded;

    Turnaround(Plane plane, Gate gate) {
        this.plane = plane;
        this.gate = gate;
    }

    Plane plane() {
        return plane;
    }

    Gate gate() {
        return gate;
    }

    synchronized void markDisembarked() {
        disembarked = true;
        notifyAll();
    }

    synchronized void markCleaned() {
        cleaned = true;
        notifyAll();
    }

    synchronized void markRefuelled() {
        refuelled = true;
        notifyAll();
    }

    synchronized void markBoarded() {
        boarded = true;
        notifyAll();
    }

    synchronized void awaitDisembarked() throws InterruptedException {
        while (!disembarked) {
            wait();
        }
    }

    synchronized void awaitCleaned() throws InterruptedException {
        while (!cleaned) {
            wait();
        }
    }

    synchronized void awaitReadyForDeparture() throws InterruptedException {
        while (!(boarded && cleaned && refuelled)) {
            wait();
        }
    }
}
