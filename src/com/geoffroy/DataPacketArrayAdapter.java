package com.geoffroy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DataPacketArrayAdapter extends ArrayAdapter<DataPacket> {

	private LayoutInflater inflater;
	private List<DataPacket> data;


	public DataPacketArrayAdapter(Context context,List<DataPacket> objects) {
		//http://sanathnandasiri.blogspot.com/2011/11/how-to-work-with-android-listview.html
		// TODO Auto-generated constructor stub
		super(context, R.layout.message_item, objects);

		inflater= LayoutInflater.from(context);
		this.data=objects;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		//if it's not create convertView yet create new one and consume it
		if(convertView == null){
			//instantiate convertView using our employee_list_item
			convertView = inflater.inflate(R.layout.message_item, null);
			//get new ViewHolder
			holder =new ViewHolder();
			//get all item in ListView item to corresponding fields in our ViewHolder class
			//holder.image=(ImageView) convertView.findViewById(R.id.imageViewEmployer);
			holder.name =(TextView) convertView.findViewById(R.id.textViewName);
			holder.address =(TextView) convertView.findViewById(R.id.textViewAddress);
			holder.date = (TextView) convertView.findViewById(R.id.textViewDate);
			//set tag of convertView to the holder
			convertView.setTag(holder);
		}
		//if it's exist convertView then consume it
		else {
			holder =(ViewHolder) convertView.getTag();
		}      
		
		//holder.image.setImageResource(R.drawable.user);
		holder.name.setText((CharSequence) data.get(position).getTitle());
		holder.address.setText("By: "+(CharSequence) data.get(position).getAuthor());
		SimpleDateFormat sdf = new SimpleDateFormat("E MMM d, K:m a");
		holder.date.setText(" " + sdf.format(new Date(data.get(position).getCreated())));
		
		return convertView;
	}
	//ViewHolder class that hold over ListView Item
	static class ViewHolder{
		//ImageView image;
		TextView name;
		TextView address;
		TextView date;
	}
}
