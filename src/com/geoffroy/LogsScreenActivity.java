package com.geoffroy;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogsScreenActivity extends ListActivity {
	
	private Handler timerHandler;
	Context context;
	
	private ArrayAdapter<String> aAdapter;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    timerHandler = new Handler();
	    context = this;
	    
	    @SuppressWarnings("unchecked")
		ArrayList<String> localEntries = (ArrayList<String>) Util.LOG_ENTRIES.clone();
	    aAdapter = new ArrayAdapter<String>(this, R.layout.log_item, localEntries);
	    setListAdapter(aAdapter);

	    ListView lv = getListView();
	    lv.setTextFilterEnabled(true);
	    
	    setContentView(R.layout.logs);
	}
	
	public void onStart() {
		super.onStart();
		
		timerHandler.removeCallbacks(timerTask);
        timerHandler.postDelayed(timerTask, 1000);
	}
	
	private Runnable timerTask = new Runnable() {
		public void run() {
			@SuppressWarnings("unchecked")
			ArrayList<String> localEntries = (ArrayList<String>) Util.LOG_ENTRIES.clone();
		    aAdapter = new ArrayAdapter<String>(context, R.layout.log_item, localEntries);
		    setListAdapter(aAdapter);
			
			timerHandler.postDelayed(this, 1000);
		}
	};
}
