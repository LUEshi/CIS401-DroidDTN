package com.geoffroy;

import java.util.ArrayList;

import com.geoffroy.ConnectionService.LocalBinder;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainAppScreenActivity extends ListActivity {
   
	/* Database helper class */
    LocalStorage db;
    ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
    
    private static final int NEW_POST_REQUEST = 0;
	private static final int NEW_PICTURE_REQUEST = 1;
	
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    
    // Connection service
    ConnectionService cService;
    boolean mBound = false;
    
    // DataPacketArrayAdapter to connect view to data posts
    private DataPacketArrayAdapter dpArrayAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.select_post);
	    
	    Util.log(Util.LOG_INFO, "Starting DroidDTN.", null);
	    
	    db = new LocalStorage(this);
        posts = DataPacket.loadAll(db, true);
        
		// Bind to our new adapter.
        dpArrayAdapter = new DataPacketArrayAdapter(this, posts);
        setListAdapter(dpArrayAdapter);
                
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	Util.log(Util.LOG_ERROR, "Bluetooth is not available, shutting down.", null);
            Toast.makeText(this, "Bluetooth is not available, shutting down.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
	}
	
	@Override
	public void onStart() {
		super.onStart();		
		// If BT is not on, request that it be enabled.
        // the connection service will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
        	Util.log(Util.LOG_INFO, "Bluetooth is not turned on - requesting enable.", null);
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Util.REQUEST_ENABLE_BT);
        }
        else {	// Otherwise, setup the connection service
        	ensureDiscoverable();
        	if (cService == null) {
        		Intent intent = new Intent(this, ConnectionService.class);
        		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        	}
        }
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		posts = DataPacket.loadAll(db, true);
		setListAdapter(new DataPacketArrayAdapter(this,posts));
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Unbind from the ConnectionService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        // Close the database
        db.close();
       
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case Util.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns, we attempt
        	// to bind to ConnectionService once again
            if (resultCode == Activity.RESULT_OK) {
            	ensureDiscoverable();
            	if (cService == null) {
            		Intent intent = new Intent(this, ConnectionService.class);
            		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            	}
            } else {
                // User did not enable Bluetooth or an error occured
            	Util.log(Util.LOG_ERROR, "Bluetooth was not enabled, shutting down.", null);
                Toast.makeText(this, "Bluetooth was not enabled, shutting down.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to ConnectionService, cast the IBinder and get ConnectionService instance
            LocalBinder binder = (LocalBinder) service;
            cService = binder.getService();
            mBound = true;
            cService.setAppHandler(new NetworkHandle());
            
            // Start the ConnectionService instance
            cService.start();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            cService.setAppHandler(null);
        }
    };
		
    // The Handler that gets information back from the ConnectionService instance
    class NetworkHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        		// Display toasts sent by other application components
	        	case Util.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), 
	                		msg.getData().getString(Util.TOAST), 
	                		Toast.LENGTH_SHORT).show();
	                break;
	            // When a new message is received, update the UI
	        	case Util.MESSAGE_READ:
	        		DataPacket newPost = (DataPacket) msg.obj;
            	    dpArrayAdapter.add(newPost);
            		dpArrayAdapter.notifyDataSetChanged();
	                break;
	        	case Util.MESSAGE_REQUEST_DISCOVERABLE:
	        		ensureDiscoverable();
	        		break;
	        		
            }
        }
    };
    
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
        	Util.log(Util.LOG_INFO, "Requesting that device be discoverable.", null);
            Intent discoverableIntent = new Intent(
            		BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
            		BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.about:
	        	i = new Intent(this, AboutScreenActivity.class);
				startActivity(i);
				return true;
		    case R.id.help:
	        	i = new Intent(this, HelpScreenActivity.class);
				startActivity(i);
				return true;
		    case R.id.logs:
	        	i = new Intent(this, LogsScreenActivity.class);
				startActivity(i);
				return true;
	        case R.id.settings:
	        	i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
	
	@Override  
	protected void onListItemClick(ListView l, View v, int position, long id) {  
	  super.onListItemClick(l, v, position, id);  
	  clickOnPost(posts.get(position));
	  
	}  
	
	// To be called when a packet is clicked.
	// Creates a new ViewPostActivity with dp's fields as parameters (might be clunky?)
	public void clickOnPost(DataPacket dp){
		Intent intent = new Intent();
		Bundle bun = new Bundle();

		bun.putLong("localID", dp.getLocalID());

		intent.setClass(this, ViewPostActivity.class);
		intent.putExtras(bun);
		startActivity(intent);
	}
	
	// To be called when New Post is clicked.
	// Creates a new NewPostActivity and requests result.
	public void onNewPostClick(View v){
		Intent i = new Intent(this, NewPostActivity.class);       
        startActivityForResult(i, NEW_POST_REQUEST);
	}

	public void onNewPictureClick(View v){
		Intent i = new Intent(this, NewPictureActivity.class);       
        startActivityForResult(i, NEW_PICTURE_REQUEST);
	}
}
