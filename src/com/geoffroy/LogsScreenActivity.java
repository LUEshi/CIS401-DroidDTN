package com.geoffroy;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogsScreenActivity extends ListActivity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    @SuppressWarnings("unchecked")
		ArrayList<String> localEntries = (ArrayList<String>) Util.LOG_ENTRIES.clone();
	    setListAdapter(new ArrayAdapter<String>(this, R.layout.log_item, localEntries));

	    ListView lv = getListView();
	    lv.setTextFilterEnabled(true);
	    
	    setContentView(R.layout.logs);
	}
}
