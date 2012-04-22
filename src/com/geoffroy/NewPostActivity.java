package com.geoffroy;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewPostActivity extends Activity {
	public static final String PREFS_NAME = "PrefsFile";
	private String title;
	private String content;
	private String username;
	private String type;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.new_post);
	}
	
	// Bundles the information and sends it back to MainAppScreenActivity.
	// To be called by publish button.
	public void onPublishButtonClick(View v){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        username = settings.getString("username", android.os.Build.MODEL);
        
		EditText titleText = (EditText) findViewById(R.id.newPostTitle);
		EditText messageText = (EditText) findViewById(R.id.newMessage);
		
		
		setPostTitle(titleText.getText().toString());
		setContent(messageText.getText().toString());
		
		DataPacket newPost = new DataPacket(username, title, content, Util.POST_TYPE_TEXT, 0, true);
		newPost.persist(new LocalStorage(this));
		
		finish();
	}
		
	public String getPostTitle() {	return title;	}
	public void setPostTitle(String title) {	this.title = title;	}
	public String getContent() {	return content;	}
	public void setContent(String content) {	this.content = content;	}
	public String getType() {	return type;	}
	public void setType(String type) {	this.type = type;	}

}
