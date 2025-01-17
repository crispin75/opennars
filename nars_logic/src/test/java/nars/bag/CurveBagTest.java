package nars.bag;

import nars.Global;
import nars.Memory;
import nars.bag.impl.CurveBag;
import nars.bag.impl.CurveBag.BagCurve;
import nars.bag.impl.LevelBag;
import nars.budget.Item;
import nars.meter.bag.NullItem;
import nars.nar.Default;
import nars.util.data.Util;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.data.sorted.SortedIndex;
import nars.util.sort.ArraySortedIndex;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class CurveBagTest extends AbstractBagTest {

    final static Random rng = new XorShift1024StarRandom(1);

    static {
        Global.DEBUG = true;
    }

    Memory p = new Default().memory();
    final static BagCurve curve = new CurveBag.FairPriorityProbabilityCurve();

    @Test
    public void testBagSampling() {

        /** for testing normalization:
         * these should produce similar results regardless of the input ranges */
        testBags(0.25f, 0.75f,
                1, 2, 3, 7, 12);
        testBags(0.5f, 0.6f,
                1, 2, 3, 7, 12);

        testBags(0, 1f,
                1, 2, 3, 6, 13, 27, 32, 64, 100);
    }

    public void testBags(float pMin, float pMax, int... capacities) {

        
        //FractalSortedItemList<NullItem> f1 = new FractalSortedItemList<>();
        //int[] d2 = testCurveBag(f1);
        //int[] d3 = testCurveBag(new RedBlackSortedIndex<>());        
        //int[] d1 = testCurveBag(new ArraySortedIndex<>(40));

        
        //use the final distribution to compare that each implementation generates exact same results
        //assertTrue(Arrays.equals(d1, d2));
        //assertTrue(Arrays.equals(d2, d3));

        int repeats = 2;

        System.out.println("Bag sampling distributions, inputs priority range=" + pMin + " .. " + pMax);
        for (int capacity : capacities ) {


            double[] total = new double[capacity];

            for (int i = 0; i < repeats; i++) {
                double[] count = testRemovalDistribution(pMin, pMax, capacity);
                total = MathArrays.ebeAdd(total, count);
            }

            System.out.println("  " + capacity + "," + " = " + Arrays.toString(total));

        }

    }
    
    @Test  public void testCurveBag() {

        ArraySortedIndex items = new ArraySortedIndex(1024);

        testCurveBag(true, items);
        testCurveBag(false, items);
        testCapacityLimit(new CurveBag(rng, 4, curve, items));

        
        
        testAveragePriority(4, items);
        testAveragePriority(8, items);        
        
        int d[] = null;
        for (int capacity : new int[] { 10, 51, 100, 256 } ) {
            d = AbstractBagTest.testRemovalPriorityDistribution(capacity, items);
        }
        

    }
    
    public void testCurveBag(boolean random, SortedIndex<NullItem> items) {
        CurveBag<CharSequence, NullItem> f = new CurveBag(rng, 4, curve, items);
        assertEquals(0, f.getPrioritySum(), 0.001);


        NullItem ni;
        f.put(ni = new NullItem(.25f));
        assertEquals(1, f.size());
        assertEquals(ni.getPriority(), f.getPrioritySum(), 0.001);
        
        f.put(new NullItem(.9f));
        f.put(new NullItem(.75f));
        
        //System.out.println(f);
        
        //sorted
        assertEquals(3, f.size());
        assertTrue(f.items.toString(), f.items.get(0).getPriority() > f.items.get(1).getPriority());

        f.pop();
        
        assertEquals(2, f.size());
        f.pop();
        assert(f.size() == 1);
        f.pop();
        assert(f.isEmpty());
        
        assertEquals(0, f.getPrioritySum(), 0.01);
    }

    public void testCapacityLimit(Bag<CharSequence,NullItem> f) {
        
        NullItem four = new NullItem(.4f);
        NullItem five = new NullItem(.5f);
        
        f.put(four); testOrder(f);



        f.put(five); testOrder(f);

        f.put(new NullItem(.6f)); testOrder(f);


        Item a = f.put(new NullItem(.7f)); assertNull(a); testOrder(f);

        assertEquals(4, f.size());
        assertEquals(f.size(), f.keySet().size());
        assertTrue(f.contains(five));    //5 should be in lowest position

        System.out.println("x\n"); f.printAll();

        f.put(new NullItem(.8f)); //limit

        System.out.println("x\n"); f.printAll(); testOrder(f);
        
        assertEquals(4, f.size());
    }

    private void testOrder(Bag<CharSequence, NullItem> f) {
        float max = f.getPriorityMax();
        float min = f.getPriorityMin();

        Iterator<NullItem> ii = f.iterator();

        NullItem last = null;
        do {
            NullItem n = ii.next();
            if (last == null)
                assertEquals(max, n.getPriority(), 0.001);
            else {
                assertTrue(n.getPriority() <= last.getPriority() );
            }

            last = n;

        } while (ii.hasNext());

        assertEquals(min, last.getPriority(), 0.001);

    }



    public static double[] testRemovalDistribution(float priMin, float priMax, int capacity) {
        int samples = 128 * capacity;
        
        double[] count = new double[capacity];


        SortedIndex<NullItem> items = new ArraySortedIndex<>(capacity);
        CurveBag<CharSequence, NullItem> f = new CurveBag(rng, capacity, curve, items);
        
        //fill
        for (int i= 0; i < capacity; i++) {
            f.put(new NullItem(priMin, priMax));
            assertTrue(f.isSorted());
        }
        
        assertEquals(f.size(), f.capacity());

        
        for (int i= 0; i < samples; i++) {
            count[f.sampler.applyAsInt(f)]++;
        }

        assert(Util.isSemiMonotonicallyDec(count));
        
        //System.out.println(random + " " + Arrays.toString(count));
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);

        return count;
    }

    public void testAveragePriority(int capacity, SortedIndex<NullItem> items) {
        
        
        final float priorityEpsilon = 0.01f;
        
        CurveBag<CharSequence, NullItem> c = new CurveBag(rng, capacity, curve, items);
        LevelBag<CharSequence, NullItem> d = new LevelBag<>(capacity, 10);
        
        assertEquals(c.getPrioritySum(), d.getPrioritySum(), 0);
        assertEquals(c.getPriorityMean(), d.getPriorityMean(), 0);

        c.printAll(System.out);

        c.put(new NullItem(.25f));
        d.put(new NullItem(.25f));

        c.printAll(System.out);

        //check that continuousbag and discretebag calculate the same average priority value        
        assertEquals(0.25f, c.getPriorityMean(), priorityEpsilon);
        assertEquals(0.25f, d.getPriorityMean(), priorityEpsilon);
        
        c.clear();
        d.clear();

        assertEquals(0, c.size());
        assertEquals(0, d.size());
        assertEquals(0, c.getPrioritySum(), 0.001);
        assertEquals(0, d.getPrioritySum(), 0.001);
        assertEquals(0, c.getPriorityMean(), 0.001);
        assertEquals(0, d.getPriorityMean(), 0.001);
        
        c.put(new NullItem(.30f));
        d.put(new NullItem(.30f));
        c.put(new NullItem(.50f));
        d.put(new NullItem(.50f));
        
        assertEquals(0.4, c.getPriorityMean(), priorityEpsilon);
        assertEquals(0.4, d.getPriorityMean(), priorityEpsilon);

    }


