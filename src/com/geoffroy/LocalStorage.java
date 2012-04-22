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
	

	private static final int DATABASE_VERSION = 5;
	private static final String DATABASE_NAME = "droid_dtn.db";
	private static final String TABLE_PACKETS = "packets";
	private static final String TABLE_DEVICES = "devices";
	
	private Context context;
	private SQLiteDatabase db;

	public LocalStorage(Context context) {
		this.context = context;
		OpenHelper openHelper = new OpenHelper(this.context);
		this.db = openHelper.getWritableDatabase();
	}
	
	private void checkOpenDB() {
		if(!db.isOpen()) {
			OpenHelper openHelper = new OpenHelper(this.context);
			db = openHelper.getWritableDatabase();
		}
	}
	
	public long insert(Object data, String table) {
		checkOpenDB();
		
		ContentValues values = new ContentValues();
		
		if(table == Util.DB_PACKETS) {
			DataPacket packet = (DataPacket)data;	
			values.put("created", packet.getCreated());
			values.put("author", packet.getAuthor());
			values.put("title", packet.getTitle());
			values.put("content", packet.getContent());
			values.put("type", packet.getType());
			return db.insert(TABLE_PACKETS, null, values);		
		} else if(table == Util.DB_DEVICES) {
			DeviceRecord record = (DeviceRecord)data;
			values.put("address", record.getAddress());
			values.put("lastConnection", record.getLastConnection());
			values.put("messagesReceived", record.getMessagesReceived());
			values.put("successfulConn", record.getSuccessfulConn());
			values.put("failedConn", record.getFailedConn());
			values.put("spamScore", record.getSpamScore());
			return db.insert(TABLE_DEVICES, null, values);		
		}
		
		return 0;
	}
	
	public boolean delete(long ID, String table) {
		checkOpenDB();
		
		if(table == Util.DB_PACKETS)
			return db.delete(TABLE_PACKETS, "localID" + "=" + ID, null) > 0;
		else if(table == Util.DB_DEVICES)
			return db.delete(TABLE_DEVICES, "localID" + "=" + ID, null) > 0;
			
		return false;
	}
	
	public boolean update(Object data, String table) {
		checkOpenDB();
		
		ContentValues values = new ContentValues();
		
		if(table == Util.DB_PACKETS) {
			DataPacket packet = (DataPacket)data;
			values.put("created", packet.getCreated());
			values.put("author", packet.getAuthor());
			values.put("title", packet.getTitle());
			values.put("content", packet.getContent());
			values.put("type", packet.getType());
			return db.update(TABLE_PACKETS, values, "localID" + "=" + packet.getLocalID(), null) > 0;
		} else if(table == Util.DB_DEVICES) {
			DeviceRecord record = (DeviceRecord)data;
			values.put("address", record.getAddress());
			values.put("lastConnection", record.getLastConnection());
			values.put("messagesReceived", record.getMessagesReceived());
			values.put("successfulConn", record.getSuccessfulConn());
			values.put("failedConn", record.getFailedConn());
			values.put("spamScore", record.getSpamScore());
			return db.update(TABLE_DEVICES, values, "localID" + "=" + record.getLocalID(), null) > 0;
		}
		
		return false;
	}
	
	public Cursor get(long ID, String table) throws SQLException {
		checkOpenDB();
		
		Cursor mCursor;
		
		if(table == Util.DB_PACKETS) {
			mCursor = db.query(true, TABLE_PACKETS, 
					new String[] {"localID", "created", "author", "title", "content", "type"}, 
					"localID" + "=" + ID, null, null, null, null, null);
		} else if(table == Util.DB_DEVICES) {
			mCursor = db.query(true, TABLE_DEVICES, 
					new String[] {"localID", "address", "lastConnection", "messagesReceived", "successfulConn", "failedConn", "spamScore"}, 
					"localID" + "=" + ID, null, null, null, null, null);
		} else {
			return null;
		}
		
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	

	public DataPacket getDataPacket(long ID) throws SQLException{
		Cursor mCursor = db.query(true, TABLE_PACKETS, 
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
	

	public Map<String,String> getPostMap(long ID, String table) throws SQLException {
		checkOpenDB();
		
		Cursor mCursor = null;
		
		if(table == Util.DB_PACKETS) {
			mCursor = db.query(true, TABLE_PACKETS, 
					new String[] {"localID", "created", "author", "title", "content", "type"}, 
					"localID" + "=" + ID, null, null, null, null, null);
		} else if(table == Util.DB_DEVICES) {
			mCursor = db.query(true, TABLE_DEVICES, 
					new String[] {"localID", "address", "lastConnection", "messagesReceived", "successfulConn", "failedConn", "spamScore"}, 
					"localID" + "=" + ID, null, null, null, null, null);
		}
		

		Map<String,String> post = new HashMap<String,String>();
		if(mCursor != null)
		{
			if(mCursor.moveToFirst())
			{
				if(table == Util.DB_PACKETS) {
					post.put("localID", new Integer(mCursor.getInt(mCursor.getColumnIndex("localID"))).toString());
					post.put("created", new Integer(mCursor.getInt(mCursor.getColumnIndex("created"))).toString());
					post.put("author",mCursor.getString(mCursor.getColumnIndex("author")));
					post.put("title",mCursor.getString(mCursor.getColumnIndex("title")));
					post.put("content",mCursor.getString(mCursor.getColumnIndex("content")));
					post.put("type", mCursor.getString(mCursor.getColumnIndex("type")));
				} else if(table == Util.DB_DEVICES) {
					post.put("localID", new Integer(mCursor.getInt(mCursor.getColumnIndex("localID"))).toString());
					post.put("address",mCursor.getString(mCursor.getColumnIndex("address")));
					post.put("lastConnection", new Integer(mCursor.getInt(mCursor.getColumnIndex("lastConnection"))).toString());
					post.put("messagesReceived", new Integer(mCursor.getInt(mCursor.getColumnIndex("messagesReceived"))).toString());
					post.put("successfulConn", new Integer(mCursor.getInt(mCursor.getColumnIndex("successfulConn"))).toString());
					post.put("failedConn", new Integer(mCursor.getInt(mCursor.getColumnIndex("failedConn"))).toString());
					post.put("spamScore", new Integer(mCursor.getInt(mCursor.getColumnIndex("spamScore"))).toString());
				}
				return post;
			}
			else
				return null;
		}
		else
			return null;
	}
	

	
	public Cursor getAll(String table) throws SQLException {
		checkOpenDB();
		
		if(table == Util.DB_PACKETS) {
			return db.query(TABLE_PACKETS, 
					new String[] {"localID", "created", "author", "title", "content", "type"}, 
					null, null, null, null, null);
		} else if(table == Util.DB_DEVICES) {
			return db.query(TABLE_DEVICES,
					new String[] {"localID", "address", "lastConnection", "messagesReceived", "successfulConn", "failedConn", "spamScore"}, 
					null, null, null, null, null);
		}
		return null;
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
			db.execSQL("CREATE TABLE " + TABLE_PACKETS + " (" +
					"localID INTEGER PRIMARY KEY, " + 
	    			"created INTEGER, " +
	    			"author TEXT, " +
	    			"title TEXT, " +
	    			"content TEXT, " +
	    			"type TEXT);");
			db.execSQL("CREATE TABLE " + TABLE_DEVICES + " (" +
					"localID INTEGER PRIMARY KEY, " + 
	    			"address TEXT, " +
	    			"lastConnection INTEGER, " +
	    			"messagesReceived INTEGER, " +
	    			"successfulConn INTEGER, " +
	    			"failedConn INTEGER, " +
	    			"spamScore INTEGER);");
		}
 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PACKETS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
			onCreate(db);
		}
	}
}
