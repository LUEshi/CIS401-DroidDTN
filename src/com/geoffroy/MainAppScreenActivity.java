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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainAppScreenActivity extends ListActivity {
	
	// Debugging
    private static final String TAG = "DroidDTN - MainAppScreenActivity";
    
	/* Database helper class */
    LocalStorage db;
    ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
    
    // Username from settings
    private String username;
    
    int NEW_POST_REQUEST = 0;
    
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
	    
	    db = new LocalStorage(this);
        posts = DataPacket.loadAll(db);
        
		// Bind to our new adapter.
        dpArrayAdapter = new DataPacketArrayAdapter(this, posts);
        setListAdapter(dpArrayAdapter);
        
        // Load settings, if username doesn't exist, load model as default
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        username = settings.getString("username", android.os.Build.MODEL);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.e(TAG, "+ ON START +");
		
		// If BT is not on, request that it be enabled.
        // the connection service will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
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
		Log.e(TAG, "+ ON RESUME +");
		
		posts = DataPacket.loadAll(db);
		setListAdapter(new DataPacketArrayAdapter(this,posts));

		if(cService != null) {
			if(cService.getState() == Util.STATE_NONE)
				cService.start();
			else if(cService.getState() == Util.STATE_LISTEN)
				cService.scan();
			else
				Toast.makeText(getApplicationContext(), "Resumed with state " + cService.getState(),
                        Toast.LENGTH_SHORT).show();
        } else if(cService == null) {
        	Intent intent = new Intent(this, ConnectionService.class);
    		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        	Log.e(TAG,"ConnectionService was found null. This should only happen" +
        			"if this is the first time onResume() is called.");
        }
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "--- ON DESTROY ---");
		
		// Unbind from the ConnectionService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        db.close();
       
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case Util.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	ensureDiscoverable();
            	// Bluetooth is now enabled, so set up the connection service
            	if (cService == null) {
            		Intent intent = new Intent(this, ConnectionService.class);
            		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            	}
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(getApplicationContext(), "Bluetooth was not enabled. Leaving DroidDTN.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to ConnectionService, cast the IBinder and get ConnectionService instance
            LocalBinder binder = (LocalBinder) service;
            cService = binder.getService();
            mBound = true;
            cService.setAppHandler(new NetworkHandle());
            cService.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            cService.setAppHandler(null);
        }
    };
    
		//Use this code to refresh the screen when new posts are found
    //posts = DataPacket.loadAll(db);
    //dpArrayAdapter.notifyDataSetChanged()
		
    // The Handler that gets information back from the BluetoothChatService
    class NetworkHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
	        	case Util.MESSAGE_TOAST:
	                Toast.makeText(getApplicationContext(), msg.getData().getString(Util.TOAST),
	                               Toast.LENGTH_SHORT).show();
	                break;
	        	case Util.MESSAGE_NO_CONNECTION:
	        		// We currently do nothing if a connection fails or is lost,
	        		// since the service timer will get clean things up
	        		break;
	        	case Util.MESSAGE_STATE_CHANGE:
	        		if(msg.arg1 == Util.STATE_CONNECTED) {
	        			cService.handshake();
	        		}
	        		break;
	        	case Util.MESSAGE_READ:
	        		byte[] readBuf = (byte[]) msg.obj;
	        		if(readBuf == null)
	        			break;
	                // construct a string from the valid bytes in the buffer
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                if(readMessage.startsWith(Util.COMPARISON_VECTOR_MSG)) {
	                	cService.transferData(readMessage.substring(
	                			Util.COMPARISON_VECTOR_MSG.length() + 1));
	                } else if(readMessage.startsWith(Util.CLOSE_TRANSMISSION_MSG)) {
	                	cService.closeConnection();
	                } else {
	                	// Save the received post to data storage
	                	DataPacket newPost = new DataPacket(readMessage);
	                	Log.d(TAG, "Received a message with title" 
	                			+ newPost.getTitle() + " and body " + newPost.getContent());
	            		newPost.persist(db);
	            		update();
	            		dpArrayAdapter.notifyDataSetChanged();
	            		// TODO: This currently does not update the UI as it should
	                }
	                Toast.makeText(getApplicationContext(), readMessage,
                            Toast.LENGTH_LONG).show();
	                break;
	        	case Util.MESSAGE_DEVICE_NAME:
	        		Toast.makeText(getApplicationContext(), "Connected to "
                            + msg.getData().getString(Util.DEVICE_NAME), 
                            Toast.LENGTH_SHORT).show();
	        		break;
	        	case Util.MESSAGE_REQUEST_DISCOVERABLE:
	        		ensureDiscoverable();
	        		break;
	        		
            }
        }
    };
    
    private void ensureDiscoverable() {
        Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
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
	    inflater.inflate(R.menu.settings_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.settings:
	        	Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// Reload posts
	public void update(){ posts = DataPacket.loadAll(db); }
	
	
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
		bun.putLong("created", dp.getCreated());
		bun.putString("author", dp.getAuthor());
		bun.putString("title", dp.getTitle());
		bun.putString("content", dp.getContent());
		bun.putString("type", dp.getType());

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
	
	
	// GETTERS
	public ArrayList<DataPacket> getPosts(){  return posts; }
	public LocalStorage getDB(){ return db; }

}
