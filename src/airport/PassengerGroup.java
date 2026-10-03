package airport;

final class PassengerGroup extends Thread {

    private final Turnaround turnaround;
    private final Statistics stats;
    private final int arriving;
    private final int departing;

    PassengerGroup(Turnaround turnaround, Statistics stats, int arriving, int departing) {
        super("Thread-Passengers-" + turnaround.plane().name());
        this.turnaround = turnaround;
        this.stats = stats;
        this.arriving = arriving;
        this.departing = departing;
    }

    @Override
    public void run() {
        String plane = turnaround.plane().name();
        String who = plane + "'s Passengers: ";
        try {
            Logger.log(who + arriving + " disembarking out of " + plane + " at " + turnaround.gate().name() + ".");
            Thread.sleep(Config.DISEMBARK_MS);
            Logger.log(who + "All " + arriving + " passengers have disembarked into the terminal.");
            stats.addDisembarked(arriving);
            turnaround.markDisembarked();

            turnaround.awaitCleaned();

            Logger.log(who + departing + " new passengers embarking onto " + plane + ".");
            Thread.sleep(Config.EMBARK_MS);
            Logger.log(who + "All " + departing + " passengers are seated on " + plane + ".");
            stats.addBoarded(departing);
            turnaround.markBoarded();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
