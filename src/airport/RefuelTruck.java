package airport;

import java.util.LinkedList;

final class RefuelTruck extends Thread {

    private final LinkedList<Turnaround> jobs = new LinkedList<>();
    private boolean busy;
    private boolean closed;

    RefuelTruck() {
        super("Thread-RefuelTruck");
    }

    synchronized void requestRefuel(Turnaround job) {
        if (job.plane().isEmergency()) {
            jobs.addFirst(job);
        } else {
            jobs.addLast(job);
        }
        notifyAll();
    }

    synchronized void close() {
        closed = true;
        notifyAll();
    }

    synchronized boolean isIdle() {
        return !busy && jobs.isEmpty();
    }

    @Override
    public void run() {
        try {
            Turnaround job;
            while ((job = nextJob()) != null) {
                refuel(job);
                finishJob();
            }
            Logger.log("RefuelTruck: No more planes today. Returning to depot.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized Turnaround nextJob() throws InterruptedException {
        while (jobs.isEmpty() && !closed) {
            wait();
        }
        if (jobs.isEmpty()) {
            return null;
        }
        busy = true;
        return jobs.removeFirst();
    }

    private synchronized void finishJob() {
        busy = false;
    }

    private void refuel(Turnaround job) throws InterruptedException {
        String plane = job.plane().name();
        Logger.log("RefuelTruck: Driving to " + job.gate().name() + " to refuel " + plane + ".");
        Thread.sleep(Config.REFUEL_MS);
        Logger.log("RefuelTruck: " + plane + " refuelled. Truck is free.");
        job.markRefuelled();
    }
}
