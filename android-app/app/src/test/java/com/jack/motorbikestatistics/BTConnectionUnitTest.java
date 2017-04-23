package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Jack on 18-Apr-17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Looper.class})
public class BTConnectionUnitTest {

    private BTConnection testItem;

    @Mock
    Looper mockLooper;
    @Mock
    BluetoothDevice mockBTDevice;
    @Mock
    BluetoothSocket mockBTSocket;
    @Mock
    Handler mockHandler;


    @Before
    public void initialise() throws Exception {

        /* Set our mock objects up. */
        MockitoAnnotations.initMocks(this);

        /* Set up our static mock */
        PowerMockito.mockStatic(Looper.class);
        when(Looper.getMainLooper()).thenReturn(mockLooper);

        /* Set up our mock function for BT device. */
        when(mockBTDevice.createRfcommSocketToServiceRecord(any(UUID.class)))
                .thenReturn(mockBTSocket);

        /* Ensure new BTConnection created before each test. */
        testItem = new BTConnection(mockBTDevice);
    }

    @Test
    public void testGetSetRXHandler() throws Exception {

        /* Check original state is null. */
        assertEquals(null, testItem.getRXHandler());

        /* Check state of handler after setting. */
        testItem.setRXHandler(mockHandler);
        assertEquals(mockHandler, testItem.getRXHandler());
    }

    @Test
    public void testTXHandleMessage() throws Exception {
        /* Check handler is not null on creation. */
        assertNotEquals(null, testItem.txHandler);
    }
}
