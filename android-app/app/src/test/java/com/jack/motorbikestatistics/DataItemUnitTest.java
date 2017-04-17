package com.jack.motorbikestatistics;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jack on 17-Apr-17.
 */

public class DataItemUnitTest {

    private static final String ITEM_NAME = "TEST 1";
    private static final boolean AVG_ENABLED = true;
    private static final Double MAX_CURRENT = 50.0;
    private static final int AVERAGE_COUNT_NUM = 50;

    private DataItem<Double> testItem;

    @Before
    public void initialise() {
        /* Ensure we have a new data item before each test. */
        testItem = new DataItem<Double>(ITEM_NAME, AVG_ENABLED);
    }

    @Test
    public void testName() throws Exception {
        assertEquals(ITEM_NAME, testItem.getName());
    }

    @Test
    public void testAvgEnabled() throws Exception {
        assertEquals(AVG_ENABLED, testItem.getEnabledAvgMinMax());
    }

    @Test
    public void testCurrent() throws Exception {

        /* At first current should be null. */
        assertEquals(null, testItem.getCurrent());

        /* Test each current reading to see if value returned matches. */
        for (Double i = 0.0; i < MAX_CURRENT; i += 1.0) {
            testItem.setCurrent(i);
            assertEquals(i, testItem.getCurrent());
        }
    }

    @Test
    public void testAverage() throws Exception {

        Double currentSum = 0.0;
        int currentCount = 0;

        /* At first average should be null. */
        assertEquals(null, testItem.getAverage());

        /* Test average after setting one value. */
        testItem.setCurrent(5.0);
        assertEquals((Double)5.0, testItem.getAverage());

        /* Test again, after adding a second value. */
        testItem.setCurrent(10.0);
        assertEquals((Double)7.5, testItem.getAverage());

        /* Test again, after adding a third value. */
        testItem.setCurrent(3.0);
        assertEquals((Double)6.0, testItem.getAverage());
    }

    @Test
    public void testMinimum() throws Exception {

        /* Test that minimum is first set. */
        testItem.setCurrent(5.0);
        assertEquals((Double)5.0, testItem.getMinimum());

        /* Add value over previous make sure not changed. */
        testItem.setCurrent(10.0);
        assertEquals((Double)5.0, testItem.getMinimum());

        /* Add value below previous, make sure changed. */
        testItem.setCurrent(2.5);
        assertEquals((Double)2.5, testItem.getMinimum());
    }

    @Test
    public void testMaximum() throws Exception {

        /* Test that maximum is first set. */
        testItem.setCurrent(5.0);
        assertEquals((Double)5.0, testItem.getMaximum());

        /* Add value below previous make sure not changed. */
        testItem.setCurrent(2.5);
        assertEquals((Double)5.0, testItem.getMaximum());

        /* Add value above previous, make sure changed. */
        testItem.setCurrent(10.0);
        assertEquals((Double)10.0, testItem.getMaximum());
    }

    @Test
    public void testConstructorWithInitialValue() throws Exception {
        DataItem<Integer> newItem = new DataItem<Integer>("Name", true, 10);

        /* Test that all initial values are set. */
        assertEquals((Integer)10, newItem.getCurrent());
        assertEquals((Double)10.0, newItem.getAverage());
        assertEquals((Integer)10, newItem.getMinimum());
        assertEquals((Integer)10, newItem.getMaximum());
    }
}
