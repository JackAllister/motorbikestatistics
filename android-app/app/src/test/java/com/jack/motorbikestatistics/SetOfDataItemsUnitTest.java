package com.jack.motorbikestatistics;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Jack on 17-Apr-17.
 */

public class SetOfDataItemsUnitTest {

    private static final int NUMBER_DATA_ITEMS = 30;

    private SetOfDataItems testSet;

    @Before
    public void initialise() {

        /* Our set of data items to be testing against. */
        testSet = new SetOfDataItems();

        /* Add some data items */
        for (int i = 0; i < NUMBER_DATA_ITEMS; i++) {
            String name = "Item " + Integer.toString(i);

            testSet.add(new DataItem(name, true));
        }
    }

    @Test
    public void searchName_notFound() throws Exception {

        /* Search for completely invalid name */
        DataItem resultItem = testSet.getItemByName("INVALID 1");
        assertEquals(null, resultItem);

        /* Search for item with similar name (dash instead of space) */
        resultItem = testSet.getItemByName("Item-1");
        assertEquals(null, resultItem);

        /* Search for partial name */
        resultItem = testSet.getItemByName("Item");
        assertEquals(null, resultItem);

        /* Search for valid name + extra */
        resultItem = testSet.getItemByName("Item-1.");
        assertEquals(null, resultItem);
    }

    @Test
    public void searchName_found() throws Exception {

        /* Test each item in array. */
        for (int i = 0; i < NUMBER_DATA_ITEMS; i++) {

            String searchName = "Item " + Integer.toString(i);
            DataItem resultItem = testSet.getItemByName(searchName);

            /* Check that result is not null. */
            assertNotEquals(null, resultItem);

            /* Check that name actually does match. */
            assertEquals(searchName, resultItem.getName());
        }
    }

}
