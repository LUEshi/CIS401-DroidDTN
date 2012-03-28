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
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Handler received from application
    private Handler mHandler;
        
    // Member fields
    private BluetoothAdapter mBtAdapter;
    List<BluetoothDevice> devices;
    private Bluetooth bService;
    
    /* Database helper class */
    LocalStorage db;
    ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
        
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
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // Register for broadcasts when scanning mode changes (ie. the
        // device becomes un-discoverable)
        filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                
        // Create the Bluetooth helper class
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
        
        if(bService != null && bService.getState() == Util.STATE_NONE)
        	bService.start();

        // Initiate a scan
        scan();
	}
	
	/**
     * Initiates a scan for devices. Cancels ongoing discovery
     * and re-initiates it. Discovered devices are stored in
     * pairedDevices by BroadcastReceiver mReceiver.
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
        Log.d(TAG, "Devices currently contains " + devices.size() + " devices.");
        devices = new ArrayList<BluetoothDevice>();
                
        // Request discovery from BluetoothAdapter
        boolean b = mBtAdapter.startDiscovery();
        Log.e(TAG, "Discovery request returned " + b);
    }
    
    /**
     * Connects with a bonded device.
     * 
     * TODO: There is currently no logic dictating which device we decide
     * to pair with. This could instead be based on history, for instance.
     * Even worse, what if the pairing fails, we re-scan, and try to pair
     * with the same device again (unsuccessfully) when there were others
     * for us to try.
     */
    public void connect() {
    	toast("connect()");

    	if(devices.size() < 1) {
    		Log.e(TAG, "We're attempting to connect but there are no connectable devices...");
    		return;
    	}
    	
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
    	Log.d(TAG, "Attempted to connect but found no devices.");
    	scan();	// If we don't connect anywhere, we should resume scanning
    }
    
    public void handshake() {
    	toast("handshake()");
    	/*
	     * Sends a message with the COMPARISON_VECTOR_MSG preamble followed by the
	     * local blog comparison vector.
	     */
    	ArrayList<Integer> localVector = DataPacket.getBlogComparisonVector(db);
    	String msg = Util.COMPARISON_VECTOR_MSG + " " + TextUtils.join(";", localVector);
    	bService.sendMessage(msg);
    	Log.d(TAG, "Sending out comparison message: " + msg);
    }
	
	// Data transfer
    // vectorString was the received vector
    public void transferData(String vectorString) {
    	toast("transferData()");
    	/*
         * Diffs the local blog comparison vector to vectorString and sends out each missing
         * blog post in JSON format.
         */
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
    	
    	// Find missing messages
    	for(DataPacket post : posts) {
    		int hash = post.hashCode();
    		if(!foreignVector.contains(hash)) {
    			toast("The other phone requested packet: " + post.getTitle());
    			bService.sendMessage(post.toJson());
    		}
    	}
    	
    	// TODO: After sending the last message, send an "ALL DONE" message so the user can disconnect and move on
    }
    
    private DataPacket getDataPacketFromHash(int hashCode) {
    	for(DataPacket post : posts) {
    		if(post.hashCode() == hashCode) {
    			return post;
    		}
    	}
    	return null;
    }
	
	// Inform UI, assuming we want to use interrupts; send a toast...
	
	// Disconnect from device
    //start(); scan();
    
 	// The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.length() > 0) {
            	Log.d(TAG, "Action : " + action);
            }

            // The Bluetooth scan mode has just changed
            if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            	int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 
            		      BluetoothAdapter.ERROR);
            	if(mode == BluetoothAdapter.SCAN_MODE_NONE 
            			|| mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Message msg = mHandler.obtainMessage(Util.MESSAGE_REQUEST_DISCOVERABLE);
                    mHandler.sendMessage(msg);
            	}
            }
            // The discovery service has just found a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Found a device named " + device.getName() + ", " + device.getAddress());
                // Add the device to our list
                devices.add(device);
            // When discovery is finished, attempt to establish a connection
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Completed discovery with a total of " + devices.size() + " found devices.");
                
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
