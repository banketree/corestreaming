package com.chaatz.live.app.base.fragment;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;


public class BaseListFragment extends ListFragment
{
	//Constant
	protected final static String TITLE = "TITLE" ;
	
	// shared value
	protected String mTitle;

	protected void setTitle( String title ) {
		ActionBarActivity activity = getActionBarActivity();
		if ( null != activity ) {
			activity.getSupportActionBar().setTitle( title );
		}
	}

	protected void setTitle( int title ) {
		ActionBarActivity activity = getActionBarActivity();
		if ( null != activity ) {
			activity.getSupportActionBar().setTitle( title );
		}
	}

	protected ActionBarActivity getActionBarActivity() {
		Activity activity = getActivity();
		if ( activity instanceof ActionBarActivity ) {
			return ( ( ActionBarActivity ) activity );
		} else {
			return null;
		}
	}
	
}
