package com.geoffroy;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		if(localID > 0) {
			db.update(this, Util.DB_DEVICES);
		} else {
			long localID = db.insert(this, Util.DB_DEVICES);
			this.setLocalID(localID);
		}
	}
	
	public static String recordsToString(LocalStorage db) {
		ArrayList<DeviceRecord> records = loadAll(db);
        String s = "";
        DeviceRecord record;
        Iterator<DeviceRecord> i = records.iterator();
        while(i.hasNext()) {
        	record = i.next();
        	s += "localID: " + record.getLocalID() + " address: " + record.getAddress() + " last conn: " + record.getLastConnection() + " failed: " + record.getFailedConn() + " success: " + record.getSuccessfulConn() + " messages: " + record.getMessagesReceived() + "\n";
        }
        return s;
	}
	
	public static DeviceRecord load(String address, LocalStorage db) {
		ArrayList<DeviceRecord> records = loadAll(db);
		DeviceRecord record;
		
		Iterator<DeviceRecord> i = records.iterator();
		while(i.hasNext()) {
			record = i.next();
			if(record.getAddress().equals(address)) {
				return record;
			}
		}
		return new DeviceRecord(address, 0, 0, 0, 0, 0);
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
	
	public static String selectBestDevice(List<String> devices, LocalStorage db) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Iterator<String> i = devices.iterator();
		DeviceRecord device;
		String deviceAddress = "";
		double maxScore = -1000;
		String maxScoreDevice = "";
		double score;
		int timeDiff;
		while(i.hasNext()) {
			deviceAddress = i.next();
			device = load(Encryption.encrypt(deviceAddress), db);
			
			if(device.getLastConnection() < 1) {
				score = 0;
				//Log.d("DR", "Device " + deviceAddress + " has score 0");
			} else {
				timeDiff = (int)((System.currentTimeMillis() - device.getLastConnection()) / 60000);
				double timeDiscount = -1.0 * Math.max(0, 10.0 - (Math.pow(timeDiff, 2) / 360.0));
				if(device.getSuccessfulConn() < 1) {
					score = timeDiscount;
					if(device.getFailedConn() > 10)
						score -= 100;	// Edge heuristic for devices with no connections
					//Log.d("DR", "Device " + deviceAddress + " has time discount " + timeDiscount + " (timeDiff: " + timeDiff + "), probMsgReceived: 0, total score: " + score);
				} else {
					double probSuccessConn = 1.0 * device.getSuccessfulConn() / (device.getSuccessfulConn() + device.getFailedConn());
					double msgsPerConn = 1.0 * Math.min(1.0, device.getMessagesReceived() / device.getSuccessfulConn());
					double probMsgReceived = 10.0 * probSuccessConn * msgsPerConn;
					score = probMsgReceived + timeDiscount + 1;
					//Log.d("DR", "Device " + deviceAddress + " has time discount " + timeDiscount + " (timeDiff: " + timeDiff + "), probMsgReceived: " + probMsgReceived + ", total score: " + score);
				}
			}
				
			if(score > maxScore) {
				maxScore = score;
				maxScoreDevice = deviceAddress;
				//Log.d("DR", "Device " + deviceAddress + " has maximum score " + score);
			}
		}
		return maxScoreDevice;
	}
}
