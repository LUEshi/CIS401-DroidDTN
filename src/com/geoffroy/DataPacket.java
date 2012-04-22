package com.geoffroy;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;

public class DataPacket {
	private long localID;
	private long created;
	private String author;
	private String title;
	private String content;
	private String type;
	private int spamScore;
	private boolean isVisible;
	
	/*
	 * Default constructor
	 */
	public DataPacket() {
		this.localID = 0;
		this.created = 0;
		this.author = "";
		this.title = "";
		this.content = "";
		this.type = "";
		this.spamScore = 0;
		this.isVisible = true;
	}
	
	public DataPacket(String author, String title, String content, String type, int spamScore, boolean isVisible) {
		this.localID = 0;
		this.created = System.currentTimeMillis();
		this.author = author;
		this.title = title;
		this.content = content;
		this.type = type;
		this.spamScore = spamScore;
		this.isVisible = isVisible;
	}
	
	/*
	 * Constructor used when receiving a post from another device
	 */
	public DataPacket(String jsonString) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		this.created = json.getLong("created");
		this.author = json.getString("author");
		this.title = json.getString("title");
		this.content = json.getString("content");
		this.type = json.getString("type");
		this.spamScore = json.getInt("spamScore");
		this.isVisible = json.getBoolean("isVisible");
	}

	public long getLocalID() {	return localID;	}
	public void setLocalID(long localID) {	this.localID = localID;	}
	public long getCreated() {	return created;	}
	public void setCreated(long created) {	this.created = created;	}
	public String getAuthor() {	return author;	}
	public void setAuthor(String author) {	this.author = author;	}
	public String getTitle() {	return title;	}
	public void setTitle(String title) {	this.title = title;	}
	public String getContent() {	return this.content;	}
	public void setContent(String content) {	this.content = content;	}
	public String getType() {	return type;	}
	public void setType(String type) {	this.type = type;	}
	public int getSpamScore() { return spamScore; }
	public void setSpamScore(int spamScore) { this.spamScore = spamScore; }
	public boolean getIsVisible() { return this.isVisible; }
	public void setIsVisible(boolean isVisible) { this.isVisible = isVisible; }
	
	public int hashCode() {
		String s = String.valueOf(created) + author + title;
		return s.hashCode();	
	}
	
	public void persist(LocalStorage db) {
		if(localID > 0)
			db.update(this, Util.DB_PACKETS);
		else {
			long localID = db.insert(this, Util.DB_PACKETS);
			this.setLocalID(localID);
		}
	}
	
	public String toJson() {
		JSONObject json = new JSONObject();
		try {
			json.put("created", this.created);
			json.put("author", this.author);
			json.put("title", this.title);
			json.put("content", this.content);
			json.put("type", this.type);
			json.put("spamScore", this.spamScore);
			json.put("isVisible", this.isVisible);
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static ArrayList<DataPacket> loadAll(LocalStorage db, boolean onlyVisible) {
		Cursor mCursor = db.getAll(Util.DB_PACKETS);
		ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
		DataPacket post;
		
		if(mCursor.moveToFirst()) {
			do {
				post = new DataPacket(mCursor.getString(2), mCursor.getString(3), mCursor.getString(4),
									  mCursor.getString(5), mCursor.getInt(6), mCursor.getInt(7)==1);
				post.setLocalID(mCursor.getInt(0));
				post.setCreated(mCursor.getLong(1));
				if (!onlyVisible || post.getIsVisible())
					posts.add(post);
			} while (mCursor.moveToNext());
		}
		
		return posts;
	}
	
	public static ArrayList<Integer> getBlogComparisonVector(LocalStorage db) {
		ArrayList<DataPacket> posts = loadAll(db, false);
		ArrayList<Integer> vector = new ArrayList<Integer>();
		
		for(DataPacket post : posts) {
			vector.add(post.hashCode());
		}
		return vector;
	}
	
	/*
	 * Returns a list of hashes corresponding to posts which we have but the other device doesn't have.
	 */
	public static ArrayList<Integer> diffComparisonVectors(LocalStorage db, ArrayList<Integer> foreignVector) {
		ArrayList<Integer> missingPosts = new ArrayList<Integer>();
		ArrayList<Integer> localVector = getBlogComparisonVector(db);
		
		for(Integer hash : localVector) {
			if(!foreignVector.contains(hash)) {
				missingPosts.add(hash);
			}
		}
		return missingPosts;
	}
	
	public static String packetsToString(LocalStorage db) {
		ArrayList<DataPacket> packets = loadAll(db, false);
        String s = "";
        DataPacket packet;
        Iterator<DataPacket> i = packets.iterator();
        while(i.hasNext()) {
        	packet = i.next();
        	s += "localID: " + packet.getLocalID() + " author: " + packet.getAuthor() + " title: " + packet.getTitle() +
        		 " content: " + packet.getContent() + " type: " + packet.getType() + " spamScore: " + packet.getSpamScore() +
        		 " isVisible: " + packet.getIsVisible() + "\n";
        }
        return s;
	}
}
