package com.geoffroy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import android.bluetooth.BluetoothDevice;
import android.database.Cursor;

public class DeviceRecord {
	private long localID;
	private String address;
	private long lastConnection;
	private int messagesReceived;
	private int successfulConn;
	private int failedConn;
	private int spamScore;
	
	/*
	 * Default constructor
	 */
	public DeviceRecord(String address, long lastConnection, int messagesReceived,
			int successfulConn, int failedConn, int spamScore) {
		this.localID = 0;
		this.address = address;
		this.lastConnection = lastConnection;
		this.messagesReceived = messagesReceived;
		this.successfulConn = successfulConn;
		this.failedConn = failedConn;
		this.spamScore = spamScore;
	}
	
	public long getLocalID() { return localID; }
	public void setLocalID(long localID) { this.localID = localID; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public long getLastConnection() { return lastConnection; }
	public void setLastConnection(long lastConnection) { this.lastConnection = lastConnection; }
	public int getMessagesReceived() { return messagesReceived; }
	public void setMessagesReceived(int messagesReceived) { this.messagesReceived = messagesReceived; }
	public int getSuccessfulConn() { return successfulConn; }
	public void setSuccessfulConn(int successfulConn) { this.successfulConn = successfulConn; }
	public int getFailedConn() { return failedConn; }
	public void setFailedConn(int failedConn) { this.failedConn = failedConn; }
	public int getSpamScore() { return spamScore; }
	public void setSpamScore(int spamScore) { this.spamScore = spamScore; }

	public void persist(LocalStorage db) {
		if(localID > 0)
			db.update(this, Util.DB_DEVICES);
		else {
			long localID = db.insert(this, Util.DB_DEVICES);
			this.setLocalID(localID);
		}
	}
	
	public static ArrayList<DeviceRecord> loadAll(LocalStorage db) {
		Cursor mCursor = db.getAll(Util.DB_DEVICES);
		ArrayList<DeviceRecord> records = new ArrayList<DeviceRecord>();
		DeviceRecord record;
		
		if(mCursor.moveToFirst()) {
			do {
				record = new DeviceRecord(mCursor.getString(1), mCursor.getLong(2), 
						mCursor.getInt(3), mCursor.getInt(4), mCursor.getInt(5), 
						mCursor.getInt(6));
				record.setLocalID(mCursor.getInt(0));
				records.add(record);
			} while (mCursor.moveToNext());
		}
		
		return records;
	}
	
	public static BluetoothDevice selectBestDevice(List<BluetoothDevice> devices) {
		/*
		 * (int)min(10, (1/10) lastConnection.hours ^ 2)
		 * + max(10, messagesReceived / successfulConn)
		 * - max(10, 2 * failedConn)
		 */
		return null;
	}
}
