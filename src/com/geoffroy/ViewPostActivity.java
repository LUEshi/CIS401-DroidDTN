package com.geoffroy;


import android.widget.TextView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


public class ViewPostActivity extends Activity {
	private long localID;
	private long created;
	private String author;
	private String title;
	private String content;
	private String type;
	
	private TextView temp;
	
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
		type = bun.getString("type");
		
		//initialize view
		setContentView(R.layout.view_post);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_title);
		temp.setText(title);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_author);
		temp.setText(author);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_message);
		temp.setText(content);
		
		
	}
	
	public Bitmap decodeString(String content){
		byte[] byteArray = content.getBytes();
		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	}
	
	public long getLocalID() { return localID; }
	public long getCreated() { return created; }
	public String getAuthor() { return author; }
	public String getPostTitle() { return title; }
	public String getContent() { return content; }
	public String getType() { return type; }

}
