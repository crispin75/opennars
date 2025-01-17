package nars.event;

import nars.Memory;
import nars.NAR;
import nars.util.event.On;

import java.util.function.Consumer;

/**  call at the end of a frame (a batch of cycles) */
abstract public class FrameReaction implements Consumer<NAR> {

    private On reg;

    public FrameReaction(NAR nar) {
        this(nar.memory);
    }
    public FrameReaction(Memory m) {
        reg = m.eventFrameStart.on(this);
    }

    public void off() {
        if (reg!=null) {
            reg.off();
            reg = null;
        }
    }

    @Override
    public void accept(NAR nar) {
        onFrame();
    }


    @Deprecated abstract public void onFrame();
}