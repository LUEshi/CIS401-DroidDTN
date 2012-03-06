package com.geoffroy;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainAppScreenActivity extends Activity {
	/* Database helper class */
    LocalStorage db;
    ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    db = new LocalStorage(this);
        posts = DataPacket.loadAll(db);
	}
	
	// Reload posts
	public void update(){
		posts = DataPacket.loadAll(db);
	}
	
	// To be called when a packet is clicked.
	// Creates a new ViewPostActivity with dp's fields as parameters (might be clunky?)
	public void click(DataPacket dp){
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
	
	// GETTERS
	public ArrayList<DataPacket> getPosts(){
		return posts;
	}
	
	public LocalStorage getDB(){
		return db;
	}

}
