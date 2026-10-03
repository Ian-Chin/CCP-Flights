package airport;

final class CleaningCrew extends Thread {

    private final Turnaround turnaround;

    CleaningCrew(Turnaround turnaround) {
        super("Thread-Crew-" + turnaround.gate().name());
        this.turnaround = turnaround;
    }

    @Override
    public void run() {
        String plane = turnaround.plane().name();
        String who = turnaround.gate().name() + " Crew: ";
        try {
            Logger.log(who + "Refilling food and supplies for " + plane + ".");
            Thread.sleep(Config.SUPPLIES_MS);
            Logger.log(who + "Supplies loaded on " + plane + ". Waiting for cabin to empty.");

            turnaround.awaitDisembarked();

            Logger.log(who + "Cleaning cabin of " + plane + ".");
            Thread.sleep(Config.CLEANING_MS);
            Logger.log(who + plane + " is clean and ready for boarding.");
            turnaround.markCleaned();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
