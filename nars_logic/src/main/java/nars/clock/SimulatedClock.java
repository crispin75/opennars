package nars.clock;

import nars.Memory;

/**
 * Clock whose time change is managed by an external process
 */
public class SimulatedClock implements Clock {

    long t, t0, tNext;


    @Override
    public void clear() {
        t = t0 = 0;
    }

    @Override
    public long time() {
        return t;
    }

    public void set(long t) {
        this.tNext = t;
    }

    public void add(long dt) { set(this.t + dt); }

    @Override
    public void preFrame(Memory m) {
        t0 = t;
        t = tNext;
    }


    @Override
    public long elapsed() {
        return t0 - t;
    }

    @Override
    public String toString() {
        return Long.toString(t);
    }
}
