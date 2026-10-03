package airport;

import java.util.Map;

final class AirTrafficController extends Thread {

    private final ControlTower tower;
    private final Statistics stats;
    private final RefuelTruck truck;

    AirTrafficController(ControlTower tower, Statistics stats, RefuelTruck truck) {
        super("Thread-ATC");
        this.tower = tower;
        this.stats = stats;
        this.truck = truck;
    }

    @Override
    public void run() {
        Logger.log("ATC: Tower open. 1 runway, " + Config.GATE_COUNT + " gates, max "
                + Config.MAX_PLANES_ON_GROUND + " planes on the ground.");
        try {
            while (tower.awaitAndDispatch()) {
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        Logger.log("ATC: All " + Config.TOTAL_PLANES + " planes have left the airport.");
        runSanityChecks();
        printStatistics();
    }

    private void runSanityChecks() {
        Logger.log("ATC: ================ SANITY CHECKS ================");
        for (String line : tower.gateStatus()) {
            Logger.log("ATC: " + line);
        }
        boolean gatesOk = check("All gates are empty", tower.allGatesEmpty());
        boolean runwayOk = check("Runway is free", tower.isRunwayFree());
        boolean groundOk = check("Planes on ground = " + tower.planesOnGround(), tower.planesOnGround() == 0);
        boolean queuesOk = check("Landing and take-off queues are empty", tower.queuesEmpty());
        boolean truckOk = check("Refuel truck is idle", truck.isIdle());
        boolean allOk = gatesOk && runwayOk && groundOk && queuesOk && truckOk;
        Logger.log("ATC: Sanity check result: " + (allOk ? "ALL CHECKS PASSED" : "CHECKS FAILED"));
    }

    private boolean check(String label, boolean passed) {
        Logger.log("ATC: [" + (passed ? "PASS" : "FAIL") + "] " + label);
        return passed;
    }

    private void printStatistics() {
        Logger.log("ATC: ================== STATISTICS ==================");
        Map<String, Long> landingWaits = stats.landingWaits();
        for (Map.Entry<String, Long> entry : landingWaits.entrySet()) {
            Logger.log(String.format("ATC: %s waited %.2fs for landing permission.",
                    entry.getKey(), entry.getValue() / 1000.0));
        }
        summarise("Waiting time to land", landingWaits);
        summarise("Waiting time to take off", stats.takeoffWaits());
        Logger.log("ATC: Planes served: " + stats.planesServed() + " / " + Config.TOTAL_PLANES);
        Logger.log("ATC: Passengers disembarked: " + stats.passengersDisembarked());
        Logger.log("ATC: Passengers boarded: " + stats.passengersBoarded());
        Logger.log("ATC: =================================================");
    }

    private void summarise(String label, Map<String, Long> waits) {
        long min = Long.MAX_VALUE;
        long max = 0;
        long total = 0;
        for (long wait : waits.values()) {
            min = Math.min(min, wait);
            max = Math.max(max, wait);
            total += wait;
        }
        if (waits.isEmpty()) {
            min = 0;
        }
        double average = waits.isEmpty() ? 0 : (double) total / waits.size();
        Logger.log(String.format("ATC: %s -> Max: %.2fs | Average: %.2fs | Min: %.2fs",
                label, max / 1000.0, average / 1000.0, min / 1000.0));
    }
}
