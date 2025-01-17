///*
// * Copyright (C) 2014 peiwang
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package nars.nal.nal7;
//
//import nars.Memory;
//import nars.term.Atom;
//
///**
// * This stores the magnitude of a time difference, which is the logarithm of the time difference
// * in base D=duration ( @see Param.java ).  The actual printed value is +1 more than the stored
// * magnitude, so for example, it will have name() "+1" if magnitude=0, and "+2" if magnitude=1.
// *
// * @author peiwang
// */
//@Deprecated public class LogInterval extends Atom implements AbstractInterval {
//
//
//    static final int INTERVAL_POOL_SIZE = 16;
//    static final LogInterval[] INTERVAL = new LogInterval[INTERVAL_POOL_SIZE];
//
////    public final int magnitude;
////
////    public static AbstractInterval interval(final String i) {
////        return interval(Integer.parseInt(i.substring(1)) - 1);
////    }
////
////    public static LogInterval interval(final long time, final Memory m) {
////        return interval(magnitude(time, m.duration));
////    }
////
////    public static LogInterval interval(final long time, final AtomicDuration duration) {
////        return interval(magnitude(time, duration));
////    }
////
////    public static LogInterval interval(int magnitude) {
////        if (magnitude >= INTERVAL_POOL_SIZE)
////            return new LogInterval(magnitude, true);
////        else if (magnitude < 0)
////            magnitude = 0;
////
////        LogInterval existing = INTERVAL[magnitude];
////        if (existing == null) {
////            existing = new LogInterval(magnitude, true);
////            INTERVAL[magnitude] = existing;
////        }
////        return existing;
////    }
//
////
////    // time is a positive integer
////    protected LogInterval(final long timeDiff, final AtomicDuration duration) {
////        this(magnitude(timeDiff, duration), true);
////    }
////
////
////    /** this constructor has an extra unused argument to differentiate it from the other one,
////     * for specifying magnitude directly.
////     */
////    protected LogInterval(final int magnitude, final boolean yesMagnitude) {
////        super(Symbols.INTERVAL_PREFIX_OLD + String.valueOf(1+magnitude));
////        this.magnitude = magnitude;
////    }
//
//    public static int magnitude(final long timeDiff, final AtomicDuration duration) {
//        int m = (int) Math.round(Math.log(timeDiff) / duration.getSubDurationLog());
//        if (m < 0) return 0;
//        return m;
//    }
//
//    public static double time(final double magnitude, final double subdurationLog) {
//        return Math.exp(magnitude * subdurationLog);
//    }
//
//    public static double time(final double magnitude, final AtomicDuration duration) {
//        if (magnitude <= 0)
//            return 1;
//        return time(magnitude, duration.getSubDurationLog());
//    }
//
//    public static long cycles(final int magnitude, final AtomicDuration duration) {
//        return Math.round(time(magnitude, duration));
//    }
//
//    /** Calculates the average of the -0.5, +0.5 interval surrounding the integer magnitude
//     *  EXPERIMENTAL
//     * */
//    public static long cyclesAdjusted(final int magnitude, final AtomicDuration duration) {
//        //TODO cache this result because it will be equal for all similar integer magnitudes
//        double magMin = magnitude - 0.5;
//        double magMax = magnitude + 0.5;
//        return Math.round((time(magMin, duration) + time(magMax, duration))/2.0);
//    }
//
//    @Override
//    public final long cycles(final Memory m) {
//        return cycles(m.duration);
//    }
//
////    public final long cycles(final AtomicDuration duration) {
////        //TODO use a lookup table for this
////        return cycles(magnitude, duration);
////    }
//
//
////    /** returns a sequence of intervals which approximate a time period with a maximum number of consecutive Interval terms */
////    public static List<AbstractInterval> intervalSequence(final long t, final int maxTerms, final Memory memory) {
////        if (maxTerms == 1)
////            return Lists.newArrayList(interval(t, memory));
////
////        Interval first = interval(t, memory);
////        long a = first.cycles(memory); //current approximation value
////        if (a == t) return Lists.newArrayList(first);
////        else if (a < t) {
////            //ok we will add to it. nothing to do here
////        }
////        else if ((a > t) && (first.magnitude > 0)) {
////            //use next lower magnitude
////            first = interval(first.magnitude - 1);
////            a = first.cycles(memory);
////        }
////
////        List c = new ArrayList(maxTerms);
////        c.add(first);
////
////        long remaining = t - a;
////        c.addAll( intervalSequence(remaining, maxTerms - 1, memory));
////
////        /*
////        Interval approx = Interval.intervalTime(t, memory);
////        System.out.println(t + " = " + c + "; ~= " +
////                        approx + " (t=" + t + ", seq=" + intervalSequenceTime(c, memory) + ", one=" + approx.getTime(memory) + ")");
////        */
////
////        return c;
////    }
////
////    /** sum the time period contained in the Intervals (if any) in a sequence of objects (usually list of Terms) */
////    public static long intervalSequence(final Iterable s, final Memory memory) {
////        long time = 0;
////        for (final Object t : s) {
////            if (t instanceof AbstractInterval) {
////                AbstractInterval i = (AbstractInterval)t;
////                time += i.cycles(memory);
////            }
////        }
////        return time;
////    }
//
//
//
//}