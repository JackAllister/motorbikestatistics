package com.jack.motorbikestatistics;

import android.os.Looper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Jack on 17-Apr-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class BTDeviceItemUnitTest {

    private BTDeviceItem testItem;

    @Mock
    BTConnection mockConnection;

    @Before
    public void initialise() {
        /* Ensure new BTDeviceItem created before each test. */
        testItem = new BTDeviceItem(null, "unpaired", 0);
    }

    @Test
    public void testGetSetConnection() throws Exception {

        /* Originally should not have a connection associated with it. */
        assertEquals(null, testItem.getConnection());

        /* Test when adding new connection object. */
        testItem.setConnection(mockConnection);
        assertEquals(mockConnection, testItem.getConnection());
    }

    @Test
    public void testGetDevice() throws Exception {
        /* We test against what was set in constructor (null). */
        assertEquals(null, testItem.getDevice());
    }

    @Test
    public void testGetSetStatus() throws Exception {
        /* We test against what was set in constructor (unpaired). */
        assertEquals("unpaired", testItem.getStatus());

        /* Test by setting new status. */
        testItem.setStatus("paired");
        assertEquals("paired", testItem.getStatus());

        /* Test by setting status to null. */
        testItem.setStatus(null);
        assertEquals(null, testItem.getStatus());
    }

    @Test
    public void testGetSetIconID() throws Exception {
        /* We test against what was set in constructor (0). */
        assertEquals(0, testItem.getIconID());

        /* Test by setting new ID */
        testItem.setIconID(1);
        assertEquals(1, testItem.getIconID());

        /* Test again by setting large ID. */
        testItem.setIconID(10000);
        assertEquals(10000, testItem.getIconID());

        /*
         * Test by using negative ID.
         * Even though impossible to have negative ID for an Icon resources better
         * to test just incase.
         */
        testItem.setIconID(-1000);
        assertEquals(-1000, testItem.getIconID());
    }


}
