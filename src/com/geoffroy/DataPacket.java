package com.geoffroy;

import java.util.ArrayList;

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
	}
	
	public DataPacket(String author, String title, String content, String type) {
		this.localID = 0;
		this.created = System.currentTimeMillis();
		this.author = author;
		this.title = title;
		this.content = content;
		this.type = type;
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
	
	public int hashCode() {
		String s = String.valueOf(created) + author + title;
		return s.hashCode();	
	}
	
	public void persist(LocalStorage db) {
		if(localID > 0)
			db.update(this);
		else {
			long localID = db.insert(this);
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
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	private static String getCurrentAuthor() {
		/* Use Android SharedPreferences to persist the author. */
		return null;
	}
	
	public static ArrayList<DataPacket> loadAll(LocalStorage db) {
		Cursor mCursor = db.getAll();
		ArrayList<DataPacket> posts = new ArrayList<DataPacket>();
		DataPacket post;
		
		if(mCursor.moveToFirst()) {
			do {
				post = new DataPacket(mCursor.getString(2), mCursor.getString(3), mCursor.getString(4), mCursor.getString(5));
				post.setLocalID(mCursor.getInt(0));
				post.setCreated(mCursor.getLong(1));
				posts.add(post);
			} while (mCursor.moveToNext());
		}
		
		return posts;
	}
	
	public static ArrayList<Integer> getBlogComparisonVector(LocalStorage db) {
		ArrayList<DataPacket> posts = loadAll(db);
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
}
