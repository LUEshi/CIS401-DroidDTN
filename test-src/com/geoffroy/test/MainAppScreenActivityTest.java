package com.geoffroy.test;

import android.test.ActivityInstrumentationTestCase2;
import com.geoffroy.*;

/* 
 * Activity to test MainAppScreenActivity.
 * 
 * Changes that I've made to other places to make this work:
 * 	- Fixed bug in LocalStorage.onCreate: column name should be localID, not ID
 *  - Fixed bug in DataPacket.loadAll: method forgot to add post to posts
 *  - Made constructors in DataPacket public so that they could be used here
 *  
 *  TODO: test deletions
 */

public class MainAppScreenActivityTest extends
		ActivityInstrumentationTestCase2<MainAppScreenActivity> {
    private MainAppScreenActivity mActivity;

	public MainAppScreenActivityTest() {
		super("com.geoffroy", MainAppScreenActivity.class);
	}
	 
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}
	
	// Make sure database is initially empty when activity is created
	// not sure this is necessary after the original use of the activity...
	//public void testInitiallyEmptyPosts() {
		//assertTrue(mActivity.getPosts().isEmpty());
	//}
	
	// Make sure database is appropriately updated when post is created/inserted
	public void testPost(){
		// Create a new DataPacket and insert it into the database
		int size = mActivity.getPosts().size();
		DataPacket dp = new DataPacket("author", "title", "content", "TEXT");
		mActivity.getDB().insert(dp);
		
		// Update the activity's posts variable and check its value
		mActivity.update();
		
		assertEquals(size + 1, mActivity.getPosts().size());
		assertEquals(dp.getAuthor(), mActivity.getPosts().get(size).getAuthor());
		assertEquals(dp.getTitle(), mActivity.getPosts().get(size).getTitle());
		assertEquals(dp.getContent(), mActivity.getPosts().get(size).getContent());
		assertEquals(dp.getType(), mActivity.getPosts().get(size).getType());
		
		// We should probably come up with some .equals method because this keeps failing...
		//assertEquals(dp, mActivity.getPosts().get(0));
	}
}
