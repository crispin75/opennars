package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.Collections;

/**
 * essentially holds a list of registrations but forms an activity context
 * from the dynamics of its event reactivity
 */
public class Active<T extends Topic> extends FasterList<On> {

    Active(int length) {
        super(length);
    }

    Active() {
        this(1);
    }

    public Active(On... r) {
        super(r.length);
        Collections.addAll(this, r);
    }

//        public void resume() {
//            for (Registration r : this)
//                r.resume();
//        }
//        public void pause() {
//            for (Registration r : this)
//                r.pause();
//        }
//        public void cancelAfterUse() {
//            for (Registration r : this)
//                r.cancelAfterUse();
//        }

    public synchronized void off() {
        for (int i = 0; i < this.size(); i++) {
            this.get(i).off();
        }
        clear();
    }

    public Active add(On... elements) {
        Collections.addAll(this, elements);
        return this;
    }


}
