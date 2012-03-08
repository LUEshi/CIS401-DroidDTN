package com.geoffroy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NewPostActivity extends Activity {
	private long localID;
	private long created;
	private String author;
	private String title;
	private String content;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	}
	
	// Bundles the information and sends it back to MainAppScreenActivity.
	// To be called by publish button.
	public void publish(){
		Intent i = new Intent();
		Bundle bun = new Bundle();
		bun.putString("author", author);
		bun.putString("title", title);
		bun.putString("content", content);
		i.putExtras(bun);
		
		setResult(RESULT_OK, i);
		finish();
	}
	
	public long getLocalID() {	return localID;	}
	public void setLocalID(long localID) {	this.localID = localID;	}
	public long getCreated() {	return created;	}
	public void setCreated(long created) {	this.created = created;	}
	public String getAuthor() {	return author;	}
	public void setAuthor(String author) {	this.author = author;	}
	public String getPostTitle() {	return title;	}
	public void setTitle(String title) {	this.title = title;	}
	public String getContent() {	return content;	}
	public void setContent(String content) {	this.content = content;	}

}
