package airport;

import java.util.LinkedHashMap;
import java.util.Map;

final class Statistics {

    private final Map<String, Long> landingWaits = new LinkedHashMap<>();
    private final Map<String, Long> takeoffWaits = new LinkedHashMap<>();
    private int planesServed;
    private int passengersDisembarked;
    private int passengersBoarded;

    synchronized void recordLandingWait(Plane plane, long millis) {
        landingWaits.put(plane.name(), millis);
    }

    synchronized void recordTakeoffWait(Plane plane, long millis) {
        takeoffWaits.put(plane.name(), millis);
    }

    synchronized void planeServed() {
        planesServed++;
    }

    synchronized void addDisembarked(int count) {
        passengersDisembarked += count;
    }

    synchronized void addBoarded(int count) {
        passengersBoarded += count;
    }

    synchronized Map<String, Long> landingWaits() {
        return new LinkedHashMap<>(landingWaits);
    }

    synchronized Map<String, Long> takeoffWaits() {
        return new LinkedHashMap<>(takeoffWaits);
    }

    synchronized int planesServed() {
        return planesServed;
    }

    synchronized int passengersDisembarked() {
        return passengersDisembarked;
    }

    synchronized int passengersBoarded() {
        return passengersBoarded;
    }
}
