package com.geoffroy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewPostActivity extends Activity {
	private String title;
	private String content;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.new_post);
	}
	
	// Bundles the information and sends it back to MainAppScreenActivity.
	// To be called by publish button.
	public void onPublishButtonClick(View v){
		
		EditText titleText = (EditText) findViewById(R.id.newPostTitle);
		EditText messageText = (EditText) findViewById(R.id.newMessage);
		
		setPostTitle(titleText.getText().toString());
		setContent(messageText.getText().toString());
		
		//TODO: actual author
		DataPacket newPost = new DataPacket("PERSON!", title, content);
		newPost.persist(new LocalStorage(this));
		
		Intent i = new Intent();
		Bundle bun = new Bundle();
		bun.putString("title", title);
		bun.putString("content", content);
		i.putExtras(bun);
		
		//TODO: remove test code
/*		Object alertDialog = new AlertDialog.Builder(this).create();  
//		  ((Activity) alertDialog).setTitle(title);  
		  ((AlertDialog) alertDialog).setMessage("Message: " + content + "\nPushed to DB");  
		  ((AlertDialog) alertDialog).setButton("OK",new DialogInterface.OnClickListener(){  
		    public void onClick(DialogInterface dialog, int which) {  
		    return;  
		  } });   
		  ((Dialog) alertDialog).show();
	*/	
		setResult(RESULT_OK, i);
		finish();
	}
	
	public String getPostTitle() {	return title;	}
	public void setPostTitle(String title) {	this.title = title;	}
	public String getContent() {	return content;	}
	public void setContent(String content) {	this.content = content;	}

}
