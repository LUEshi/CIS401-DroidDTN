package com.geoffroy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	private String username;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        
        // Load settings, if username doesn't exist, load model as default
        SharedPreferences settings = getPreferences(0);
        username = settings.getString("username", android.os.Build.MODEL);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("username", username);
		editor.commit();
	}
	
	public void saveAndExit(){
		finish();
	}
	
	public void setUsername(String username) { this.username = username; }
	public String getUsername() { return this.username; }
}
