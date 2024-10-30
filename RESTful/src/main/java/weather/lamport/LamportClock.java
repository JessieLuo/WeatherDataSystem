package weather.lamport;

public interface LamportClock {
    // Get the current time
    int getTime();

    // Increment the clock for an internal event
    // Carefully use it; It only used on node internally
    void tick();

    // Update the clock after receiving a message
    void update(int otherClock);

    // Prepare clock for sending a message
    int incrementAndGet();
}
