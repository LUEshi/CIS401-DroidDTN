package com.geoffroy;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainMenuActivity extends ListActivity {
	
	static final String[] APPS = new String[] { "Device List", 
		"Create Post", "View Post", "View Posts (Broken)"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// no more this
		// setContentView(R.layout.list_fruit);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.main,APPS));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		final Context c=this;
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CharSequence clicked = ((TextView) view).getText();
				if(clicked.equals(APPS[0]))
				{
					Intent i = new Intent().setClass(c, DeviceListActivity.class);
					startActivity(i);
				}
				else if(clicked.equals(APPS[1]))
				{

					Intent i = new Intent(c, NewPostActivity.class);
					startActivity(i);
				
				}
				else if(clicked.equals(APPS[2]))
				{

					Intent i = new Intent(c, ViewPostActivity.class);
					Bundle bun = new Bundle();
					bun.putString("author", "SUPER COOL GUYS");
					bun.putString("title", "Test Title!!!!!!11!1");
					bun.putString("content", "This is a super cool message brought to you by DroidDTN");
					i.putExtras(bun);
					startActivity(i);
				
				}
				else if(clicked.equals(APPS[3]))
				{

					Intent i = new Intent(c, MainAppScreenActivity.class);
					startActivity(i);
				
				}
		}});

	}
}