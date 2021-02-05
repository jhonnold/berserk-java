package me.honnold.berserk.util;

import me.honnold.berserk.search.PVS;

import java.util.concurrent.atomic.AtomicBoolean;

public class TimeManager implements Runnable {
    private final PVS search;
    private final long timeout;
    private AtomicBoolean running = new AtomicBoolean(false);

    public TimeManager(PVS search, long timeout) {
        this.search = search;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        running.set(true);
        try {
            long sleepTime = 0;
            while (sleepTime < this.timeout && running.get()) {
                Thread.sleep(10);
                sleepTime += 10;
            }
            search.stop();
        } catch (InterruptedException ignored) {
        }
        running.set(false);
    }

    public void stop() {
        running.set(false);
    }
}
