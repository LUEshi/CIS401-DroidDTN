package com.geoffroy;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Util {
    // Message types sent from the NetworkHandle Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_REQUEST_DISCOVERABLE = 6;
    public static final int MESSAGE_NO_CONNECTION = 7;
    
    // Constants that indicate the current Bluetooth connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    // Strings used in message data packets
    public static final String TOAST = "toast";
    
    // Header for comparison vector messages
    public static final String COMPARISON_VECTOR_MSG = "comparison_vector_msg";
    
    // Intent request codes
    public static final int REQUEST_ENABLE_BT = 3;
    
    // Device name
    public static final String DEVICE_NAME = "device_name";
    
    public static void toast(Handler h, String s) {
    	// Send a message back to the Activity
        Message msg = h.obtainMessage(Util.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Util.TOAST, s);
        msg.setData(bundle);
        h.sendMessage(msg);
    }

}