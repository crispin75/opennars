package nars.clock;

import nars.Memory;

import java.io.Serializable;

/**
 * Created by me on 7/2/15.
 */
public interface Clock extends Serializable {

    /** called when memory reset */
    void clear();

    /** returns the current time, as measured in units determined by this clock */
    long time();



    void preFrame(Memory m);

    long elapsed();
}
