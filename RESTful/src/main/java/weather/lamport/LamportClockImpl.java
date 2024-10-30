package weather.lamport;

public class LamportClockImpl implements LamportClock {
    private int clock;

    public LamportClockImpl() {
        // Start clock at 0
        this.clock = 0;
    }

    @Override
    public synchronized int getTime() {
        return clock;
    }

    @Override
    public synchronized void tick() {
        clock++;
    }

    @Override
    public synchronized void update(int otherClock) {
        clock = Math.max(clock, otherClock) + 1;
    }

    @Override
    public synchronized int incrementAndGet() {
        // Increment the clock before sending
        tick();
        return getTime();
    }
}
