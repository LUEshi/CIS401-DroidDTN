package com.geoffroy;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewPictureActivity extends Activity {

	private static final int SELECT_PICTURE = 1;

	private String selectedImagePath;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_picture);
		
		selectedImagePath="";
		Button b =((Button) findViewById(R.id.Button01));
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
						"Select Picture"), SELECT_PICTURE);
			}
		});
	}

	public void onPublishButtonClick(View v)
	{
		if(selectedImagePath.equals("")) {
			Toast.makeText(this, "Please select an image first!", Toast.LENGTH_LONG).show();
			return;
		}

		try{
			SharedPreferences settings = getSharedPreferences("PrefsFile", MODE_PRIVATE);
	        String username = settings.getString("username", android.os.Build.MODEL);
	        
			EditText titleText = (EditText) findViewById(R.id.newPostTitle);

			String title = (titleText.getText().toString());
			String content = encodePicture(selectedImagePath);
			
			DataPacket newPost = new DataPacket(username, title, content, Util.POST_TYPE_IMAGE, 0, true);
			newPost.persist(new LocalStorage(this));
			
			finish();
		}
		catch (UnsupportedEncodingException e) {
			Toast.makeText(this, "This image could not be sent.", Toast.LENGTH_LONG).show();
		}
		

	}
	
	public String encodePicture(String fileName) throws UnsupportedEncodingException{
		Bitmap bitmap = BitmapFactory.decodeFile(fileName);
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream); 
	    byte[] byteArray = outputStream.toByteArray();
	    Base64.encodeToString(byteArray, Base64.DEFAULT);
	    return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				selectedImagePath = getPath(selectedImageUri);
				ImageView v = (ImageView) findViewById(R.id.img);
				Bitmap bm = BitmapFactory.decodeFile(selectedImagePath);
				v.setImageBitmap(bm);
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}