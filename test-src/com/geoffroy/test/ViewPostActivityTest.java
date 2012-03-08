package com.geoffroy.test;

import android.test.ActivityInstrumentationTestCase2;
import com.geoffroy.*;

/* 
 * Activity to test ViewPostActivity.
 * 
 *  TODO: doesn't work - null pointer exception in oncreate for vActivity
 */

public class ViewPostActivityTest extends
	ActivityInstrumentationTestCase2<ViewPostActivity> {
	
	private MainAppScreenActivity mActivity;
	private ViewPostActivity vActivity;
	
	public ViewPostActivityTest() {
		super("com.geoffroy", ViewPostActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = new MainAppScreenActivity();
		vActivity = this.getActivity();
	}
	
	public void testClick(){
		DataPacket dp = new DataPacket("author", "title", "content");
		mActivity.click(dp);
		assertEquals(dp.getLocalID(), vActivity.getLocalID());
		assertEquals(dp.getCreated(), vActivity.getCreated());
		assertEquals(dp.getAuthor(), vActivity.getAuthor());
		assertEquals(dp.getTitle(), vActivity.getPostTitle());
		assertEquals(dp.getContent(), vActivity.getContent());
	}

}
