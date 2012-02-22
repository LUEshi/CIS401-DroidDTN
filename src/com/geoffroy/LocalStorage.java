package com.geoffroy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalStorage {
	
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "blog.db";
	private static final String TABLE_NAME = "posts";
	
	private Context context;
	private SQLiteDatabase db;

	public LocalStorage(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
	
	public long insert(DataPacket packet) {
		ContentValues values = new ContentValues();
		values.put("created", packet.getCreated());
		values.put("author", packet.getAuthor());
		values.put("title", packet.getTitle());
		values.put("content", packet.getContent());
		return db.insert(TABLE_NAME, null, values);		
	}
	
	public boolean delete(long ID) {
		return db.delete(TABLE_NAME, "localID" + "=" + ID, null) > 0;
	}
	
	public boolean update(DataPacket packet) {
		ContentValues values = new ContentValues();
		values.put("created", packet.getCreated());
		values.put("author", packet.getAuthor());
		values.put("title", packet.getTitle());
		values.put("content", packet.getContent());
		return db.update(TABLE_NAME, values, "localID" + "=" + packet.getLocalID(), null) > 0;
	}
	
	public Cursor get(long ID) throws SQLException {
		Cursor mCursor = db.query(true, TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content"}, 
				"localID" + "=" + ID, null, null, null, null, null);
	
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public Cursor getAll() throws SQLException {
		return db.query(TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content"}, 
				null, null, null, null, null);
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
 
		@Override
      	public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
					"ID INTEGER, " + 
	    			"created INTEGER, " +
	    			"author TEXT, " +
	    			"title TEXT, " +
	    			"content TEXT);");
		}
 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
