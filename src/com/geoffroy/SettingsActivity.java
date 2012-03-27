package com.geoffroy;

import com.geoffroy.R.id;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	private String username;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        
	    setContentView(R.layout.settings);
		
        // Load settings, if username doesn't exist, load model as default
        SharedPreferences settings = getPreferences(0);
        username = settings.getString("username", android.os.Build.MODEL);
        //initialize fields
        EditText t = (EditText)this.findViewById(id.editName);
        t.setText(username);
	}
	
	
	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences settings = getPreferences(0);
		SharedPreferences.Editor editor = settings.edit();
		//push values here
		editor.putString("username", username);
		editor.commit();
	}
	
	public void saveAndExit(View v){
		//get field values
		EditText t = (EditText)this.findViewById(id.editName);
        username = t.getText().toString();
		finish();
	}
	
	public void setUsername(String username) { this.username = username; }
	public String getUsername() { return this.username; }
}
