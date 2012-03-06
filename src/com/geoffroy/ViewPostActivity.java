package com.geoffroy;

import android.app.Activity;
import android.os.Bundle;

public class ViewPostActivity extends Activity {
	private long localID;
	private long created;
	private String author;
	private String title;
	private String content;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    Bundle bun = getIntent().getExtras();
	    localID = bun.getLong("localID");
	    created = bun.getLong("created");
		author = bun.getString("author");
		title = bun.getString("title");
		content = bun.getString("content");
	}

}
