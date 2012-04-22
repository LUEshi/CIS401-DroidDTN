package com.geoffroy;


import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


public class ViewPostActivity extends Activity {
	private LocalStorage ls;
	private DataPacket dp;
	private long localID;
	private long created;
	private String author;
	private String title;
	private String content;
	private String type;
	private int spamScore;
	private boolean isVisible;
	
	private TextView temp;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    Bundle bun = getIntent().getExtras();
	    localID = bun.getLong("localID");

	    ls = new LocalStorage(this);
	    dp = ls.getDataPacket(localID);
	    title=dp.getTitle();
	    created = dp.getCreated();
	    author=dp.getAuthor();
	    content=dp.getContent();
	    type=dp.getType();
	    spamScore=dp.getSpamScore();
	    isVisible=dp.getIsVisible();
	    
		//initialize view
		setContentView(R.layout.view_post);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_title);
		temp.setText(title);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_author);
		temp.setText(" " + author);
		
		temp=(TextView)this.findViewById(com.geoffroy.R.id.view_created);
		SimpleDateFormat sdf = new SimpleDateFormat("E MMM d, K:m a");
		temp.setText(" " + sdf.format(new Date(created)));
		
		
		if(type.equals(Util.POST_TYPE_IMAGE)){
			ImageView temp2=(ImageView)this.findViewById(com.geoffroy.R.id.view_picture);
			Bitmap b = decodeString(content);
			
			temp2.setImageBitmap(b);
		}
		else{
			temp=(TextView)this.findViewById(com.geoffroy.R.id.view_message);
			temp.setText(content);
		}
		
	}
	
	public Bitmap decodeString(String content){
		byte[] byteArray = Base64.decode(content, Base64.DEFAULT);

		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
	}
	
	public void markAsSpam(View v){
		dp.setSpamScore(spamScore+1);
		dp.setIsVisible(false);
		dp.persist(ls);
		finish();
	}
	
	public long getLocalID() { return localID; }
	public long getCreated() { return created; }
	public String getAuthor() { return author; }
	public String getPostTitle() { return title; }
	public String getContent() { return content; }
	public String getType() { return type; }
	public int getSpamScore() { return spamScore; }
	public boolean getIsVisible() { return isVisible; }

}
