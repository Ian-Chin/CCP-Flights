package airport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class ControlTower {

    private final Gate[] gates;
    private final LinkedList<Request> inbox = new LinkedList<>();
    private final LinkedList<Request> landingQueue = new LinkedList<>();
    private final LinkedList<Request> takeoffQueue = new LinkedList<>();

    private boolean runwayBusy;
    private int planesOnGround;
    private int planesDeparted;
    private boolean stateChanged;

    ControlTower(int gateCount) {
        gates = new Gate[gateCount];
        for (int i = 0; i < gateCount; i++) {
            gates[i] = new Gate(i + 1);
        }
    }

    synchronized Gate requestLanding(Plane plane) throws InterruptedException {
        Request request = submit(plane, Request.Type.LANDING);
        while (!request.granted) {
            wait();
        }
        return request.gate;
    }

    synchronized void requestTakeoff(Plane plane) throws InterruptedException {
        Request request = submit(plane, Request.Type.TAKEOFF);
        while (!request.granted) {
            wait();
        }
    }

    synchronized void vacateRunway() {
        runwayBusy = false;
        signalChange();
    }

    synchronized void releaseGate(Gate gate) {
        gate.release();
        signalChange();
    }

    synchronized void reportDeparted() {
        runwayBusy = false;
        planesOnGround--;
        planesDeparted++;
        signalChange();
    }

    synchronized boolean awaitAndDispatch() throws InterruptedException {
        while (inbox.isEmpty() && !stateChanged && !allPlanesDeparted()) {
            wait();
        }
        if (allPlanesDeparted()) {
            return false;
        }
        stateChanged = false;

        List<Request> fresh = new ArrayList<>(inbox);
        inbox.clear();
        for (Request request : fresh) {
            enqueue(request);
        }

        dispatch();

        for (Request request : fresh) {
            if (!request.granted) {
                announceHold(request);
            }
        }
        return true;
    }

    synchronized boolean allGatesEmpty() {
        for (Gate gate : gates) {
            if (!gate.isFree()) {
                return false;
            }
        }
        return true;
    }

    synchronized List<String> gateStatus() {
        List<String> lines = new ArrayList<>();
        for (Gate gate : gates) {
            String state = gate.isFree() ? "EMPTY" : "OCCUPIED by " + gate.occupant().name();
            lines.add(gate.name() + " is " + state);
        }
        return lines;
    }

    synchronized boolean isRunwayFree() {
        return !runwayBusy;
    }

    synchronized int planesOnGround() {
        return planesOnGround;
    }

    synchronized boolean queuesEmpty() {
        return inbox.isEmpty() && landingQueue.isEmpty() && takeoffQueue.isEmpty();
    }

    private Request submit(Plane plane, Request.Type type) {
        Request request = new Request(plane, type);
        inbox.add(request);
        notifyAll();
        return request;
    }

    private void signalChange() {
        stateChanged = true;
        notifyAll();
    }

    private boolean allPlanesDeparted() {
        return planesDeparted == Config.TOTAL_PLANES;
    }

    private void enqueue(Request request) {
        if (request.type == Request.Type.TAKEOFF) {
            takeoffQueue.addLast(request);
            return;
        }
        if (!request.plane.isEmergency()) {
            landingQueue.addLast(request);
            return;
        }

        int position = 0;
        while (position < landingQueue.size() && landingQueue.get(position).plane.isEmergency()) {
            position++;
        }
        List<String> overtaken = new ArrayList<>();
        for (int i = position; i < landingQueue.size(); i++) {
            overtaken.add(landingQueue.get(i).plane.name());
        }
        landingQueue.add(position, request);

        Logger.log("ATC: MAYDAY received from " + request.plane.name() + " (fuel shortage). Emergency priority granted.");
        if (!overtaken.isEmpty()) {
            Logger.log("ATC: " + request.plane.name() + " moved to front of landing queue, ahead of "
                    + String.join(", ", overtaken) + ".");
        }
    }

    private void dispatch() {
        if (runwayBusy) {
            return;
        }

        Request firstLanding = landingQueue.peekFirst();
        boolean canLand = firstLanding != null && planesOnGround < Config.MAX_PLANES_ON_GROUND && freeGate() != null;

        Request next = null;
        if (canLand && firstLanding.plane.isEmergency()) {
            next = landingQueue.removeFirst();
        } else if (!takeoffQueue.isEmpty()) {
            next = takeoffQueue.removeFirst();
        } else if (canLand) {
            next = landingQueue.removeFirst();
        }
        if (next == null) {
            return;
        }

        runwayBusy = true;
        if (next.type == Request.Type.LANDING) {
            Gate gate = freeGate();
            gate.assign(next.plane);
            next.gate = gate;
            planesOnGround++;
            String kind = next.plane.isEmergency() ? "EMERGENCY landing" : "Landing";
            Logger.log("ATC: " + kind + " permission granted for " + next.plane.name() + ".");
            Logger.log("ATC: " + gate.name() + " assigned for " + next.plane.name() + ".");
        } else {
            Logger.log("ATC: Taking-off is granted for " + next.plane.name() + ". Runway is free.");
        }
        next.granted = true;
        notifyAll();
    }

    private void announceHold(Request request) {
        String plane = request.plane.name();
        if (request.type == Request.Type.TAKEOFF) {
            Logger.log("ATC: " + plane + ", hold short of runway. Runway is occupied.");
            return;
        }
        String reason;
        if (planesOnGround >= Config.MAX_PLANES_ON_GROUND || freeGate() == null) {
            reason = "Airport Full";
        } else if (runwayBusy) {
            reason = "Runway occupied";
        } else {
            reason = "other planes ahead in queue";
        }
        int position = landingQueue.indexOf(request) + 1;
        Logger.log("ATC: Landing Permission Denied for " + plane + ", " + reason
                + ". Join holding circle, position " + position + ".");
    }

    private Gate freeGate() {
        for (Gate gate : gates) {
            if (gate.isFree()) {
                return gate;
            }
        }
        return null;
    }
}
