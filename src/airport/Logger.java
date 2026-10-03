package airport;

final class Logger {

    private static final long START = System.currentTimeMillis();

    private Logger() {
    }

    static synchronized void log(String message) {
        double seconds = (System.currentTimeMillis() - START) / 1000.0;
        System.out.printf("[%6.2fs] %-26s : %s%n", seconds, Thread.currentThread().getName(), message);
    }

    static long elapsedMillis() {
        return System.currentTimeMillis() - START;
    }
}
