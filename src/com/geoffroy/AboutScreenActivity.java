package com.geoffroy;

import android.app.Activity;
import android.os.Bundle;

public class AboutScreenActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//initialize view
		setContentView(R.layout.about);
	}

}
