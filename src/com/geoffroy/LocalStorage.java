package com.geoffroy;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalStorage {
	
	private static final int DATABASE_VERSION = 3;
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
		values.put("type", packet.getType());
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
		values.put("type", packet.getType());
		return db.update(TABLE_NAME, values, "localID" + "=" + packet.getLocalID(), null) > 0;
	}
	
	public Cursor get(long ID) throws SQLException {
		Cursor mCursor = db.query(true, TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content", "type"}, 
				"localID" + "=" + ID, null, null, null, null, null);
	
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public DataPacket getDataPacket(long ID) throws SQLException{
		Cursor mCursor = db.query(true, TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content", "type"}, 
				"localID" + "=" + ID, null, null, null, null, null);
	
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		DataPacket d = new DataPacket();
		d.setLocalID(ID);
		d.setCreated(mCursor.getLong(mCursor.getColumnIndex("created")));
		d.setAuthor(mCursor.getString(mCursor.getColumnIndex("author")));
		d.setTitle(mCursor.getString(mCursor.getColumnIndex("title")));
		d.setContent(mCursor.getString(mCursor.getColumnIndex("content")));
		d.setType(mCursor.getString(mCursor.getColumnIndex("type")));
		return d;
	}
	
	//TODO: is that the correct number of nulls?
	public Map<String,String> getPostMap(long ID) throws SQLException {
		Cursor mCursor = db.query(true, TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content", "type"}, 
				"localID" + "=" + ID, null, null, null, null, null);
	
		Map<String,String> post = new HashMap<String,String>();
		if(mCursor != null)
		{
			if(mCursor.moveToFirst())
			{
				post.put("localID", new Integer(mCursor.getInt(mCursor.getColumnIndex("localID"))).toString());
				post.put("created", new Integer(mCursor.getInt(mCursor.getColumnIndex("created"))).toString());
				post.put("author",mCursor.getString(mCursor.getColumnIndex("author")));
				post.put("title",mCursor.getString(mCursor.getColumnIndex("title")));
				post.put("content",mCursor.getString(mCursor.getColumnIndex("content")));
				post.put("type", mCursor.getString(mCursor.getColumnIndex("type")));
				return post;
			}
			else
				return null;
		}
		else
			return null;
	}
	

	
	public Cursor getAll() throws SQLException {
		return db.query(TABLE_NAME, 
				new String[] {"localID", "created", "author", "title", "content", "type"}, 
				null, null, null, null, null);
	}
	
	public void close() {
		db.close();
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
 
		@Override
      	public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
					"localID INTEGER, " + 
	    			"created INTEGER, " +
	    			"author TEXT, " +
	    			"title TEXT, " +
	    			"content TEXT, " +
	    			"type TEXT);");
		}
 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