//    public void testCurveBag2(boolean random) {
//        ContinuousBag2<NullItem,CharSequence> f = new ContinuousBag2(4, new ContinuousBag2.PriorityProbabilityApproximateCurve(), random);
//        
//        f.putIn(new NullItem(.25f));
//        assert(f.size() == 1);
//        assert(f.getMass() > 0);
//        
//        f.putIn(new NullItem(.9f));
//        f.putIn(new NullItem(.75f));
//        assert(f.size() == 3);
//        
//        //System.out.println(f);
//
//        //sorted
//        assert(f.items.first().getPriority() < f.items.last().getPriority());
//        assert(f.items.first().getPriority() < f.items.exact(1).getPriority());
//
//        assert(f.items.size() == f.nameTable.size());
//        
//        assert(f.size() == 3);
//        
//        f.takeOut();
//        assert(f.size() == 2);
//        assert(f.items.size() == f.nameTable.size());        
//        
//        f.takeOut();
//        assert(f.size() == 1);
//        assert(f.items.size() == f.nameTable.size());        
//        assert(f.getMass() > 0);
//        
//        f.takeOut();
//        assert(f.size() == 0);
//        assert(f.getMass() == 0);
//        assert(f.items.size() == f.nameTable.size());                
//        
//    }


    @Test public void testEqualBudgetedItems() {
        int capacity = 4;

        CurveBag<CharSequence, NullItem> c = new CurveBag(rng, capacity, curve);
        c.mergeAverage();

        NullItem a, b;
        c.put(a = new NullItem(0.5f));
        c.put(b = new NullItem(0.5f));

        assertEquals(2, c.size());

        NullItem aRemoved = c.remove(a.name());

        assertEquals(aRemoved, a);
        assertNotEquals(aRemoved, b);
        assertEquals(1, c.size());

        c.put(a);
        assertEquals(2, c.size());

        NullItem x = c.peekNext();
        assertNotNull(x);

        assertEquals(2, c.size());

        x = c.pop();

        assertTrue(x.equals(a) || x.equals(b));
        assertEquals(1, c.size());

    }


    @Test public void testMerge() {
        int capacity = 3;

        final AtomicInteger putKey = new AtomicInteger(0);
        final AtomicInteger removeKey = new AtomicInteger(0);

        ArraySortedIndex<NullItem> as = new ArraySortedIndex<NullItem>(capacity);
        CurveBag<CharSequence, NullItem> c = new CurveBag<CharSequence, NullItem>(rng, capacity, curve, as) {

            protected CurveMap<CharSequence, NullItem> newIndex(int capacity) {
                return new CurveMap<CharSequence, NullItem>(
                        //new HashMap(capacity)
                        Global.newHashMap(capacity),
                        items
                ) {
                    @Override
                    public NullItem put(NullItem value) {
                        return super.put(value);
                    }

                    @Override
                    public NullItem putKey(CharSequence key, NullItem value) {
                        putKey.incrementAndGet();
                        return super.putKey(key, value);
                    }

                    @Override
                    public NullItem remove(CharSequence key) {
                        removeKey.incrementAndGet();
                        return super.remove(key);
                    }
                };
            }
        };
        c.mergePlus();

        NullItem a = new NullItem(0.5f, "a");

        c.put(a);

        assertEquals(1, putKey.get()); assertEquals(0, removeKey.get());
        assertEquals(1, c.size());

        c.put(a);

        assertEquals(2, putKey.get()); assertEquals(0, removeKey.get());
        assertEquals(1, c.size());



        //merged with plus, 0.5 + 0.5 = 1.0
        assertEquals(1f, c.iterator().next().getPriority(), 0.001);


        c.validate();

        c.mergeAverage();

        //for average merge, we need a new instance of same key
        //but with different priority so that it doesnt modify itself (having no effect)
        NullItem a2 = new NullItem(0.5f, "a");

        c.put(a2);

        //still only one element
        assertEquals(1, c.size());

        //but the merge should have decreased the priority from 1.0
        assertEquals(0.833f, c.iterator().next().getPriority(), 0.001);


        //finally, remove everything

        c.remove(a.name());

        assertEquals(3, putKey.get()); assertEquals(1, removeKey.get());
        assertEquals(0, c.size());

        c.validate();

    }


}
