package airport;

import java.util.Random;

public final class AirportSimulation {

    private AirportSimulation() {
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("Thread-Main");
        Logger.log("Main: Asia Pacific Airport simulation starting.");

        Random rand = new Random();
        Statistics stats = new Statistics();
        ControlTower tower = new ControlTower(Config.GATE_COUNT);
        RefuelTruck truck = new RefuelTruck();
        AirTrafficController atc = new AirTrafficController(tower, stats, truck);

        truck.start();
        atc.start();

        Plane[] planes = new Plane[Config.TOTAL_PLANES];
        for (int i = 0; i < planes.length; i++) {
            int id = i + 1;
            planes[i] = new Plane(id, id == Config.EMERGENCY_PLANE_ID, tower, truck, stats,
                    rand.nextInt(Config.MAX_PASSENGERS) + 1, rand.nextInt(Config.MAX_PASSENGERS) + 1);
            planes[i].start();
            if (id < planes.length) {
                Thread.sleep(rand.nextInt(3) * 1000L);
            }
        }

        for (Plane plane : planes) {
            plane.join();
        }
        atc.join();
        truck.close();
        truck.join();

        Logger.log(String.format("Main: Simulation finished in %.2fs.", Logger.elapsedMillis() / 1000.0));
    }
}
