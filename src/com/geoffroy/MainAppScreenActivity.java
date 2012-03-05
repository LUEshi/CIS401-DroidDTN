package com.geoffroy;

import java.util.ArrayList;

import android.app.Activity;
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
	
	public void update(){
		posts = DataPacket.loadAll(db);
	}
	
	public ArrayList<DataPacket> getPosts(){
		return posts;
	}
	
	public LocalStorage getDB(){
		return db;
	}

}
