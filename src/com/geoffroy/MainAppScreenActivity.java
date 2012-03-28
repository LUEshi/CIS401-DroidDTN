package com.geoffroy;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainAppScreenActivity extends ListActivity {
	/* Database helper class */
    LocalStorage db;
    ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
    
    // Username from settings
    private String username;
    
    int NEW_POST_REQUEST = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.select_post);
	    
	    db = new LocalStorage(this);
        posts = DataPacket.loadAll(db);
        
		// Bind to our new adapter.
        setListAdapter(new DataPacketArrayAdapter(this,posts));
        
        // Load settings, if username doesn't exist, load model as default
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        username = settings.getString("username", android.os.Build.MODEL);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		posts = DataPacket.loadAll(db);
		setListAdapter(new DataPacketArrayAdapter(this,posts));

	}
	
	@Override
	// Called when NewPostActivity returns
	// Creates a new DataPacket with the given information, persists the DP, and updates posts
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/*
		 * if(resultCode == RESULT_OK){
			Bundle bun = data.getExtras();
			String title = bun.getString("title");
			String content = bun.getString("content");
			
			DataPacket dp = new DataPacket(username, title, content);
			dp.persist(db);
			update();
	    }
		else{
			System.out.println("RESULT FAILED");
	    }
		*/
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
