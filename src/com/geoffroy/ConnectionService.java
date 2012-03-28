package com.geoffroy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class ConnectionService extends Service {

	// Debugging
    private static final String TAG = "AndroidBackground_v3 - ConnectionService";
	// Binder given to clients to allow interaction
    private final IBinder mBinder = new LocalBinder();
    // Handler received from application to allow messaging
    private Handler mHandler;
    // Bluetooth adapter
    private BluetoothAdapter mBtAdapter;
    // Bluetooth devices found via discovery
    List<BluetoothDevice> devices;
    // Helper Bluetooth service (contains connectivity threads)
    private Bluetooth bService;
    // Database
    LocalStorage db;
    // DataPacket blog posts
    ArrayList<DataPacket> posts;
        
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ConnectionService getService() {
            // Return this instance of ConnectionService so clients can call public methods
            return ConnectionService.this;
        }
    }
	
	public IBinder onBind(Intent intent) {
        return mBinder;
    }

	public void setMhandler(Handler handler) {
		mHandler = handler;
		
		if(bService != null)
			bService.setHandler(handler);
	}
	
	public int getState() {
		return bService.getState();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		/*
		 * Broadcast registrations
		 */
		// When a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // When discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // When scanning mode changes (ie. the device becomes un-discoverable)
        filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        // Fetch the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                
        // Instantiate the Bluetooth helper class
        bService = new Bluetooth(this.getApplicationContext());
        
        // Maintain a list of connectable devices
        devices = new ArrayList<BluetoothDevice>();
        
        // Load the database
        db = new LocalStorage(this);
        posts = DataPacket.loadAll(db);
  	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);  
        // Close the database
        db.close();
    }
	
	public void start() {
        toast("start()");
        // Start the Bluetooth helper instance
        if(bService != null && bService.getState() == Util.STATE_NONE)
        	bService.start();
        // Initiate a scan
        scan();
	}
	
	/**
     * Initiates a scan for devices. Cancels ongoing discovery
     * and re-initiates it. Discovered devices are added to
     * devices by BroadcastReceiver mReceiver.
     * 
     * TODO: We currently wait till discovery completes before doing anything.
     * Instead, we might want to optimistically pair as soon as we find a
     * valid device.
     */
    public void scan() {
        Log.d(TAG, "scan()");

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Clear the existing device list
        devices = new ArrayList<BluetoothDevice>();                
        // Request discovery from BluetoothAdapter
        boolean b = mBtAdapter.startDiscovery();
    }
    
    /**
     * Connects with a paired device.
     * 
     * TODO: There is currently no logic dictating which device we decide
     * to pair with. This could instead be based on history, for instance.
     * Even worse, what if the pairing fails, we re-scan, and try to pair
     * with the same device again (unsuccessfully) when there were others
     * for us to try.
     */
    public void connect() {
    	toast("connect()");

    	// Make sure there are actually devices to connect to, or return to scan
    	if(devices.size() < 1) {
    		Log.e(TAG, "We're attempting to connect but there are no connectable devices...");
    		scan();
    		return;
    	}
    	// TODO: Change the following logic, which currently hard-codes the
    	// MAC address of our test phone
    	Iterator<BluetoothDevice> i = devices.iterator();
    	while(i.hasNext()) {
    		BluetoothDevice d = i.next();
    		//if(d.getAddress().equals("D8:54:3A:BD:39:2B")) {	 // Test phone
    		if(d.getAddress().equals("3C:5A:37:87:6E:F2")) {	// Nexus S
    			toast("Found test phone and returning it for connection: " + d.getAddress());
    			
    			// Cancel discovery because it's costly and we're about to connect
                mBtAdapter.cancelDiscovery();

                // Connect to the found device
    			bService.connect(d);
    			return;
    		}
    	}
    	// If we don't connect anywhere, we should resume scanning
    	scan();
    }
    
    /**
     * Perform a handshake with a connected device. Sends a message with the 
     * COMPARISON_VECTOR_MSG preamble followed by the local blog comparison vector.
     */
    public void handshake() {
    	toast("handshake()");
    	// Check that we're actually connected before trying anything
        if (bService.getState() != Util.STATE_CONNECTED) {
        	toast("You are not connected to a device and cannot perform a handshake.");
            return;
        }
    	// Get the local blog comparison vector and package it to a string
    	ArrayList<Integer> localVector = DataPacket.getBlogComparisonVector(db);
    	String msg = Util.COMPARISON_VECTOR_MSG + " " + TextUtils.join(";", localVector);
    	// Send the local vector
    	bService.sendMessage(msg);
    	Log.d(TAG, "Sending out comparison message: " + msg);
    }
	
    /**
     * Send data packets to the connected device. Diffs the local blog comparison
     * vector to parameter vectorString (the foreign vector) and sends out each
     * missing blog post in JSON format.
     */
    public void transferData(String vectorString) {
    	toast("transferData()");
    	// Check that we're actually connected before trying anything
        if (bService.getState() != Util.STATE_CONNECTED) {
        	toast("You are not connected to a device and cannot perform a handshake.");
            return;
        }
    	// Build the foreign vector
    	ArrayList<Integer> foreignVector = new ArrayList<Integer>();
    	String[] vectorArray = vectorString.split(";"); 	
    	for(String arrayEl : vectorArray) {
    		if(arrayEl.length() > 0) {
    			foreignVector.add(Integer.valueOf(arrayEl));
    		}
    	}    	
    	// Fetch local messages
    	posts = DataPacket.loadAll(db);
    	// Find missing messages and send them
    	for(DataPacket post : posts) {
    		int hash = post.hashCode();
    		if(!foreignVector.contains(hash)) {
    			toast("The other phone requested packet: " + post.getTitle());
    			bService.sendMessage(post.toJson());
    		}
    	}
    	// TODO: After sending the last message, send an "ALL DONE" message so 
    	// the user can disconnect and move on
    }

    /**
     * TODO: Implement a disconnect() method to end a connection once all messages
     * have been swapped. It should call start() to resume scanning and listening.
     */
    
 	// The BroadcastReceiver that listens for system broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // The Bluetooth scan mode has just changed
            if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            	int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 
            		      BluetoothAdapter.ERROR);
            	if(mode == BluetoothAdapter.SCAN_MODE_NONE 
            			|| mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
            		// If the device is not currently discoverable, request that
            		// it be made discoverable
                    Message msg = mHandler.obtainMessage(
                    		Util.MESSAGE_REQUEST_DISCOVERABLE);
                    mHandler.sendMessage(msg);
            	}
            }
            // The discovery service has just found a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(
                		BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found a device named " + device.getName() 
                		+ ", " + device.getAddress());
                // Add the device to our list
                devices.add(device);
            // Discovery has just completed
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Completed discovery with a total of " 
                		+ devices.size() + " found devices.");
            	// If devices were found, attempt to connect; otherwise, repeat the scan
            	if(devices.size() > 0) {
            		connect();
            	} else {
            		scan();
            	}
			}
        }
    };
    
    private void toast(String s) {
    	Log.d(TAG, "toasted: " + s);
    	Util.toast(mHandler, s);
    }
}
