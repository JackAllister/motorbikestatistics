package com.jack.motorbikestatistics;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Jack on 18-Apr-17.
 */

public class TripItemUnitTest {

    private static final String TEST_NAME = "Test Trip 1";
    private static final int TEST_SIZE = 100;

    private TripItem testItem;

    @Before
    public void initialise() {
        /* Ensure we have a fresh trip item before each test. */
        testItem = new TripItem(TEST_NAME, TEST_SIZE);
    }

    @Test
    public void testGetSetTripName() throws Exception {

        /* Test with original name. */
        assertEquals(TEST_NAME, testItem.getTripName());

        /* Test after setting new name. */
        String newName = "New Name";
        testItem.setTripName(newName);
        assertEquals(newName, testItem.getTripName());
    }

    @Test
    public void testGetSetFileSize() throws Exception {

        /* Test with original size. */
        assertEquals(TEST_SIZE, testItem.getFileSize());

        /* Test after setting new size. */
        int newSize = 2000;
        testItem.setFileSize(newSize);
        assertEquals(newSize, testItem.getFileSize());

        /*
         * Test with negative size.
         * Setting a negative size should fail (not possible).
         */
        int negativeSize = -1000;
        testItem.setFileSize(negativeSize);
        assertNotEquals(negativeSize, testItem.getFileSize());
    }
}
