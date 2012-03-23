package com.geoffroy;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class DataPacketArrayAdapter extends ArrayAdapter<DataPacket> {

	public DataPacketArrayAdapter(Context context,List<DataPacket> objects) {
		//http://sanathnandasiri.blogspot.com/2011/11/how-to-work-with-android-listview.html
		super(context, R.layout.select_post/* some layout */ , objects);
		// TODO Auto-generated constructor stub
	}

}
